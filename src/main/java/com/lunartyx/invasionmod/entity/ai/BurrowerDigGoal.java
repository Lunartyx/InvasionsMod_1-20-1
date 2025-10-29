package com.lunartyx.invasionmod.entity.ai;

import com.lunartyx.invasionmod.entity.custom.RiftBurrowerEntity;
import com.lunartyx.invasionmod.entity.terrain.TerrainDigger;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Goal that coordinates the Rift Burrower's excavation helper so it can chew
 * through soft blocks blocking its path.
 */
public class BurrowerDigGoal extends Goal {
    private final RiftBurrowerEntity burrower;
    private final TerrainDigger digger;
    private int idleCooldown;

    public BurrowerDigGoal(RiftBurrowerEntity burrower, TerrainDigger digger) {
        this.burrower = burrower;
        this.digger = digger;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (burrower.getTarget() != null) {
            return false;
        }
        if (!digger.isIdle()) {
            return true;
        }
        if (idleCooldown > 0) {
            idleCooldown--;
            return false;
        }
        idleCooldown = 5;
        return digger.planObstructionWork();
    }

    @Override
    public boolean shouldContinue() {
        if (!digger.isIdle()) {
            return true;
        }
        return digger.planObstructionWork();
    }

    @Override
    public void start() {
        digger.planObstructionWork();
    }
}
