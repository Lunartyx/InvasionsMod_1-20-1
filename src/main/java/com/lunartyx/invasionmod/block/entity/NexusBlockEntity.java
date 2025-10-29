package com.lunartyx.invasionmod.block.entity;

import com.lunartyx.invasionmod.block.NexusBlock;
import com.lunartyx.invasionmod.command.NexusCommandTracker;
import com.lunartyx.invasionmod.item.custom.TrapItem;
import com.lunartyx.invasionmod.network.InvasionNetwork;
import com.lunartyx.invasionmod.nexus.InvasionManager;
import com.lunartyx.invasionmod.registry.ModBlockEntities;
import com.lunartyx.invasionmod.registry.ModItems;
import com.lunartyx.invasionmod.registry.ModSoundEvents;
import com.lunartyx.invasionmod.screen.NexusScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NexusBlockEntity extends BlockEntity implements Inventory, ExtendedScreenHandlerFactory {
    public static final int MIN_SPAWN_RADIUS = 32;
    public static final int MAX_SPAWN_RADIUS = 128;
    public static final int RADIUS_STEP = 8;
    private static final int INVENTORY_SIZE = 2;
    public static final int ACTIVATION_TIME = 400;
    public static final int FLUX_PER_CRYSTAL = 3000;
    public static final int MAX_COOK_TIME = 1200;
    public static final int MAX_HEALTH = 100;
    public static final int PROPERTY_COUNT = 12;
    private static final int POWER_TICK_INTERVAL = 2200;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> activationTicks;
                case 1 -> mode.ordinal();
                case 2 -> currentWave;
                case 3 -> nexusLevel;
                case 4 -> spawnRadius;
                case 5 -> fluxProgress;
                case 6 -> cookTime;
                case 7 -> health;
                case 8 -> mobsRemaining;
                case 9 -> activeMobs;
                case 10 -> waveCooldown;
                case 11 -> powerLevel;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> activationTicks = value;
                case 1 -> mode = Mode.byIndex(value);
                case 2 -> currentWave = value;
                case 3 -> nexusLevel = value;
                case 4 -> spawnRadius = value;
                case 5 -> fluxProgress = value;
                case 6 -> cookTime = value;
                case 7 -> health = value;
                case 8 -> mobsRemaining = value;
                case 9 -> activeMobs = value;
                case 10 -> waveCooldown = value;
                case 11 -> powerLevel = value;
                default -> {
                }
            }
        }

        @Override
        public int size() {
            return PROPERTY_COUNT;
        }
    };

    private int spawnRadius = 64;
    private Mode mode = Mode.DORMANT;
    private ActivationType pendingActivation;
    private int activationTicks;
    private int nexusLevel = 1;
    private int currentWave;
    private int cookTime;
    private int fluxProgress;
    private int health = MAX_HEALTH;
    private boolean active;
    private int mobsRemaining;
    private int activeMobs;
    private int waveCooldown;
    private int powerLevel;
    private int powerLevelTimer;
    private boolean continuousWaveActive;

    private final InvasionManager invasionManager;

    public NexusBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEXUS, pos, state);
        this.invasionManager = new InvasionManager(this);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
            world.updateComparators(pos, getCachedState().getBlock());
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, NexusBlockEntity nexus) {
        if (world.isClient) {
            return;
        }

        boolean dirty = false;
        dirty |= nexus.processActivation();
        dirty |= nexus.processContinuousPower();
        dirty |= nexus.processFlux();
        dirty |= nexus.processCrafting();
        dirty |= nexus.invasionManager.tick(world);

        if (dirty) {
            nexus.markDirty();
        }
    }

    public int getSpawnRadius() {
        return spawnRadius;
    }

    public void setSpawnRadius(int spawnRadius) {
        int clamped = Math.min(MAX_SPAWN_RADIUS, Math.max(MIN_SPAWN_RADIUS, spawnRadius));
        if (clamped != this.spawnRadius) {
            this.spawnRadius = clamped;
            markDirty();
        }
    }

    public int cycleSpawnRadius(boolean decrease) {
        int newRadius = spawnRadius + (decrease ? -RADIUS_STEP : RADIUS_STEP);
        if (newRadius < MIN_SPAWN_RADIUS) {
            newRadius = MAX_SPAWN_RADIUS;
        } else if (newRadius > MAX_SPAWN_RADIUS) {
            newRadius = MIN_SPAWN_RADIUS;
        }
        setSpawnRadius(newRadius);
        return spawnRadius;
    }

    public boolean isActive() {
        return active;
    }

    private void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        if (world != null && !world.isClient) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof NexusBlock) {
                world.setBlockState(pos, state.with(NexusBlock.ACTIVE, active), Block.NOTIFY_LISTENERS);
            }
        }
    }

    public Mode getMode() {
        return mode;
    }

    private void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }
        boolean wasContinuous = this.mode == Mode.CONTINUOUS;
        this.mode = mode;
        if (wasContinuous && mode != Mode.CONTINUOUS) {
            continuousWaveActive = false;
            powerLevelTimer = 0;
        }
        setActive(mode.isActive());
        markDirty();
    }

    public int getActivationTicks() {
        return activationTicks;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getFluxProgress() {
        return fluxProgress;
    }

    public int getHealth() {
        return health;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getNexusLevel() {
        return nexusLevel;
    }

    public boolean tryAcceptItem(PlayerEntity player, Hand hand) {
        ItemStack heldStack = player.getStackInHand(hand);
        if (heldStack.isEmpty() || !isValidInput(heldStack)) {
            return false;
        }

        ItemStack remainder = insertIntoSlot(heldStack, 0);
        if (remainder.getCount() != heldStack.getCount()) {
            player.setStackInHand(hand, remainder);
            return true;
        }
        return false;
    }

    public boolean tryGivePlayerItem(PlayerEntity player) {
        ItemStack extracted = removeStack(1);
        if (extracted.isEmpty()) {
            extracted = removeStack(0);
        }

        if (extracted.isEmpty()) {
            return false;
        }

        if (!player.giveItemStack(extracted)) {
            player.dropItem(extracted, false);
        }
        return true;
    }

    public int getComparatorOutput() {
        return ScreenHandler.calculateComparatorOutput(this);
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.invasionmod.nexus");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new NexusScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    public void notifyInteraction(@Nullable PlayerEntity player) {
        if (player == null || world == null || world.isClient) {
            return;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            NexusCommandTracker.setFocus(serverPlayer, pos);
        }

        MutableText modeText = Text.translatable("message.invasionmod.nexus.mode." + mode.translationKey);
        MutableText activationText;
        if (pendingActivation != null) {
            activationText = Text.translatable("message.invasionmod.nexus.activation_progress", activationTicks, ACTIVATION_TIME);
        } else {
            activationText = Text.translatable("message.invasionmod.nexus.activation_idle");
        }

        player.sendMessage(Text.translatable("message.invasionmod.nexus_status",
                modeText,
                health,
                MAX_HEALTH,
                activationText,
                spawnRadius,
                currentWave,
                mobsRemaining,
                activeMobs,
                waveCooldown),
                false);
    }

    public void debugStatus(PlayerEntity player) {
        if (player == null || world == null || world.isClient) {
            return;
        }

        player.sendMessage(Text.translatable("message.invasionmod.nexus_debug.mode", modeText()), false);
        player.sendMessage(Text.translatable("message.invasionmod.nexus_debug.wave", currentWave, nexusLevel), false);
        player.sendMessage(Text.translatable("message.invasionmod.nexus_debug.health", health, MAX_HEALTH), false);
        player.sendMessage(Text.translatable("message.invasionmod.nexus_debug.progress", activationTicks, cookTime, fluxProgress), false);
        player.sendMessage(Text.translatable("message.invasionmod.nexus_debug.mobs",
                mobsRemaining,
                activeMobs,
                invasionManager.getPendingSpawnCount(),
                waveCooldown), false);
        player.sendMessage(Text.translatable("message.invasionmod.nexus_debug.queue",
                invasionManager.getCurrentWaveTarget(),
                invasionManager.isRunning(),
                invasionManager.isContinuous()), false);
        ItemStack input = inventory.get(0);
        boolean damping = !input.isEmpty() && input.isOf(ModItems.DAMPING_AGENT);
        boolean strongDamping = !input.isEmpty() && input.isOf(ModItems.STRONG_DAMPING_AGENT);
        player.sendMessage(Text.translatable("message.invasionmod.nexus_debug.power",
                powerLevel,
                powerLevelTimer,
                continuousWaveActive,
                damping,
                strongDamping), false);
    }

    public void onWaveStarted(int waveNumber, int totalMobs) {
        currentWave = waveNumber;
        nexusLevel = Math.max(nexusLevel, waveNumber);
        mobsRemaining = totalMobs;
        activeMobs = 0;
        waveCooldown = 0;
        if (mode == Mode.CONTINUOUS) {
            continuousWaveActive = true;
        }
        markDirty();
        broadcast(Text.translatable("message.invasionmod.nexus_wave_start", waveNumber, totalMobs));
        playSoundForNexus(ModSoundEvents.RUMBLE_1, SoundCategory.HOSTILE, 1.0F, 1.0F);
    }

    public void onWaveCleared(int waveNumber, int cooldownTicks) {
        waveCooldown = cooldownTicks;
        if (mode == Mode.CONTINUOUS) {
            continuousWaveActive = false;
        }
        markDirty();
        broadcast(Text.translatable("message.invasionmod.nexus_wave_cleared",
                waveNumber,
                cooldownTicks,
                String.format(java.util.Locale.ROOT, "%.1f", cooldownTicks / 20.0F)));
        playSoundForNexus(ModSoundEvents.CHIME_1, SoundCategory.HOSTILE, 1.0F, 1.0F);
    }

    public void updateMobCounts(int pendingSpawns, int currentActive) {
        int total = Math.max(0, pendingSpawns) + Math.max(0, currentActive);
        boolean changed = false;
        if (mobsRemaining != total) {
            mobsRemaining = total;
            changed = true;
        }
        if (activeMobs != currentActive) {
            activeMobs = currentActive;
            changed = true;
        }
        if (changed) {
            markDirty();
        }
    }

    public void setWaveCooldown(int ticks) {
        if (waveCooldown != ticks) {
            waveCooldown = ticks;
            markDirty();
        }
    }

    public void onInvasionStopped(boolean announce) {
        mobsRemaining = 0;
        activeMobs = 0;
        waveCooldown = 0;
        currentWave = 0;
        continuousWaveActive = false;
        markDirty();
        if (announce) {
            broadcast(Text.translatable("message.invasionmod.nexus_invasion_stopped"));
        }
    }

    public int getMobsRemaining() {
        return mobsRemaining;
    }

    public int getActiveMobs() {
        return activeMobs;
    }

    public int getWaveCooldown() {
        return waveCooldown;
    }

    public InvasionManager getInvasionManager() {
        return invasionManager;
    }

    private Text modeText() {
        return Text.translatable("message.invasionmod.nexus.mode." + mode.getTranslationKey());
    }

    public void attack(int damage) {
        if (world == null || world.isClient || damage <= 0) {
            return;
        }

        int previous = health;
        health = Math.max(0, health - damage);
        if (health != previous) {
            markDirty();
        }
        if (health == 0) {
            stop();
            playSoundForNexus(SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 1.0F);
            broadcast(Text.translatable("message.invasionmod.nexus_destroyed"));
        }
    }

    public void restore(int amount) {
        int previous = health;
        health = Math.min(MAX_HEALTH, health + amount);
        if (health != previous) {
            markDirty();
        }
    }

    private boolean processActivation() {
        ItemStack catalyst = inventory.get(0);
        ActivationType type = ActivationType.fromStack(catalyst);
        Mode previousMode = mode;
        ActivationType previousActivation = pendingActivation;
        int previousTicks = activationTicks;

        if (type == null || !mode.canActivate(type)) {
            activationTicks = 0;
            pendingActivation = null;
            if (mode == Mode.ACTIVATING) {
                setMode(Mode.DORMANT);
            }
            return previousTicks != activationTicks || previousActivation != pendingActivation || previousMode != mode;
        }

        if (pendingActivation != type) {
            pendingActivation = type;
            activationTicks = 0;
        }

        setMode(Mode.ACTIVATING);
        activationTicks += 1;
        if (activationTicks >= ACTIVATION_TIME && world != null && !world.isClient) {
            completeActivation(type);
            return true;
        }
        return previousTicks != activationTicks || previousActivation != pendingActivation || previousMode != mode;
    }

    private void completeActivation(ActivationType type) {
        consumeInput(0, 1);
        activationTicks = 0;
        pendingActivation = null;
        health = MAX_HEALTH;
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            switch (type) {
                case BASIC -> {
                    currentWave = 1;
                    nexusLevel = Math.max(nexusLevel, currentWave);
                    setMode(Mode.INVASION);
                    broadcast(Text.translatable("message.invasionmod.nexus_activation_invasion"));
                    playSoundForNexus(ModSoundEvents.RUMBLE_1, SoundCategory.HOSTILE, 1.0F, 1.0F);
                    invasionManager.startInvasion(serverWorld, currentWave);
                }
                case STRONG -> {
                    currentWave = 10;
                    nexusLevel = Math.max(nexusLevel, currentWave);
                    setMode(Mode.INVASION);
                    broadcast(Text.translatable("message.invasionmod.nexus_activation_strong"));
                    playSoundForNexus(ModSoundEvents.RUMBLE_1, SoundCategory.HOSTILE, 1.0F, 1.0F);
                    invasionManager.startInvasion(serverWorld, currentWave);
                }
                case STABLE -> {
                    currentWave = 1;
                    setMode(Mode.CONTINUOUS);
                    broadcast(Text.translatable("message.invasionmod.nexus_activation_stable"));
                    playSoundForNexus(ModSoundEvents.RUMBLE_1, SoundCategory.HOSTILE, 1.0F, 1.0F);
                    powerLevel = 0;
                    powerLevelTimer = 0;
                    continuousWaveActive = false;
                    invasionManager.startContinuous(serverWorld, currentWave);
                }
            }
        }
    }

    private boolean processContinuousPower() {
        if (mode != Mode.CONTINUOUS) {
            if (powerLevelTimer != 0) {
                powerLevelTimer = 0;
                return true;
            }
            return false;
        }

        boolean dirty = false;
        powerLevelTimer += 1;
        while (powerLevelTimer >= POWER_TICK_INTERVAL) {
            powerLevelTimer -= POWER_TICK_INTERVAL;
            if (!hasDampingAgent()) {
                int previous = powerLevel;
                powerLevel = Math.min(Integer.MAX_VALUE, powerLevel + 1);
                if (powerLevel != previous) {
                    dirty = true;
                }
            }
        }

        if (hasStrongDampingAgent() && !continuousWaveActive) {
            if (powerLevel >= 0) {
                powerLevel -= 1;
                dirty = true;
                if (powerLevel < 0) {
                    powerLevel = 0;
                    stop(false);
                    return true;
                }
            }
        }

        return dirty;
    }

    private boolean processFlux() {
        if (!mode.isActive()) {
            return false;
        }

        int previousFlux = fluxProgress;
        fluxProgress = Math.min(FLUX_PER_CRYSTAL, fluxProgress + 5);

        if (fluxProgress < FLUX_PER_CRYSTAL) {
            return previousFlux != fluxProgress;
        }

        ItemStack output = new ItemStack(ModItems.RIFT_FLUX);
        if (tryInsertOutput(output)) {
            fluxProgress = Math.max(0, fluxProgress - FLUX_PER_CRYSTAL);
            return true;
        }

        return previousFlux != fluxProgress;
    }

    private boolean processCrafting() {
        int previousCook = cookTime;
        ItemStack input = inventory.get(0);
        if (input.isEmpty()) {
            if (cookTime != 0) {
                cookTime = 0;
                return true;
            }
            return false;
        }

        if (input.isOf(ModItems.IM_TRAP)) {
            TrapItem.TrapType trapType = TrapItem.getTrapType(input);
            if (trapType == TrapItem.TrapType.EMPTY) {
                int increment = mode.isActive() ? 9 : 1;
                cookTime = Math.min(MAX_COOK_TIME, cookTime + increment);
                if (cookTime >= MAX_COOK_TIME) {
                    ItemStack output = new ItemStack(ModItems.IM_TRAP);
                    TrapItem.setTrapType(output, TrapItem.TrapType.RIFT);
                    if (tryInsertOutput(output)) {
                        consumeInput(0, 1);
                        cookTime = 0;
                        return true;
                    }
                }
                return previousCook != cookTime;
            }

            if (cookTime != 0) {
                cookTime = 0;
                return true;
            }
            return false;
        }

        if (input.isOf(ModItems.RIFT_FLUX) && nexusLevel >= 10) {
            int increment = mode.isActive() ? 9 : 5;
            cookTime = Math.min(MAX_COOK_TIME, cookTime + increment);
            if (cookTime >= MAX_COOK_TIME) {
                if (tryInsertOutput(new ItemStack(ModItems.STRONG_CATALYST))) {
                    consumeInput(0, 1);
                    cookTime = 0;
                    return true;
                }
            }
            return previousCook != cookTime;
        }

        if (cookTime != 0) {
            cookTime = 0;
            return true;
        }
        return previousCook != cookTime;
    }

    private boolean tryInsertOutput(ItemStack stack) {
        ItemStack current = inventory.get(1);
        if (current.isEmpty()) {
            inventory.set(1, stack.copy());
            markDirty();
            return true;
        }

        if (!ItemStack.canCombine(current, stack)) {
            return false;
        }

        int transferable = Math.min(stack.getCount(), getMaxCountPerStack() - current.getCount());
        if (transferable <= 0) {
            return false;
        }

        current.increment(transferable);
        markDirty();
        return true;
    }

    private void consumeInput(int slot, int count) {
        ItemStack input = inventory.get(slot);
        if (input.isEmpty()) {
            return;
        }

        input.decrement(count);
        if (input.isEmpty()) {
            inventory.set(slot, ItemStack.EMPTY);
        }
        markDirty();
    }

    public boolean isValidInput(ItemStack stack) {
        return stack.isOf(ModItems.NEXUS_CATALYST)
                || stack.isOf(ModItems.STRONG_CATALYST)
                || stack.isOf(ModItems.STABLE_NEXUS_CATALYST)
                || stack.isOf(ModItems.RIFT_FLUX)
                || stack.isOf(ModItems.CATALYST_MIXTURE)
                || stack.isOf(ModItems.STABLE_CATALYST_MIXTURE)
                || stack.isOf(ModItems.DAMPING_AGENT)
                || stack.isOf(ModItems.STRONG_DAMPING_AGENT)
                || (stack.isOf(ModItems.IM_TRAP) && TrapItem.getTrapType(stack) == TrapItem.TrapType.EMPTY);
    }

    private ItemStack insertIntoSlot(ItemStack stack, int slot) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = inventory.get(slot);
        int maxCount = Math.min(stack.getMaxCount(), getMaxCountPerStack());
        if (current.isEmpty()) {
            int toMove = Math.min(stack.getCount(), maxCount);
            ItemStack inserted = stack.copy();
            inserted.setCount(toMove);
            inventory.set(slot, inserted);
            markDirty();
            if (stack.getCount() == toMove) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.decrement(toMove);
            return remainder;
        }

        if (!ItemStack.canCombine(current, stack)) {
            return stack;
        }

        int toMove = Math.min(stack.getCount(), maxCount - current.getCount());
        if (toMove <= 0) {
            return stack;
        }

        current.increment(toMove);
        markDirty();
        if (stack.getCount() == toMove) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        remainder.decrement(toMove);
        return remainder;
    }

    private void stop() {
        stop(false);
    }

    private void stop(boolean announce) {
        setMode(Mode.DORMANT);
        activationTicks = 0;
        pendingActivation = null;
        fluxProgress = 0;
        continuousWaveActive = false;
        invasionManager.stop(announce);
    }

    public boolean commandStart(ServerWorld world, int wave, boolean continuous) {
        if (world == null || world.isClient) {
            return false;
        }
        if (!(this.world instanceof ServerWorld serverWorld) || serverWorld != world) {
            if (this.world instanceof ServerWorld actualWorld) {
                world = actualWorld;
            } else {
                return false;
            }
        }

        if (mode == Mode.ACTIVATING) {
            return false;
        }

        if (invasionManager.isRunning()) {
            invasionManager.stop(false);
        }

        int targetWave = Math.max(1, wave);
        activationTicks = 0;
        pendingActivation = null;
        fluxProgress = 0;
        mobsRemaining = 0;
        activeMobs = 0;
        waveCooldown = 0;
        health = MAX_HEALTH;

        if (continuous) {
            setMode(Mode.CONTINUOUS);
            currentWave = targetWave;
            invasionManager.startContinuous(world, targetWave);
        } else {
            setMode(Mode.INVASION);
            currentWave = targetWave;
            invasionManager.startInvasion(world, targetWave);
        }

        nexusLevel = Math.max(nexusLevel, targetWave);
        markDirty();
        return true;
    }

    public boolean commandStop(boolean announce) {
        if (world == null || world.isClient) {
            return false;
        }

        boolean wasRunning = invasionManager.isRunning() || mode == Mode.ACTIVATING || mode.isActive();
        if (!wasRunning) {
            return false;
        }

        stop(announce);
        return true;
    }

    public void broadcast(Text message) {
        if (world == null || world.isClient) {
            return;
        }

        for (PlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= (double) (spawnRadius * spawnRadius)) {
                player.sendMessage(message, false);
            }
        }
    }

    private void playSoundForNexus(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (!(world instanceof ServerWorld serverWorld) || sound == null) {
            return;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double radiusSq = (double) spawnRadius * (double) spawnRadius;
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (player.squaredDistanceTo(centerX, centerY, centerZ) <= radiusSq) {
                InvasionNetwork.sendNexusSound(player, pos, sound, category, volume, pitch);
            }
        }
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = Inventories.splitStack(inventory, slot, amount);
        if (!stack.isEmpty()) {
            markDirty();
        }
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = Inventories.removeStack(inventory, slot);
        if (!stack.isEmpty()) {
            markDirty();
        }
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null) {
            return false;
        }
        return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0D;
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("SpawnRadius", spawnRadius);
        nbt.putString("Mode", mode.name());
        nbt.putInt("Activation", activationTicks);
        nbt.putInt("NexusLevel", nexusLevel);
        nbt.putInt("CurrentWave", currentWave);
        nbt.putInt("CookTime", cookTime);
        nbt.putInt("Flux", fluxProgress);
        nbt.putInt("Health", health);
        nbt.putBoolean("Active", active);
        nbt.putInt("MobsRemaining", mobsRemaining);
        nbt.putInt("ActiveMobs", activeMobs);
        nbt.putInt("WaveCooldown", waveCooldown);
        nbt.putInt("PowerLevel", powerLevel);
        nbt.putInt("PowerTimer", powerLevelTimer);
        nbt.putBoolean("ContinuousWaveActive", continuousWaveActive);
        if (pendingActivation != null) {
            nbt.putString("PendingActivation", pendingActivation.name());
        }
        Inventories.writeNbt(nbt, inventory);
        NbtCompound invasionData = new NbtCompound();
        invasionManager.writeNbt(invasionData);
        nbt.put("Invasion", invasionData);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        spawnRadius = nbt.getInt("SpawnRadius");
        String modeName = nbt.getString("Mode");
        if (!modeName.isEmpty()) {
            try {
                mode = Mode.valueOf(modeName);
            } catch (IllegalArgumentException ignored) {
                mode = Mode.DORMANT;
            }
        } else {
            mode = Mode.DORMANT;
        }
        activationTicks = nbt.getInt("Activation");
        nexusLevel = nbt.getInt("NexusLevel");
        currentWave = nbt.getInt("CurrentWave");
        cookTime = nbt.getInt("CookTime");
        fluxProgress = nbt.getInt("Flux");
        health = nbt.getInt("Health");
        active = nbt.getBoolean("Active");
        mobsRemaining = nbt.getInt("MobsRemaining");
        activeMobs = nbt.getInt("ActiveMobs");
        waveCooldown = nbt.getInt("WaveCooldown");
        powerLevel = nbt.getInt("PowerLevel");
        powerLevelTimer = nbt.getInt("PowerTimer");
        continuousWaveActive = nbt.getBoolean("ContinuousWaveActive");
        if (nbt.contains("PendingActivation")) {
            String activationName = nbt.getString("PendingActivation");
            try {
                pendingActivation = ActivationType.valueOf(activationName);
            } catch (IllegalArgumentException ignored) {
                pendingActivation = null;
            }
        } else {
            pendingActivation = null;
        }
        Inventories.readNbt(nbt, inventory);
        setActive(active);
        if (nbt.contains("Invasion")) {
            invasionManager.readNbt(nbt.getCompound("Invasion"));
        } else {
            invasionManager.stop(false);
        }
    }

    private boolean hasDampingAgent() {
        ItemStack stack = inventory.get(0);
        return !stack.isEmpty() && stack.isOf(ModItems.DAMPING_AGENT);
    }

    private boolean hasStrongDampingAgent() {
        ItemStack stack = inventory.get(0);
        return !stack.isEmpty() && stack.isOf(ModItems.STRONG_DAMPING_AGENT);
    }

    @Override
    public int getMaxCountPerStack() {
        return 64;
    }

    public enum Mode {
        DORMANT(false, "dormant"),
        ACTIVATING(false, "activating"),
        INVASION(true, "invasion"),
        CONTINUOUS(true, "continuous");

        private final boolean active;
        private final String translationKey;

        Mode(boolean active, String translationKey) {
            this.active = active;
            this.translationKey = translationKey;
        }

        public boolean isActive() {
            return active;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public boolean canActivate(ActivationType type) {
            if (this == CONTINUOUS && type != ActivationType.STABLE) {
                return false;
            }
            return this == DORMANT || this == CONTINUOUS || this == ACTIVATING;
        }

        public static Mode byIndex(int index) {
            Mode[] values = values();
            if (index < 0 || index >= values.length) {
                return DORMANT;
            }
            return values[index];
        }
    }

    private enum ActivationType {
        BASIC,
        STRONG,
        STABLE;

        @Nullable
        public static ActivationType fromStack(ItemStack stack) {
            if (stack.isEmpty()) {
                return null;
            }
            if (stack.isOf(ModItems.NEXUS_CATALYST)) {
                return BASIC;
            }
            if (stack.isOf(ModItems.STRONG_CATALYST)) {
                return STRONG;
            }
            if (stack.isOf(ModItems.STABLE_NEXUS_CATALYST)) {
                return STABLE;
            }
            return null;
        }
    }
}
