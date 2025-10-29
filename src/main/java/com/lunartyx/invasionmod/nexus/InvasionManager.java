package com.lunartyx.invasionmod.nexus;

import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.entity.NexusBoundMob;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

/**
 * Server-side controller that recreates the Nexus invasion loop. The manager is
 * responsible for reading wave definitions, spawning mobs around the Nexus, and
 * advancing to subsequent waves once players survive the current assault.
 */
public class InvasionManager {
    private static final String NBT_PENDING = "Pending";
    private static final String NBT_RUNNING = "Running";
    private static final String NBT_CONTINUOUS = "Continuous";
    private static final String NBT_WAVE = "CurrentWave";
    private static final String NBT_BREAK = "Break";
    private static final String NBT_WAITING = "Waiting";
    private static final String NBT_SPAWN_COOLDOWN = "SpawnCooldown";
    private static final String NBT_TOTAL_MOBS = "TotalMobs";
    private static final String NBT_SPAWNED = "Spawned";
    private static final String NBT_DEFINITION_WAVE = "DefinitionWave";
    private static final String NBT_DIFFICULTY = "DefinitionDifficulty";
    private static final String NBT_TIER = "DefinitionTier";
    private static final String NBT_LENGTH = "DefinitionLength";

    private final NexusBlockEntity nexus;
    private final String mobTag;
    private final Queue<PendingSpawn> spawnQueue = new ArrayDeque<>();

    private final AdaptiveWaveBuilder waveBuilder = new AdaptiveWaveBuilder();
    private WaveDefinition currentDefinition;
    private AdaptiveWaveBuilder.Parameters currentParameters;
    private boolean running;
    private boolean continuous;
    private boolean waitingForNextWave;
    private int currentWave;
    private int breakTicksRemaining;
    private int spawnCooldown;
    private int totalMobsThisWave;
    private int mobsSpawnedThisWave;

    public InvasionManager(NexusBlockEntity nexus) {
        this.nexus = nexus;
        this.mobTag = "invasionmod:nexus_" + nexus.getPos().asLong();
    }

    private AdaptiveWaveBuilder.Parameters parametersForWave(int waveNumber) {
        AdaptiveWaveBuilder.Parameters base = waveBuilder.parametersForWave(waveNumber, continuous);
        if (!continuous) {
            return base;
        }

        float bonus = Math.max(0.0F, nexus.getPowerLevel() / 4500.0F);
        float difficulty = Math.max(0.8F, base.difficulty() + bonus);
        float tier = MathHelper.clamp(base.tierLevel() + bonus, 0.9F, 6.0F);
        return new AdaptiveWaveBuilder.Parameters(difficulty, tier, base.lengthSeconds(), base.waveNumber());
    }

