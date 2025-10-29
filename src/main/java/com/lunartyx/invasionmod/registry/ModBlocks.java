package com.lunartyx.invasionmod.registry;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.block.NexusBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block NEXUS = registerBlock("nexus",
            new NexusBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).strength(3.0F, 1200.0F).requiresTool().luminance(state -> state.get(NexusBlock.ACTIVE) ? 8 : 0)));

    private ModBlocks() {
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(InvasionMod.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(InvasionMod.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        InvasionMod.LOGGER.info("Registering blocks for {}", InvasionMod.MOD_ID);
    }
}
