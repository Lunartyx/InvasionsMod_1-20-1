package com.lunartyx.invasionmod.entity.ai;

import com.lunartyx.invasionmod.entity.NexusBoundMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/**
 * Basic goal that keeps a mob travelling toward the Nexus when it does not
 * currently have a combat target. The legacy mob AI constantly regrouped around
 * the structure so new waves did not leave stragglers; this goal reproduces that
 * behaviour in a Fabric-friendly manner.
 */
public class MoveToNexusGoal extends Goal {
    private final PathAwareEntity mob;
    private final NexusBoundMob nexusMob;
    private final double speed;
    private int cooldown;

    public MoveToNexusGoal(PathAwareEntity mob, NexusBoundMob nexusMob, double speed) {
        this.mob = mob;
        this.nexusMob = nexusMob;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        BlockPos nexus = nexusMob.invasionmod$getNexus();
        return nexus != null
                && mob.getTarget() == null
                && mob.squaredDistanceTo(nexus.getX() + 0.5D, nexus.getY() + 0.5D, nexus.getZ() + 0.5D) > 9.0D;
    }

    @Override
    public boolean shouldContinue() {
        BlockPos nexus = nexusMob.invasionmod$getNexus();
        return nexus != null
                && !mob.getNavigation().isIdle()
                && mob.getTarget() == null
                && mob.squaredDistanceTo(nexus.getX() + 0.5D, nexus.getY() + 0.5D, nexus.getZ() + 0.5D) > 4.0D;
    }

    @Override
    public void start() {
        this.cooldown = 0;
        this.tryMove();
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (cooldown-- <= 0) {
            cooldown = 20;
            tryMove();
        }
    }

    private void tryMove() {
        BlockPos nexus = nexusMob.invasionmod$getNexus();
        if (nexus == null) {
            return;
        }
        mob.getNavigation().startMovingTo(nexus.getX() + 0.5D, nexus.getY(), nexus.getZ() + 0.5D, speed);
    }
}