    public boolean tick(World world) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return false;
        }

        if (!running) {
            return false;
        }

        boolean dirty = false;

        if (currentDefinition == null) {
            beginWave(serverWorld, currentWave <= 0 ? 1 : currentWave);
            dirty = true;
        }

        if (spawnCooldown > 0) {
            spawnCooldown--;
        }

        if (spawnCooldown <= 0 && !spawnQueue.isEmpty()) {
            PendingSpawn pending = spawnQueue.peek();
            if (pending != null && spawnMob(serverWorld, pending.entry())) {
                pending.decrement();
                mobsSpawnedThisWave++;
                spawnCooldown = pending.entry().nextDelay(serverWorld.getRandom());
                if (pending.remaining() <= 0) {
                    spawnQueue.poll();
                }
                dirty = true;
            } else {
                spawnCooldown = 20;
            }
        }

        int activeMobs = countTaggedMobs(serverWorld);
        int pendingSpawns = pendingMobs();
        nexus.updateMobCounts(pendingSpawns, activeMobs);

        if (spawnQueue.isEmpty() && activeMobs == 0) {
            if (!waitingForNextWave) {
                waitingForNextWave = true;
                breakTicksRemaining = currentDefinition != null ? currentDefinition.breakTicks() : 0;
                nexus.onWaveCleared(currentWave, breakTicksRemaining);
                dirty = true;
            } else {
                if (breakTicksRemaining > 0) {
                    breakTicksRemaining--;
                    nexus.setWaveCooldown(breakTicksRemaining);
                } else {
                    advanceWave(serverWorld);
                    dirty = true;
                }
            }
        } else if (waitingForNextWave) {
            waitingForNextWave = false;
            nexus.setWaveCooldown(0);
            dirty = true;
        }

        return dirty;
    }

    public void startInvasion(ServerWorld world, int startingWave) {
        running = true;
        continuous = false;
        waitingForNextWave = false;
        currentWave = Math.max(1, startingWave);
        breakTicksRemaining = 0;
        spawnCooldown = 0;
        totalMobsThisWave = 0;
        mobsSpawnedThisWave = 0;
        spawnQueue.clear();
        beginWave(world, currentWave);
    }

    public void startContinuous(ServerWorld world, int startingWave) {
        running = true;
        continuous = true;
        waitingForNextWave = false;
        currentWave = Math.max(1, startingWave);
        breakTicksRemaining = 0;
        spawnCooldown = 0;
        totalMobsThisWave = 0;
        mobsSpawnedThisWave = 0;
        spawnQueue.clear();
        beginWave(world, currentWave);
    }

    public void stop(boolean announce) {
        running = false;
        waitingForNextWave = false;
        spawnQueue.clear();
        currentDefinition = null;
        currentParameters = null;
        breakTicksRemaining = 0;
        spawnCooldown = 0;
        totalMobsThisWave = 0;
        mobsSpawnedThisWave = 0;
        nexus.updateMobCounts(0, 0);
        nexus.setWaveCooldown(0);
        nexus.onInvasionStopped(announce);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public int getPendingSpawnCount() {
        return pendingMobs();
    }

    public int getCurrentWaveTarget() {
        return totalMobsThisWave;
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putBoolean(NBT_RUNNING, running);
        nbt.putBoolean(NBT_CONTINUOUS, continuous);
        nbt.putInt(NBT_WAVE, currentWave);
        nbt.putInt(NBT_BREAK, breakTicksRemaining);
        nbt.putBoolean(NBT_WAITING, waitingForNextWave);
        nbt.putInt(NBT_SPAWN_COOLDOWN, spawnCooldown);
        nbt.putInt(NBT_TOTAL_MOBS, totalMobsThisWave);
        nbt.putInt(NBT_SPAWNED, mobsSpawnedThisWave);
        if (currentDefinition != null) {
            nbt.putInt(NBT_DEFINITION_WAVE, currentDefinition.waveNumber());
            if (currentParameters != null) {
                nbt.putFloat(NBT_DIFFICULTY, currentParameters.difficulty());
                nbt.putFloat(NBT_TIER, currentParameters.tierLevel());
                nbt.putInt(NBT_LENGTH, currentParameters.lengthSeconds());
            }
        }

        NbtList pending = new NbtList();
        for (PendingSpawn entry : spawnQueue) {
            pending.add(entry.toNbt());
        }
        nbt.put(NBT_PENDING, pending);
    }

    public void readNbt(NbtCompound nbt) {
        running = nbt.getBoolean(NBT_RUNNING);
        continuous = nbt.getBoolean(NBT_CONTINUOUS);
        currentWave = nbt.getInt(NBT_WAVE);
        breakTicksRemaining = nbt.getInt(NBT_BREAK);
        waitingForNextWave = nbt.getBoolean(NBT_WAITING);
        spawnCooldown = nbt.getInt(NBT_SPAWN_COOLDOWN);
        totalMobsThisWave = nbt.getInt(NBT_TOTAL_MOBS);
        mobsSpawnedThisWave = nbt.getInt(NBT_SPAWNED);

        if (running) {
            int storedWave = nbt.contains(NBT_DEFINITION_WAVE) ? nbt.getInt(NBT_DEFINITION_WAVE) : currentWave;
            int resolvedWave = storedWave <= 0 ? Math.max(1, currentWave) : storedWave;
            AdaptiveWaveBuilder.Parameters defaults = parametersForWave(resolvedWave);
            float difficulty = nbt.contains(NBT_DIFFICULTY) ? nbt.getFloat(NBT_DIFFICULTY) : defaults.difficulty();
            float tier = nbt.contains(NBT_TIER) ? nbt.getFloat(NBT_TIER) : defaults.tierLevel();
            int length = nbt.contains(NBT_LENGTH) ? nbt.getInt(NBT_LENGTH) : defaults.lengthSeconds();
            currentParameters = new AdaptiveWaveBuilder.Parameters(difficulty, tier, length, resolvedWave);
            currentDefinition = waveBuilder.build(waveSeed(resolvedWave), currentParameters, continuous);
            totalMobsThisWave = currentDefinition.totalMobCount();
        } else {
            currentParameters = null;
            currentDefinition = null;
        }

        spawnQueue.clear();
        if (currentDefinition != null && nbt.contains(NBT_PENDING, NbtElement.LIST_TYPE)) {
            Map<Identifier, WaveEntry> lookup = currentDefinition.entryLookup();
            NbtList pending = nbt.getList(NBT_PENDING, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < pending.size(); i++) {
                NbtCompound entryData = pending.getCompound(i);
                Identifier id = Identifier.tryParse(entryData.getString(PendingSpawn.NBT_ID));
                if (id == null) {
                    continue;
                }
                WaveEntry entry = lookup.get(id);
                if (entry != null) {
                    int remaining = MathHelper.clamp(entryData.getInt(PendingSpawn.NBT_REMAINING), 0, entry.amount());
                    spawnQueue.add(new PendingSpawn(entry, remaining));
                }
            }
        }
    }

    private void beginWave(ServerWorld world, int waveNumber) {
        currentWave = Math.max(1, waveNumber);
        currentParameters = parametersForWave(currentWave);
        currentDefinition = waveBuilder.build(waveSeed(currentWave), currentParameters, continuous);
        spawnQueue.clear();
        for (WaveEntry entry : currentDefinition.entries()) {
            if (entry.amount() > 0) {
                spawnQueue.add(new PendingSpawn(entry));
            }
        }
        totalMobsThisWave = currentDefinition.totalMobCount();
        mobsSpawnedThisWave = 0;
        spawnCooldown = 20;
        waitingForNextWave = false;
        breakTicksRemaining = currentDefinition.breakTicks();
        nexus.setWaveCooldown(0);
        nexus.updateMobCounts(pendingMobs(), 0);
        nexus.onWaveStarted(currentWave, totalMobsThisWave);
    }

    private void advanceWave(ServerWorld world) {
        currentWave = Math.max(1, currentWave + 1);
        beginWave(world, currentWave);
    }

    private long waveSeed(int waveNumber) {
        long positionSeed = nexus.getPos().asLong();
        long waveComponent = (long) waveNumber * 341873128712L;
        return positionSeed ^ waveComponent;
    }

    private int pendingMobs() {
        int total = 0;
        for (PendingSpawn pending : spawnQueue) {
            total += pending.remaining();
        }
        return total;
    }

    private int countTaggedMobs(ServerWorld world) {
        BlockPos pos = nexus.getPos();
        Box box = new Box(pos).expand(nexus.getSpawnRadius());
        return world.getEntitiesByClass(MobEntity.class, box, mob -> mob.getCommandTags().contains(mobTag)).size();
    }

    private boolean spawnMob(ServerWorld world, WaveEntry entry) {
        EntityType<? extends MobEntity> entityType = entry.entityType();
        MobEntity mob = entityType.create(world);
        if (mob == null) {
            return false;
        }

        BlockPos center = nexus.getPos();
        int radius = Math.max(8, nexus.getSpawnRadius());

        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = world.getRandom().nextDouble() * Math.PI * 2.0D;
            int distance = world.getRandom().nextBetween(Math.max(6, radius - 8), radius + 8);
            int spawnX = center.getX() + MathHelper.floor(Math.cos(angle) * distance);
            int spawnZ = center.getZ() + MathHelper.floor(Math.sin(angle) * distance);
            int spawnY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);
            if (entry.spawnType() == SpawnType.AIR) {
                spawnY += 4 + world.getRandom().nextBetween(0, 6);
            }
            BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);

            if (!world.getWorldBorder().contains(spawnPos)) {
                continue;
            }

            mob.refreshPositionAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, world.getRandom().nextFloat() * 360.0F, 0.0F);
            if (!world.isSpaceEmpty(mob)) {
                continue;
            }

            mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.EVENT, null, null);
            mob.addCommandTag(mobTag);
            mob.setPersistent();
            if (mob instanceof NexusBoundMob boundMob) {
                boundMob.invasionmod$setNexus(nexus.getPos());
            }

            if (world.spawnNewEntityAndPassengers(mob)) {
                return true;
            }
        }

        return false;
    }

    private static final class PendingSpawn {
        private static final String NBT_ID = "Id";
        private static final String NBT_REMAINING = "Remaining";

        private final WaveEntry entry;
        private int remaining;

        private PendingSpawn(WaveEntry entry) {
            this(entry, entry.amount());
        }

        private PendingSpawn(WaveEntry entry, int remaining) {
            this.entry = Objects.requireNonNull(entry, "entry");
            this.remaining = MathHelper.clamp(remaining, 0, entry.amount());
        }

        private WaveEntry entry() {
            return entry;
        }

        private void decrement() {
            if (remaining > 0) {
                remaining--;
            }
        }

        private int remaining() {
            return remaining;
        }

        private NbtCompound toNbt() {
            NbtCompound data = new NbtCompound();
            data.putString(NBT_ID, entry.entityId().toString());
            data.putInt(NBT_REMAINING, remaining);
            return data;
        }
    }
}
