package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RiftZombiePiglinEntity extends ZombifiedPiglinEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;

    public RiftZombiePiglinEntity(EntityType<? extends ZombifiedPiglinEntity> entityType, World world) {
        super(entityType, world);
        this.setAngryAt(Util.NIL_UUID);
        this.setAngerTime(200);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.1D, false));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(5, new MoveToNexusGoal(this, this, 1.0D));
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!this.getWorld().isClient) {
            // permanent wütend halten
            this.setAngerTime(200);
        }
    }

    // isAngryAt(...) gibt es in 1.20.1 nicht als Override -> entfernen

    @Override
    public boolean shouldAngerAt(LivingEntity entity) {
        return true;
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
