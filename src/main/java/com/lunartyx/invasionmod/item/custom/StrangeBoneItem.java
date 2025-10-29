package com.lunartyx.invasionmod.item.custom;

import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.entity.custom.RiftWolfEntity;
import com.lunartyx.invasionmod.registry.ModBlocks;
import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class StrangeBoneItem extends Item {
    private static final int SEARCH_RADIUS = 7;
    private static final int SEARCH_HEIGHT = 4;

    public StrangeBoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof WolfEntity wolf) || entity.getWorld().isClient()) {
            return ActionResult.PASS;
        }

        if (!wolf.isTamed() || entity instanceof RiftWolfEntity) {
            return ActionResult.PASS;
        }

        ServerWorld world = (ServerWorld) entity.getWorld();
        BlockPos nexusPos = findNearbyNexus(world, wolf.getBlockPos());
        if (nexusPos == null) {
            user.sendMessage(Text.translatable("message.invasionmod.strange_bone.no_nexus"), true);
            return ActionResult.PASS;
        }

        RiftWolfEntity riftWolf = ModEntityTypes.RIFT_WOLF.create(world);
        if (riftWolf == null) {
            return ActionResult.FAIL;
        }

        riftWolf.refreshPositionAndAngles(wolf.getX(), wolf.getY(), wolf.getZ(), wolf.getYaw(), wolf.getPitch());
        riftWolf.setTamed(true);
        riftWolf.setOwnerUuid(user.getUuid());
        riftWolf.setHealth(riftWolf.getMaxHealth());
        riftWolf.invasionmod$setNexus(nexusPos);

        riftWolf.setCollarColor(wolf.getCollarColor());

        world.spawnEntity(riftWolf);
        wolf.discard();
        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        user.sendMessage(Text.translatable("message.invasionmod.strange_bone.success"), true);
        return ActionResult.SUCCESS;
    }

    @Nullable
    private BlockPos findNearbyNexus(ServerWorld world, BlockPos origin) {
        for (int y = -SEARCH_HEIGHT; y <= SEARCH_HEIGHT; y++) {
            for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos testPos = origin.add(x, y, z);
                    if (world.getBlockState(testPos).isOf(ModBlocks.NEXUS)) {
                        if (world.getBlockEntity(testPos) instanceof NexusBlockEntity) {
                            return testPos.toImmutable();
                        }
                    }
                }
            }
        }
        return null;
    }
}
