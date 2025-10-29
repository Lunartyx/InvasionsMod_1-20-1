package com.lunartyx.invasionmod.registry;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.config.InvasionConfigManager;
import com.lunartyx.invasionmod.item.custom.DebugWandItem;
import com.lunartyx.invasionmod.item.custom.InfusedSwordItem;
import com.lunartyx.invasionmod.item.custom.ProbeItem;
import com.lunartyx.invasionmod.item.custom.SearingBowItem;
import com.lunartyx.invasionmod.item.custom.StrangeBoneItem;
import com.lunartyx.invasionmod.item.custom.TrapItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class ModItems {
    public static final Item PHASE_CRYSTAL = register("phase_crystal", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item RIFT_FLUX = register("rift_flux", new Item(new FabricItemSettings().maxCount(64)));
    public static final Item SMALL_REMNANTS = register("small_remnants", new Item(new FabricItemSettings().maxCount(64)));
    public static final Item NEXUS_CATALYST = register("nexus_catalyst", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item INFUSED_SWORD = register("infused_sword", new InfusedSwordItem(ToolMaterials.DIAMOND, new FabricItemSettings().maxCount(1)));
    public static final Item IM_TRAP = register("im_trap", new TrapItem(new FabricItemSettings()));
    public static final Item SEARING_BOW = register("searing_bow", new SearingBowItem(new FabricItemSettings().maxDamage(512)));
    public static final Item CATALYST_MIXTURE = register("catalyst_mixture", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item STABLE_CATALYST_MIXTURE = register("stable_catalyst_mixture", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item STABLE_NEXUS_CATALYST = register("stable_nexus_catalyst", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item DAMPING_AGENT = register("damping_agent", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item STRONG_DAMPING_AGENT = register("strong_damping_agent", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item STRANGE_BONE = register("strange_bone", new StrangeBoneItem(new FabricItemSettings().maxCount(1)));
    public static final Item PROBE = register("probe", new ProbeItem(new FabricItemSettings().maxCount(1)));
    public static final Item STRONG_CATALYST = register("strong_catalyst", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item ENGY_HAMMER = register("engy_hammer", new PickaxeItem(ToolMaterials.IRON, 1, -2.8F, new FabricItemSettings()));
    @Nullable
    public static final Item DEBUG_WAND = InvasionConfigManager.getConfig().general().debugMode()
            ? register("debug_wand", new DebugWandItem(new FabricItemSettings().maxCount(1)))
            : null;

    public static final Item RIFT_ZOMBIE_SPAWN_EGG = register("rift_zombie_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_ZOMBIE, 0x3f4a3d, 0x7c8b69, new FabricItemSettings()));
    public static final Item RIFT_SKELETON_SPAWN_EGG = register("rift_skeleton_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_SKELETON, 0xc7c7c7, 0x3e3e3e, new FabricItemSettings()));
    public static final Item RIFT_SPIDER_SPAWN_EGG = register("rift_spider_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_SPIDER, 0x352a1b, 0x8f5b30, new FabricItemSettings()));
    public static final Item RIFT_CREEPER_SPAWN_EGG = register("rift_creeper_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_CREEPER, 0x1f5131, 0x3f7c4a, new FabricItemSettings()));
    public static final Item RIFT_THROWER_SPAWN_EGG = register("rift_thrower_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_THROWER, 0x67513a, 0xc06e25, new FabricItemSettings()));
    public static final Item RIFT_PIG_ENGINEER_SPAWN_EGG = register("rift_pig_engineer_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_PIG_ENGINEER, 0x86443a, 0xdec17d, new FabricItemSettings()));
    public static final Item RIFT_BURROWER_SPAWN_EGG = register("rift_burrower_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_BURROWER, 0x5a574b, 0xb9ab78, new FabricItemSettings()));
    public static final Item RIFT_IMP_SPAWN_EGG = register("rift_imp_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_IMP, 0x3b1f1f, 0xe06b46, new FabricItemSettings()));
    public static final Item RIFT_ZOMBIE_PIGLIN_SPAWN_EGG = register("rift_zombie_piglin_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_ZOMBIE_PIGLIN, 0xeb8e91, 0x49652f, new FabricItemSettings()));
    public static final Item RIFT_WOLF_SPAWN_EGG = register("rift_wolf_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_WOLF, 0x242424, 0x56a3d9, new FabricItemSettings()));
    public static final Item RIFT_BIRD_SPAWN_EGG = register("rift_bird_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_BIRD, 0x4866a4, 0xf4d663, new FabricItemSettings()));
    public static final Item RIFT_GIANT_BIRD_SPAWN_EGG = register("rift_giant_bird_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_GIANT_BIRD, 0x1b1b1b, 0x6dddf0, new FabricItemSettings()));
    public static final Item RIFT_EGG_SPAWN_EGG = register("rift_egg_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_EGG, 0xbba66c, 0x3d341f, new FabricItemSettings()));
    public static final Item RIFT_SPAWN_PROXY_SPAWN_EGG = register("rift_spawn_proxy_spawn_egg", new SpawnEggItem(ModEntityTypes.RIFT_SPAWN_PROXY, 0x2e2e2e, 0x9c7c4c, new FabricItemSettings()));

    private ModItems() {
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(InvasionMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        InvasionMod.LOGGER.info("Registering items for {}", InvasionMod.MOD_ID);
        if (DEBUG_WAND == null) {
            InvasionMod.LOGGER.info("Debug wand disabled via config");
        }
    }
}
