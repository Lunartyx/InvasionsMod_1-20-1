package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Allied wolf that defends the Nexus. The Fabric port keeps them permanently
 * tamed, directs them back to the structure when idle, and makes them favour
 * hostile mobs over players.
 */
public class RiftWolfEntity extends WolfEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;

    public RiftWolfEntity(EntityType<? extends WolfEntity> entityType, World world) {
        super(entityType, world);
        this.setTamed(true);
        this.setOwnerUuid(null);
        this.setCollarColor(DyeColor.CYAN);
        this.setPersistent();
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.add(5, new MoveToNexusGoal(this, this, 1.1D));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean success = super.tryAttack(target);
        if (success) {
            this.heal(2.0F);
        }
        return success;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }


    public boolean canBreed() {
        return false;
    }


    public boolean canMateWith(AnimalEntity other) {
        return false;
    }


    public boolean isAngryAt(PlayerEntity player) {
        return false;
    }

    @Override
    public void invasionmod$setNexus(BlockPos pos) {
        this.nexusPos = pos;
    }

    @Override
    public BlockPos invasionmod$getNexus() {
        return nexusPos;
    }
}
