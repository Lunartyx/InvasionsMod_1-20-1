package com.lunartyx.invasionmod.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Visual and audio lightning bolt used by invasion casters. The entity renders
 * client-side particles and deals a burst of damage via lightning sounds
 * without summoning actual lightning that might ignite builds.
 */
public class RiftBoltEntity extends Entity {
    private static final int DEFAULT_LIFESPAN = 20;
    private int age;
    private int lifespan = DEFAULT_LIFESPAN;

    public RiftBoltEntity(EntityType<? extends RiftBoltEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.isClient) {
            if (this.age == 0) {
                this.world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 2.0F, 0.9F + this.random.nextFloat() * 0.2F);
            }
            if (this.world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        }
        this.age++;
        if (this.age > this.lifespan) {
            discard();
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.age = nbt.getInt("Age");
        this.lifespan = nbt.getInt("Life");
        if (this.lifespan <= 0) {
            this.lifespan = DEFAULT_LIFESPAN;
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Age", this.age);
        nbt.putInt("Life", this.lifespan);
    }

    @Override
    public Vec3d getVelocity() {
        return Vec3d.ZERO;
    }

    @Override
    public EntitySpawnS2CPacket createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
