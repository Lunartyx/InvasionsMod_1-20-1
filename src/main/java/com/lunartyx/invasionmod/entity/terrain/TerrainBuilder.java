package com.lunartyx.invasionmod.entity.terrain;

import com.lunartyx.invasionmod.entity.custom.RiftPigEngineerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Replicates the engineer's scaffold/ladder/bridge logic from the legacy mod
 * with Fabric-friendly block requests. The logic is intentionally conservative
 * but still builds multi-block structures so engineers can scale walls and fill
 * gaps around the Nexus.
 */
public class TerrainBuilder {
    private static final int BUILD_DELAY = 12;
    private static final int SUPPORT_DELAY = 8;

    private final RiftPigEngineerEntity engineer;
    private final TerrainModifier modifier;
    private int scanCooldown;

    public TerrainBuilder(RiftPigEngineerEntity engineer, TerrainModifier modifier) {
        this.engineer = engineer;
        this.modifier = modifier;
    }

    public void tick() {
        modifier.tick();
        if (scanCooldown > 0) {
            scanCooldown--;
        }
    }

    public boolean isIdle() {
        return modifier.isIdle();
    }

    public boolean planNextStep() {
        if (!(engineer.getWorld() instanceof ServerWorld world)) {
            return false;
        }
        if (!TerrainModifier.isMobGriefingEnabled(world)) {
            return false;
        }
        if (scanCooldown > 0) {
            return false;
        }

        BlockPos nexus = engineer.invasionmod$getNexus();
        if (nexus != null && engineer.squaredDistanceTo(nexus.getX() + 0.5D, nexus.getY() + 0.5D, nexus.getZ() + 0.5D) > 20 * 20) {
            return false;
        }

        BlockPos feet = engineer.getBlockPos();
        Direction facing = engineer.getHorizontalFacing();

        if (scheduleUnderfootSupport(world, feet)) {
            scanCooldown = 20;
            return true;
        }
        if (scheduleBridge(world, feet, facing)) {
            scanCooldown = 20;
            return true;
        }
        if (scheduleLadderTower(world, feet, facing)) {
            scanCooldown = 40;
            return true;
        }
        return false;
    }

    private boolean scheduleUnderfootSupport(ServerWorld world, BlockPos feet) {
        BlockPos below = feet.down();
        if (BlockStrengthHelper.isStructural(world.getBlockState(below), world, below)) {
            return false;
        }
        if (!world.getBlockState(below).isAir()) {
            return false;
        }
        List<ModifyBlockRequest> requests = new ArrayList<>();
        requests.add(new ModifyBlockRequest(below, Blocks.COBBLESTONE.getDefaultState(), ModifyBlockRequest.Action.PLACE, SUPPORT_DELAY));
        BlockPos deeper = below.down();
        if (world.getBlockState(deeper).isAir()) {
            requests.add(new ModifyBlockRequest(deeper, Blocks.COBBLESTONE.getDefaultState(), ModifyBlockRequest.Action.PLACE, SUPPORT_DELAY));
        }
        return modifier.submit(requests);
    }

    private boolean scheduleBridge(ServerWorld world, BlockPos feet, Direction facing) {
        BlockPos front = feet.offset(facing);
        BlockPos frontBelow = front.down();
        BlockPos frontTwoBelow = frontBelow.down();
        BlockState frontState = world.getBlockState(front);
        boolean gapAhead = frontState.isAir() || !BlockStrengthHelper.isStructural(frontState, world, front);
        if (!gapAhead) {
            return false;
        }
        BlockState belowState = world.getBlockState(frontBelow);
        if (!belowState.isAir() && BlockStrengthHelper.isStructural(belowState, world, frontBelow)) {
            return false;
        }
        List<ModifyBlockRequest> requests = new ArrayList<>();
        requests.add(new ModifyBlockRequest(frontBelow, Blocks.OAK_PLANKS.getDefaultState(), ModifyBlockRequest.Action.PLACE, BUILD_DELAY));
        if (world.getBlockState(frontTwoBelow).isAir()) {
            requests.add(new ModifyBlockRequest(frontTwoBelow, Blocks.COBBLESTONE.getDefaultState(), ModifyBlockRequest.Action.PLACE, BUILD_DELAY));
        }
        return modifier.submit(requests);
    }

    private boolean scheduleLadderTower(ServerWorld world, BlockPos feet, Direction facing) {
        BlockPos front = feet.offset(facing);
        if (!isSolid(world, front)) {
            return false;
        }
        int height = 0;
        while (height < 3 && isSolid(world, front.up(height))) {
            height++;
        }
        if (height == 0) {
            return false;
        }
        List<ModifyBlockRequest> requests = new ArrayList<>();
        BlockState support = Blocks.OAK_PLANKS.getDefaultState();
        BlockState ladder = Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, facing.getOpposite());
        for (int i = 0; i < height + 1; i++) {
            BlockPos supportPos = front.up(i);
            BlockPos ladderPos = feet.up(i);
            if (world.getBlockState(supportPos).isAir()) {
                requests.add(new ModifyBlockRequest(supportPos, support, ModifyBlockRequest.Action.PLACE, BUILD_DELAY));
            }
            if (world.getBlockState(ladderPos).isAir()) {
                requests.add(new ModifyBlockRequest(ladderPos, ladder, ModifyBlockRequest.Action.PLACE, BUILD_DELAY));
            }
        }
        BlockPos platform = front.up(height);
        BlockPos platformForward = platform.offset(facing);
        if (world.getBlockState(platformForward).isAir()) {
            requests.add(new ModifyBlockRequest(platformForward, Blocks.OAK_PLANKS.getDefaultState(), ModifyBlockRequest.Action.PLACE, BUILD_DELAY));
        }
        return modifier.submit(requests);
    }

    private boolean isSolid(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return BlockStrengthHelper.isStructural(state, world, pos);
    }
}
