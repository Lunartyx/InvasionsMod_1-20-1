package com.lunartyx.invasionmod.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

public class InfusedSwordItem extends SwordItem {
    private static final int MAX_COOLDOWN = 40;

    public InfusedSwordItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 3, -2.4F, settings.maxDamage(MAX_COOLDOWN));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.getDamage() == 0;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && stack.getDamage() == 0) {
            if (user.isSneaking()) {
                user.getHungerManager().add(6, 0.5F);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP,
                        SoundCategory.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            } else {
                user.heal(6.0F);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HEART,
                            user.getX(),
                            user.getBodyY(0.5D),
                            user.getZ(),
                            6,
                            0.8D,
                            0.4D,
                            0.8D,
                            0.0D);
                }
            }
            stack.setDamage(getMaxDamage());
            user.getItemCooldownManager().set(this, 20);
            return TypedActionResult.success(stack, world.isClient);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int damage = stack.getDamage();
        if (damage > 0) {
            stack.setDamage(damage - 1);
        }
        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, net.minecraft.block.BlockState state, net.minecraft.util.math.BlockPos pos,
                            LivingEntity miner) {
        return true;
    }
}
