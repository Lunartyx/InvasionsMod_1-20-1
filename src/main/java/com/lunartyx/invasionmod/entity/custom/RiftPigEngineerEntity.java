package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.EngineerBuildGoal;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import com.lunartyx.invasionmod.entity.terrain.TerrainBuilder;
import com.lunartyx.invasionmod.entity.terrain.TerrainModifier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Pig engineer that coordinates with a terrain builder to raise ladders,
 * scaffolding, and bridges near the Nexus so invasion mobs can keep pressure on
 * defenders.
 */
public class RiftPigEngineerEntity extends ZombifiedPiglinEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;
    private final TerrainModifier terrainModifier;
    private final TerrainBuilder terrainBuilder;

    public RiftPigEngineerEntity(EntityType<? extends ZombifiedPiglinEntity> entityType, World world) {
        super(entityType, world);
        this.setCanPickUpLoot(false);
        this.terrainModifier = new TerrainModifier(this);
        this.terrainBuilder = new TerrainBuilder(this, terrainModifier);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.1D, false));
        this.goalSelector.add(5, new MoveToNexusGoal(this, this, 1.0D));
        this.goalSelector.add(6, new EngineerBuildGoal(this, terrainBuilder));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            terrainBuilder.tick();
        }
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
