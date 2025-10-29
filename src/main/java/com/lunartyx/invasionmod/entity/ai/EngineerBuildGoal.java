package com.lunartyx.invasionmod.entity.ai;

import com.lunartyx.invasionmod.entity.custom.RiftPigEngineerEntity;
import com.lunartyx.invasionmod.entity.terrain.TerrainBuilder;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Goal that periodically asks the engineer's {@link TerrainBuilder} to plan
 * scaffolding, ladders, or bridges when pathing towards the Nexus.
 */
public class EngineerBuildGoal extends Goal {
    private final RiftPigEngineerEntity engineer;
    private final TerrainBuilder builder;

    public EngineerBuildGoal(RiftPigEngineerEntity engineer, TerrainBuilder builder) {
        this.engineer = engineer;
        this.builder = builder;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (engineer.getTarget() != null) {
            return false;
        }
        if (!builder.isIdle()) {
            return true;
        }
        return builder.planNextStep();
    }

    @Override
    public boolean shouldContinue() {
        if (!builder.isIdle()) {
            return true;
        }
        return builder.planNextStep();
    }

    @Override
    public void tick() {
        builder.planNextStep();
    }
}
