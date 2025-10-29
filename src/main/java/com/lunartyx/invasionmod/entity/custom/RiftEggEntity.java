package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Spider egg dropped by the old Rift spider queen. The egg idles for a configurable
 * duration before hatching two spiders to ambush players.
 */
public class RiftEggEntity extends PassiveEntity {
    private static final int DEFAULT_HATCH_TIME = 20 * 15;
    private int hatchTime = DEFAULT_HATCH_TIME;
    private int ticks;
    private boolean hatched;

    public RiftEggEntity(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void initGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            return;
        }

        if (!hatched) {
            ticks++;
            if (ticks >= hatchTime) {
                hatch();
            }
        } else if (ticks >= hatchTime + 40) {
            discard();
        } else {
            ticks++;
        }
    }

    private void hatch() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        this.hatched = true;
        serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_FOX_EAT, SoundCategory.HOSTILE, 0.8F, 1.0F);
        serverWorld.spawnParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.25D, this.getZ(), 12, 0.2D, 0.2D, 0.2D, 0.01D);
        for (int i = 0; i < 2; i++) {
            MobEntity spider = ModEntityTypes.RIFT_SPIDER.create(serverWorld);
            if (spider == null) {
                continue;
            }
            spider.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
            spider.initialize(serverWorld, serverWorld.getLocalDifficulty(spider.getBlockPos()), SpawnReason.EVENT, null, null);
            spider.setPersistent();
            serverWorld.spawnEntityAndPassengers(spider);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.hatchTime = nbt.getInt("HatchTime");
        this.ticks = nbt.getInt("Ticks");
        this.hatched = nbt.getBoolean("Hatched");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("HatchTime", this.hatchTime);
        nbt.putInt("Ticks", this.ticks);
        nbt.putBoolean("Hatched", this.hatched);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld serverWorld, PassiveEntity mate) {
        return null;
    }

    @Override
    public EntitySpawnS2CPacket createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
