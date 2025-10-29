package com.lunartyx.invasionmod.screen;

import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class NexusScreenHandler extends ScreenHandler {
    private static final int NEXUS_SLOTS = 2;
    private final Inventory inventory;
    private final PropertyDelegate properties;

    public NexusScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(NEXUS_SLOTS), new ArrayPropertyDelegate(NexusBlockEntity.PROPERTY_COUNT));
    }

    public NexusScreenHandler(int syncId, PlayerInventory playerInventory, NexusBlockEntity nexus, PropertyDelegate properties) {
        this(syncId, playerInventory, (Inventory) nexus, properties);
    }

    public NexusScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        super(ModScreenHandlers.NEXUS, syncId);
        checkSize(inventory, NEXUS_SLOTS);
        this.inventory = inventory;
        this.properties = properties;

        inventory.onOpen(playerInventory.player);
        addNexusSlots(inventory);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addProperties(properties);
    }

    private void addNexusSlots(Inventory inventory) {
        this.addSlot(new Slot(inventory, 0, 32, 33) {
            @Override
            public boolean canInsert(ItemStack stack) {
                if (inventory instanceof NexusBlockEntity nexus) {
                    return nexus.isValidInput(stack);
                }
                return true;
            }
        });

        this.addSlot(new Slot(inventory, 1, 102, 33) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                int index = column + row * 9 + 9;
                int x = 8 + column * 18;
                int y = 84 + row * 18;
                this.addSlot(new Slot(playerInventory, index, x, y));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            newStack = stack.copy();

            if (index == 1) {
                if (!this.insertItem(stack, NEXUS_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(stack, newStack);
            } else if (index == 0) {
                if (!this.insertItem(stack, NEXUS_SLOTS, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (canInsertIntoInput(stack)) {
                if (!this.insertItem(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 29) {
                if (!this.insertItem(stack, 29, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(stack, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, stack);
        }

        return newStack;
    }

    private boolean canInsertIntoInput(ItemStack stack) {
        if (inventory instanceof NexusBlockEntity nexus) {
            return nexus.isValidInput(stack);
        }
        return true;
    }

    public int getActivationProgressScaled(int pixels) {
        int activation = properties.get(0);
        if (activation <= 0) {
            return 0;
        }
        return activation * pixels / NexusBlockEntity.ACTIVATION_TIME;
    }

    public int getFluxProgressScaled(int pixels) {
        int flux = properties.get(5);
        if (flux <= 0) {
            return 0;
        }
        return Math.min(pixels, flux * pixels / NexusBlockEntity.FLUX_PER_CRYSTAL);
    }

    public int getCookProgressScaled(int pixels) {
        int cook = properties.get(6);
        if (cook <= 0) {
            return 0;
        }
        return Math.min(pixels, cook * pixels / NexusBlockEntity.MAX_COOK_TIME);
    }

    public NexusBlockEntity.Mode getMode() {
        return NexusBlockEntity.Mode.byIndex(properties.get(1));
    }

    public boolean isActivating() {
        int activation = properties.get(0);
        NexusBlockEntity.Mode mode = getMode();
        return activation > 0 && (mode == NexusBlockEntity.Mode.DORMANT || mode == NexusBlockEntity.Mode.ACTIVATING);
    }

    public boolean isContinuousActivation() {
        return properties.get(0) > 0 && getMode() == NexusBlockEntity.Mode.CONTINUOUS;
    }

    public int getNexusLevel() {
        return properties.get(3);
    }

    public int getCurrentWave() {
        return properties.get(2);
    }

    public int getSpawnRadius() {
        return properties.get(4);
    }

    public int getHealth() {
        return properties.get(7);
    }

    public int getMobsRemaining() {
        return properties.get(8);
    }

    public int getActiveMobs() {
        return properties.get(9);
    }

    public int getWaveCooldown() {
        return properties.get(10);
    }

    public PropertyDelegate getProperties() {
        return properties;
    }
}
