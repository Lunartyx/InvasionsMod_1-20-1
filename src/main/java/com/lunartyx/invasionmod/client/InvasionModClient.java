package com.lunartyx.invasionmod.client;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.client.network.InvasionClientNetwork;
import com.lunartyx.invasionmod.client.render.RiftBirdEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftBoltEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftBurrowerEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftEggEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftGiantBirdEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftImpEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftPigEngineerEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftSpiderEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftSpawnProxyEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftThrowerEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftTrapEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftWolfEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftZombieEntityRenderer;
import com.lunartyx.invasionmod.client.render.RiftZombiePiglinEntityRenderer;
import com.lunartyx.invasionmod.client.screen.NexusScreen;
import com.lunartyx.invasionmod.item.custom.ProbeItem;
import com.lunartyx.invasionmod.item.custom.TrapItem;
import com.lunartyx.invasionmod.registry.ModEntityTypes;
import com.lunartyx.invasionmod.registry.ModScreenHandlers;
import com.lunartyx.invasionmod.registry.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.util.Identifier;

public class InvasionModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InvasionMod.LOGGER.info("Initializing client hooks for InvasionMod");
        InvasionClientNetwork.register();
        EntityRendererRegistry.register(ModEntityTypes.RIFT_ZOMBIE, RiftZombieEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_SKELETON, SkeletonEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_SPIDER, RiftSpiderEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_CREEPER, CreeperEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_THROWER, RiftThrowerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_PIG_ENGINEER, RiftPigEngineerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_BURROWER, RiftBurrowerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_IMP, RiftImpEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_ZOMBIE_PIGLIN, RiftZombiePiglinEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_WOLF, RiftWolfEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_BIRD, RiftBirdEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_GIANT_BIRD, RiftGiantBirdEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_EGG, RiftEggEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_SPAWN_PROXY, RiftSpawnProxyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_BOLT, RiftBoltEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_BOULDER, (context) -> new FlyingItemEntityRenderer<>(context));
        EntityRendererRegistry.register(ModEntityTypes.RIFT_TRAP, RiftTrapEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIFT_PRIMED_TNT, TntEntityRenderer::new);
        HandledScreens.register(ModScreenHandlers.NEXUS, NexusScreen::new);
        ModelPredicateProviderRegistry.register(ModItems.PROBE, new Identifier(InvasionMod.MOD_ID, "mode"),
                (stack, world, entity, seed) -> ProbeItem.getMode(stack) == ProbeItem.Mode.ADJUSTER ? 0.0F : 1.0F);
        ModelPredicateProviderRegistry.register(ModItems.IM_TRAP, new Identifier(InvasionMod.MOD_ID, "trap_type"),
                (stack, world, entity, seed) -> TrapItem.getTrapType(stack).id());
    }
}
