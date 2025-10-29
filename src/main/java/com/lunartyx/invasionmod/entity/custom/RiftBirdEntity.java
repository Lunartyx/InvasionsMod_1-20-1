package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.ai.MoveToNexusGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WatchClosestGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Lightweight Fabric recreation of the classic invasion bird. The original
 * Forge version shipped with bespoke animation controllers; for the Fabric port
 * we focus on behaviour: the bird hovers, circles the Nexus and darts towards
 * nearby players for melee strikes.
 */
public class RiftBirdEntity extends HostileEntity implements NexusBoundMob {
    @Nullable
    private BlockPos nexusPos;

    public RiftBirdEntity(EntityType<? extends RiftBirdEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.setNoGravity(true);
        this.experiencePoints = 5;
    }

    public static DefaultAttributeContainer.Builder createBirdAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 14.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D, 10.0F));
        this.goalSelector.add(6, new WatchClosestGoal(this, PlayerEntity.class, 12.0F));
        this.goalSelector.add(7, new MoveToNexusGoal(this, this, 1.1D));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanEnterOpenDoors(true);
        navigation.setCanPathThroughDoors(false);
        navigation.setCanSwim(false);
        return navigation;
    }

    @Override
    public boolean hasWings() {
        return true;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.95D).add(0.0D, this.getPreferredYVelocity(), 0.0D));
        }
    }

    private double getPreferredYVelocity() {
        double targetY = this.nexusPos != null ? this.nexusPos.getY() + 6.0D : this.getY();
        double diff = targetY - this.getY();
        return Math.max(-0.1D, Math.min(0.1D, diff * 0.05D));
    }

    @Override
    public void invasionmod$setNexus(BlockPos pos) {
        this.nexusPos = pos;
    }

    @Override
    public BlockPos invasionmod$getNexus() {
        return this.nexusPos;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
