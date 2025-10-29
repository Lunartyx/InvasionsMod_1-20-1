package com.lunartyx.invasionmod.item.custom;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SearingBowItem extends BowItem {
    public SearingBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return;
        }

        boolean creativeOrInfinity = player.getAbilities().creativeMode
                || EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0;
        ItemStack arrowStack = player.getArrowType(stack);

        if (arrowStack.isEmpty() && !creativeOrInfinity) {
            return;
        }

        float charge = getRawPullProgress(this.getMaxUseTime(stack) - remainingUseTicks);
        if (charge < 0.1F) {
            return;
        }

        boolean special = charge >= 3.8F;
        float clampedCharge = Math.min(charge, 1.0F);

        if (arrowStack.isEmpty()) {
            arrowStack = new ItemStack(Items.ARROW);
        }

        if (!world.isClient) {
            ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
            PersistentProjectileEntity projectile = arrowItem.createArrow(world, arrowStack, player);
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, clampedCharge * 3.0F, 1.0F);

            if (clampedCharge == 1.0F && !special) {
                projectile.setCritical(true);
            }

            int power = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
            if (power > 0) {
                projectile.setDamage(projectile.getDamage() + (double) power * 0.5D + 0.5D);
            }

            int punch = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
            if (punch > 0) {
                projectile.setPunch(punch);
            }

            if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) {
                projectile.setOnFireFor(100);
            }

            if (special) {
                projectile.setOnFireFor(100);
                projectile.setDamage(projectile.getDamage() * 1.5D + 1.0D);
            }

            if (creativeOrInfinity && arrowStack.isOf(Items.ARROW)) {
                projectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }

            world.spawnEntity(projectile);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS, 1.0F, 1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + clampedCharge * 0.5F);

        boolean infinityArrow = creativeOrInfinity && arrowStack.isOf(Items.ARROW);
        if (!infinityArrow && !player.getAbilities().creativeMode) {
            arrowStack.decrement(1);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeOne(arrowStack);
            }
        }

        player.incrementStat(Stats.USED.getOrCreateStat(this));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getAbilities().creativeMode || !user.getArrowType(stack).isEmpty()) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        return TypedActionResult.fail(stack);
    }

    private float getRawPullProgress(int useTicks) {
        float f = (float) useTicks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return f;
    }
}
