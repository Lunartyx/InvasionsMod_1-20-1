package com.lunartyx.invasionmod.item.custom;

import com.lunartyx.invasionmod.entity.custom.RiftTrapEntity;
import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class TrapItem extends Item {
    private static final String TRAP_TYPE_KEY = "TrapType";

    public TrapItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.invasionmod.im_trap." + getTrapType(stack).translationKey());
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        stacks.add(createStack(TrapType.EMPTY));
        stacks.add(createStack(TrapType.RIFT));
        stacks.add(createStack(TrapType.FLAME));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Direction side = context.getSide();
        TrapType type = getTrapType(context.getStack());
        if (!type.isPlaceable()) {
            return ActionResult.PASS;
        }

        if (side != Direction.UP) {
            return ActionResult.FAIL;
        }

        BlockPos placePos = context.getBlockPos().up();
        RiftTrapEntity trap = new RiftTrapEntity(ModEntityTypes.RIFT_TRAP, world, placePos, type.id());
        if (!trap.canPlaceAt(placePos)) {
            return ActionResult.FAIL;
        }

        if (!world.isClient) {
            if (!world.spawnEntity(trap)) {
                return ActionResult.FAIL;
            }
            PlayerEntity player = context.getPlayer();
            if (player == null || !player.getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
        }

        return ActionResult.success(world.isClient);
    }

    public ItemStack createStack(TrapType type) {
        ItemStack stack = new ItemStack(this);
        setTrapType(stack, type);
        return stack;
    }

    public static TrapType getTrapType(@NotNull ItemStack stack) {
        if (!(stack.getItem() instanceof TrapItem)) {
            return TrapType.EMPTY;
        }
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(TRAP_TYPE_KEY, NbtElement.INT_TYPE)) {
            return TrapType.EMPTY;
        }
        return TrapType.byId(nbt.getInt(TRAP_TYPE_KEY));
    }

    public static void setTrapType(ItemStack stack, TrapType type) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (type == TrapType.EMPTY) {
            if (nbt.contains(TRAP_TYPE_KEY)) {
                nbt.remove(TRAP_TYPE_KEY);
            }
            if (nbt.isEmpty()) {
                stack.setNbt(null);
            }
            return;
        }
        nbt.putInt(TRAP_TYPE_KEY, type.id());
    }

    public enum TrapType {
        EMPTY(0, "empty", false),
        RIFT(1, "rift", true),
        FLAME(2, "flame", true);

        private final int id;
        private final String translationKey;
        private final boolean placeable;

        TrapType(int id, String translationKey, boolean placeable) {
            this.id = id;
            this.translationKey = translationKey;
            this.placeable = placeable;
        }

        public int id() {
            return id;
        }

        public String translationKey() {
            return translationKey;
        }

        public boolean isPlaceable() {
            return placeable;
        }

        public static TrapType byId(int id) {
            for (TrapType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return EMPTY;
        }
    }
}
