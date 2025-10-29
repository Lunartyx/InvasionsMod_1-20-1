package com.lunartyx.invasionmod.item.custom;

import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.entity.NexusBoundMob;
import com.lunartyx.invasionmod.entity.custom.RiftWolfEntity;
import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DebugWandItem extends Item {
    private static final String NEXUS_KEY = "BoundNexus";

    public DebugWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            BlockPos bound = getBoundNexus(stack);
            if (bound != null) {
                user.sendMessage(Text.translatable("message.invasionmod.debug_wand.bound",
                        bound.getX(), bound.getY(), bound.getZ()), true);
            } else {
                user.sendMessage(Text.translatable("message.invasionmod.debug_wand.idle"), true);
            }
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (world.isClient || player == null) {
            return ActionResult.SUCCESS;
        }

        BlockPos pos = context.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NexusBlockEntity nexus) {
            bindNexus(stack, pos);
            nexus.debugStatus(player);
            player.sendMessage(Text.translatable("message.invasionmod.debug_wand.bound",
                    pos.getX(), pos.getY(), pos.getZ()), true);
            return ActionResult.CONSUME;
        }

        spawnTestMobs((ServerWorld) world, pos.up(), getBoundNexus(stack), player);
        player.sendMessage(Text.translatable("message.invasionmod.debug_wand.spawn"), true);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof PlayerEntity player)) {
            return false;
        }

        if (!attacker.getWorld().isClient && target instanceof WolfEntity wolf && !(target instanceof RiftWolfEntity)) {
            wolf.setTamed(true);
            wolf.setOwnerUuid(player.getUuid());
            wolf.setSitting(false);
            player.sendMessage(Text.translatable("message.invasionmod.debug_wand.tame"), true);
            return true;
        }
        return false;
    }

    private void bindNexus(ItemStack stack, BlockPos pos) {
        stack.getOrCreateNbt().put(NEXUS_KEY, NbtHelper.fromBlockPos(pos));
    }

    @Nullable
    private BlockPos getBoundNexus(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(NEXUS_KEY)) {
            return NbtHelper.toBlockPos(nbt.getCompound(NEXUS_KEY));
        }
        return null;
    }

    private void spawnTestMobs(ServerWorld world, BlockPos spawnPos, @Nullable BlockPos nexusPos, PlayerEntity summoner) {
        spawn(world, spawnPos, nexusPos, ModEntityTypes.RIFT_ZOMBIE);
        spawn(world, spawnPos, nexusPos, ModEntityTypes.RIFT_SPIDER);
        spawn(world, spawnPos, nexusPos, ModEntityTypes.RIFT_CREEPER);
        spawn(world, spawnPos, nexusPos, ModEntityTypes.RIFT_THROWER);
        spawn(world, spawnPos, nexusPos, ModEntityTypes.RIFT_PIG_ENGINEER);
        spawn(world, spawnPos, nexusPos, ModEntityTypes.RIFT_BURROWER);
        spawn(world, spawnPos, nexusPos, ModEntityTypes.RIFT_IMP);
        Entity wolf = ModEntityTypes.RIFT_WOLF.create(world);
        if (wolf instanceof RiftWolfEntity riftWolf) {
            if (nexusPos != null) {
                riftWolf.invasionmod$setNexus(nexusPos);
            }
            riftWolf.setOwnerUuid(summoner.getUuid());
            riftWolf.refreshPositionAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    world.random.nextFloat() * 360.0F, 0.0F);
            world.spawnEntity(riftWolf);
        }
    }

    private void spawn(ServerWorld world, BlockPos pos, @Nullable BlockPos nexusPos, EntityType<? extends Entity> type) {
        Entity entity = type.create(world);
        if (entity == null) {
            return;
        }
        entity.refreshPositionAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                world.random.nextFloat() * 360.0F, 0.0F);
        if (entity instanceof NexusBoundMob boundMob && nexusPos != null) {
            boundMob.invasionmod$setNexus(nexusPos);
        }
        world.spawnEntity(entity);
    }
}
