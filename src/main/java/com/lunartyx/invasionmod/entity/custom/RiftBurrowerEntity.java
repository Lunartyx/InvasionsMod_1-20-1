package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.BurrowerDigGoal;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import com.lunartyx.invasionmod.entity.terrain.TerrainDigger;
import com.lunartyx.invasionmod.entity.terrain.TerrainModifier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The burrower is represented by a beefed-up silverfish backed by a terrain
 * digger so it can clear soft blocks in a tunnel-sized column when pathing to
 * the Nexus.
 */
public class RiftBurrowerEntity extends SilverfishEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;
    private final TerrainModifier terrainModifier;
    private final TerrainDigger terrainDigger;

    public RiftBurrowerEntity(EntityType<? extends SilverfishEntity> entityType, World world) {
        super(entityType, world);
        this.setCanPickUpLoot(false);
        this.terrainModifier = new TerrainModifier(this);
        this.terrainDigger = new TerrainDigger(this, terrainModifier);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.add(5, new MoveToNexusGoal(this, this, 1.0D));
        this.goalSelector.add(6, new BurrowerDigGoal(this, terrainDigger));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            terrainDigger.tick();
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
