package com.lunartyx.invasionmod.entity.terrain;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a single block modification request that can be processed over
 * multiple ticks by {@link TerrainModifier}. Each request optionally carries a
 * predicate that must pass before the modification is executed to avoid
 * clobbering unexpected blocks.
 */
public class ModifyBlockRequest {
    public enum Action {
        PLACE,
        BREAK
    }

    private final BlockPos pos;
    private final BlockState targetState;
    private final Action action;
    private final int delayTicks;
    private final Predicate<BlockState> precondition;

    public ModifyBlockRequest(BlockPos pos, BlockState targetState, Action action, int delayTicks) {
        this(pos, targetState, action, delayTicks, state -> true);
    }

    public ModifyBlockRequest(BlockPos pos, BlockState targetState, Action action, int delayTicks,
                              Predicate<BlockState> precondition) {
        this.pos = Objects.requireNonNull(pos);
        this.targetState = targetState;
        this.action = Objects.requireNonNull(action);
        this.delayTicks = Math.max(delayTicks, 1);
        this.precondition = precondition == null ? state -> true : precondition;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public boolean canExecute(ServerWorld world) {
        return precondition.test(world.getBlockState(pos));
    }

    public boolean execute(ServerWorld world, LivingEntity actor, boolean dropItems) {
        return switch (action) {
            case PLACE -> place(world);
            case BREAK -> breakBlock(world, actor, dropItems);
        };
    }

    private boolean place(ServerWorld world) {
        if (targetState == null) {
            return false;
        }
        BlockState current = world.getBlockState(pos);
        if (!current.isAir() && !current.getCollisionShape(world, pos).isEmpty()) {
            return false;
        }
        return world.setBlockState(pos, targetState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
    }

    private boolean breakBlock(ServerWorld world, LivingEntity actor, boolean dropItems) {
        BlockState current = world.getBlockState(pos);
        if (current.isAir()) {
            return true;
        }
        return world.breakBlock(pos, dropItems, actor);
    }
}
