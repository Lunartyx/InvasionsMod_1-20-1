package com.lunartyx.invasionmod.entity.terrain;

import com.lunartyx.invasionmod.entity.custom.RiftBurrowerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the Rift Burrower's excavation requests by translating detected
 * obstructions into queued block removals processed via {@link TerrainModifier}.
 */
public class TerrainDigger {
    private final RiftBurrowerEntity burrower;
    private final TerrainModifier modifier;

    public TerrainDigger(RiftBurrowerEntity burrower, TerrainModifier modifier) {
        this.burrower = burrower;
        this.modifier = modifier;
    }

    public void tick() {
        modifier.tick();
    }

    public boolean isIdle() {
        return modifier.isIdle();
    }

    public boolean planObstructionWork() {
        if (!(burrower.getWorld() instanceof ServerWorld world)) {
            return false;
        }
        if (!TerrainModifier.isMobGriefingEnabled(world)) {
            return false;
        }

        BlockPos feet = burrower.getBlockPos();
        BlockPos nexus = burrower.invasionmod$getNexus();
        if (nexus != null && burrower.squaredDistanceTo(nexus.getX() + 0.5D, nexus.getY() + 0.5D, nexus.getZ() + 0.5D) > 20 * 20) {
            return false;
        }
        Direction facing = burrower.getHorizontalFacing();
        List<ModifyBlockRequest> requests = new ArrayList<>();

        addIfDiggable(world, requests, feet);
        addIfDiggable(world, requests, feet.up());

        BlockPos front = feet.offset(facing);
        addIfDiggable(world, requests, front);
        addIfDiggable(world, requests, front.up());
        addIfDiggable(world, requests, front.down());

        if (requests.isEmpty()) {
            return false;
        }
        return modifier.submit(requests);
    }

    private void addIfDiggable(ServerWorld world, List<ModifyBlockRequest> requests, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!BlockStrengthHelper.isDestructible(state, world, pos)) {
            return;
        }
        int delay = BlockStrengthHelper.getDigDelayTicks(state, world, pos);
        if (delay == Integer.MAX_VALUE) {
            return;
        }
        requests.add(new ModifyBlockRequest(pos, state, ModifyBlockRequest.Action.BREAK, delay));
    }
}
