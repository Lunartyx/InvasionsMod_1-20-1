package com.lunartyx.invasionmod.recipe;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.config.InvasionConfigManager;
import com.lunartyx.invasionmod.mixin.RecipeManagerAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RecipeConditions {
    private static final Set<Identifier> CONTROLLED_RECIPES = Set.of(
            id("nexus"),
            id("phase_crystal_from_lapis"),
            id("phase_crystal_from_redstone"),
            id("rift_flux_from_small_remnants"),
            id("infused_sword"),
            id("catalyst_mixture"),
            id("catalyst_mixture_mirrored"),
            id("stable_catalyst_mixture"),
            id("damping_agent"),
            id("strong_damping_agent_column"),
            id("strong_damping_agent_row"),
            id("strange_bone"),
            id("searing_bow"),
            id("diamond_from_flux_column"),
            id("diamond_from_flux_row"),
            id("iron_from_flux"),
            id("redstone_from_flux"),
            id("lapis_from_flux"),
            id("im_trap_empty"),
            id("im_trap_flame"),
            id("probe_base"),
            id("probe_material_mode"),
            id("smelting/catalyst_mixture"),
            id("smelting/stable_catalyst_mixture")
    );

    private RecipeConditions() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> applyConfig(server, false));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                applyConfig(server, true);
            }
        });
    }

    private static void applyConfig(MinecraftServer server, boolean isReload) {
        boolean enabled = InvasionConfigManager.getConfig().general().craftItemsEnabled();
        if (enabled) {
            if (isReload) {
                InvasionMod.LOGGER.info("Crafting recipes remain enabled after data pack reload");
            }
            return;
        }

        RecipeManager manager = server.getRecipeManager();
        RecipeManagerAccessor accessor = (RecipeManagerAccessor) manager;
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipesByType = accessor.invasionmod$getRecipes();
        Map<Identifier, Recipe<?>> recipesById = accessor.invasionmod$getRecipesById();

        int removed = 0;
        for (Identifier identifier : CONTROLLED_RECIPES) {
            Recipe<?> recipe = recipesById.remove(identifier);
            if (recipe == null) {
                continue;
            }
            removed++;
            Optional.ofNullable(recipesByType.get(recipe.getType()))
                    .ifPresent(map -> map.remove(identifier));
        }

        if (removed > 0) {
            InvasionMod.LOGGER.info("Disabled {} invasion crafting recipes because crafting is disabled in config", removed);
        } else if (isReload) {
            InvasionMod.LOGGER.info("No invasion crafting recipes required removal on reload");
        }
    }

    private static Identifier id(String path) {
        return new Identifier(InvasionMod.MOD_ID, path);
    }
}
