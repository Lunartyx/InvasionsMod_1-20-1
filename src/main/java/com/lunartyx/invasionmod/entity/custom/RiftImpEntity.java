package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Flying imp that harasses players from above while circling the Nexus.
 */
public class RiftImpEntity extends VexEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;

    public RiftImpEntity(EntityType<? extends VexEntity> entityType, World world) {
        super(entityType, world);
        this.setCanPickUpLoot(false);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.add(5, new MoveToNexusGoal(this, this, 1.2D));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
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
