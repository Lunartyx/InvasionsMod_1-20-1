package com.lunartyx.invasionmod.item.custom;

import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.entity.terrain.BlockStrengthHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ProbeItem extends Item {
    private static final String MODE_KEY = "Mode";

    public enum Mode {
        ADJUSTER("adjuster"),
        MATERIAL("material");

        private final String translationKey;

        Mode(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public Mode cycle() {
            return switch (this) {
                case ADJUSTER -> MATERIAL;
                case MATERIAL -> ADJUSTER;
            };
        }
    }

    public ProbeItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createStack(Mode mode) {
        ItemStack stack = new ItemStack(com.lunartyx.invasionmod.registry.ModItems.PROBE);
        setMode(stack, mode);
        return stack;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.invasionmod.probe." + getMode(stack).translationKey());
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        stacks.add(createStack(Mode.ADJUSTER));
        stacks.add(createStack(Mode.MATERIAL));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user.isSneaking()) {
            Mode newMode = getMode(stack).cycle();
            setMode(stack, newMode);
            MutableText modeName = Text.translatable("item.invasionmod.probe." + newMode.translationKey());
            user.sendMessage(Text.translatable("message.invasionmod.probe.mode", modeName), true);
            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }

        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        Mode mode = getMode(stack);

        if (world.isClient || player == null) {
            return ActionResult.SUCCESS;
        }

        BlockPos pos = context.getBlockPos();
        if (mode == Mode.ADJUSTER) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof NexusBlockEntity nexus)) {
                return ActionResult.PASS;
            }

            boolean decrease = player.isSneaking();
            int newRadius = nexus.cycleSpawnRadius(decrease);
            nexus.notifyInteraction(player);
            player.sendMessage(Text.translatable("message.invasionmod.nexus_range", newRadius), true);
            return ActionResult.CONSUME;
        }

        BlockState state = world.getBlockState(pos);
        float strength = BlockStrengthHelper.getBlockStrength(state, world, pos);
        if (Float.isInfinite(strength) || strength < 0.0F) {
            player.sendMessage(Text.translatable("message.invasionmod.probe.block_indestructible"), true);
        } else {
            player.sendMessage(Text.translatable("message.invasionmod.probe.block_strength",
                    String.format("%.2f", strength)), true);
        }
        return ActionResult.CONSUME;
    }

    public static Mode getMode(@NotNull ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(MODE_KEY)) {
            nbt.putString(MODE_KEY, Mode.ADJUSTER.translationKey());
            return Mode.ADJUSTER;
        }
        String value = nbt.getString(MODE_KEY);
        for (Mode mode : Mode.values()) {
            if (mode.translationKey().equals(value)) {
                return mode;
            }
        }
        nbt.putString(MODE_KEY, Mode.ADJUSTER.translationKey());
        return Mode.ADJUSTER;
    }

    private static void setMode(ItemStack stack, Mode mode) {
        stack.getOrCreateNbt().putString(MODE_KEY, mode.translationKey());
    }
}
