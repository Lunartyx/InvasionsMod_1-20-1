package com.lunartyx.invasionmod.registry;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.screen.NexusScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class ModScreenHandlers {
    public static final ScreenHandlerType<NexusScreenHandler> NEXUS = ScreenHandlerRegistry.registerExtended(
            new Identifier(InvasionMod.MOD_ID, "nexus"),
            (syncId, playerInventory, buf) -> {
                BlockPos pos = buf.readBlockPos();
                BlockEntity blockEntity = playerInventory.player.getWorld().getBlockEntity(pos);
                if (blockEntity instanceof NexusBlockEntity nexus) {
                    return new NexusScreenHandler(syncId, playerInventory, nexus, nexus.getPropertyDelegate());
                }
                return new NexusScreenHandler(syncId, playerInventory);
            }
    );

    private ModScreenHandlers() {
    }

    public static void registerScreenHandlers() {
        InvasionMod.LOGGER.debug("Registering screen handlers for {}", InvasionMod.MOD_ID);
    }
}
