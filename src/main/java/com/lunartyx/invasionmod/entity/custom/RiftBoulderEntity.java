package com.lunartyx.invasionmod.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/**
 * Simple thrown rock used by throwers. We re-use the vanilla gravel item model
 * for visuals while providing area damage on impact.
 */
public class RiftBoulderEntity extends ThrownItemEntity {
    public RiftBoulderEntity(EntityType<? extends RiftBoulderEntity> entityType, World world) {
        super(entityType, world);
    }

    public RiftBoulderEntity(EntityType<? extends RiftBoulderEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GRAVEL;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().damage(getDamageSource(), 8.0F);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.HOSTILE, 1.0F, 0.75F);
            if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.0D);
            }
            this.discard();
        }
    }

    private DamageSource getDamageSource() {
        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        return this.getDamageSources().thrown(this, owner);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.GRAVEL);
    }
}
