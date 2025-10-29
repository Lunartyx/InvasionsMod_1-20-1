package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The thrower reprises its ranged harassment role by reusing the vanilla
 * Pillager crossbow AI while remaining loyal to the Nexus.
 */
public class RiftThrowerEntity extends PillagerEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;

    public RiftThrowerEntity(EntityType<? extends PillagerEntity> entityType, World world) {
        super(entityType, world);
        this.setCanPickUpLoot(false);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(5, new MoveToNexusGoal(this, this, 1.0D));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty difficulty) {
        super.initEquipment(random, difficulty);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
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
