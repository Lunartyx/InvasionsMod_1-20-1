package com.lunartyx.invasionmod;

import com.lunartyx.invasionmod.command.InvasionCommands;
import com.lunartyx.invasionmod.config.InvasionConfigManager;
import com.lunartyx.invasionmod.network.InvasionNetwork;
import com.lunartyx.invasionmod.recipe.RecipeConditions;
import com.lunartyx.invasionmod.registry.ModBlockEntities;
import com.lunartyx.invasionmod.registry.ModBlocks;
import com.lunartyx.invasionmod.registry.ModEntityTypes;
import com.lunartyx.invasionmod.registry.ModItemGroups;
import com.lunartyx.invasionmod.registry.ModItems;
import com.lunartyx.invasionmod.registry.ModScreenHandlers;
import com.lunartyx.invasionmod.registry.ModSoundEvents;
import com.lunartyx.invasionmod.entity.terrain.BlockStrengthHelper;
import com.lunartyx.invasionmod.world.NightSpawnManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvasionMod implements ModInitializer {
    public static final String MOD_ID = "invasionmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing InvasionMod Fabric port");
        InvasionConfigManager.initialize();
        BlockStrengthHelper.initialize(InvasionConfigManager.getConfig());
        ModSoundEvents.registerSoundEvents();
        InvasionNetwork.init();
        RecipeConditions.init();
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModBlockEntities.registerBlockEntities();
        ModEntityTypes.register();
        ModItemGroups.registerItemGroups();
        ModScreenHandlers.registerScreenHandlers();
        InvasionCommands.register();
        NightSpawnManager.initialize();
    }
}
