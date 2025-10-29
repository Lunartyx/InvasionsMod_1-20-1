package com.lunartyx.invasionmod.block;

import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class NexusBlock extends BlockWithEntity {
    public static final BooleanProperty ACTIVE = Properties.LIT;
    private static final VoxelShape SHAPE = VoxelShapes.fullCube();

    public NexusBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(ACTIVE, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NexusBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isClient || !state.get(ACTIVE)) {
            return;
        }

        for (int i = 0; i < 6; i++) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D);
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D);
            world.addParticle(net.minecraft.particle.ParticleTypes.PORTAL, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.2D,
                    (random.nextDouble() - 0.5D) * 0.2D,
                    (random.nextDouble() - 0.5D) * 0.2D);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof NexusBlockEntity nexus)) {
            return ActionResult.PASS;
        }

        if (player.isSneaking() && player.getStackInHand(hand).isEmpty()) {
            if (nexus.tryGivePlayerItem(player)) {
                return ActionResult.CONSUME;
            }
        }

        if (nexus.tryAcceptItem(player, hand)) {
            return ActionResult.CONSUME;
        }

        player.openHandledScreen(nexus);
        nexus.notifyInteraction(player);
        return ActionResult.CONSUME;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (world.isClient) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NexusBlockEntity nexus) {
            boolean active = nexus.isActive();
            if (state.get(ACTIVE) != active) {
                world.setBlockState(pos, state.with(ACTIVE, active), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NexusBlockEntity nexus && nexus.isActive()) {
            return 0.0F;
        }

        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, net.minecraft.util.math.Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NexusBlockEntity nexus) {
            boolean active = nexus.isActive();
            if (state.get(ACTIVE) != active) {
                return state.with(ACTIVE, active);
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof NexusBlockEntity nexus) {
                ItemScatterer.spawn(world, pos, nexus);
                world.updateComparators(pos, this);
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NexusBlockEntity nexus) {
            return nexus.getComparatorOutput();
        }

        return 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.NEXUS, NexusBlockEntity::serverTick);
    }
}
