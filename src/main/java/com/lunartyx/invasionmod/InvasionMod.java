package com.lunartyx.invasionmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvasionMod implements ModInitializer {
    public static final String MOD_ID = "invasionmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing InvasionMod Fabric port");
    }
}
