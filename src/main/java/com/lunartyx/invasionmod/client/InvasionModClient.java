package com.lunartyx.invasionmod.client;

import com.lunartyx.invasionmod.InvasionMod;
import net.fabricmc.api.ClientModInitializer;

public class InvasionModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InvasionMod.LOGGER.info("Initializing client hooks for InvasionMod");
    }
}
