package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Replacement for the Invasion skeleton that keeps the wave pressure up using a
 * bow and arrow while still drifting toward the Nexus when idle.
 */
public class RiftSkeletonEntity extends SkeletonEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;

    public RiftSkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(4, new BowAttackGoal<>(this, 1.0D, 20, 15.0F));
        this.goalSelector.add(5, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.add(6, new MoveToNexusGoal(this, this, 1.0D));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty difficulty) {
        super.initEquipment(random, difficulty);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
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
