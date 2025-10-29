package com.lunartyx.invasionmod.registry;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.item.custom.ProbeItem;
import com.lunartyx.invasionmod.item.custom.TrapItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    public static final ItemGroup INVASION_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier(InvasionMod.MOD_ID, "general"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.PHASE_CRYSTAL))
                    .displayName(Text.translatable("itemgroup.invasionmod.general"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.NEXUS);
                        entries.add(ModItems.PHASE_CRYSTAL);
                        entries.add(ModItems.RIFT_FLUX);
                        entries.add(ModItems.SMALL_REMNANTS);
                        entries.add(ModItems.NEXUS_CATALYST);
                        entries.add(ModItems.INFUSED_SWORD);
                        TrapItem trapItem = (TrapItem) ModItems.IM_TRAP;
                        entries.add(trapItem.createStack(TrapItem.TrapType.EMPTY));
                        entries.add(trapItem.createStack(TrapItem.TrapType.RIFT));
                        entries.add(trapItem.createStack(TrapItem.TrapType.FLAME));
                        entries.add(ModItems.SEARING_BOW);
                        entries.add(ModItems.CATALYST_MIXTURE);
                        entries.add(ModItems.STABLE_CATALYST_MIXTURE);
                        entries.add(ModItems.STABLE_NEXUS_CATALYST);
                        entries.add(ModItems.DAMPING_AGENT);
                        entries.add(ModItems.STRONG_DAMPING_AGENT);
                        entries.add(ModItems.STRANGE_BONE);
                        entries.add(ProbeItem.createStack(ProbeItem.Mode.ADJUSTER));
                        entries.add(ProbeItem.createStack(ProbeItem.Mode.MATERIAL));
                        entries.add(ModItems.STRONG_CATALYST);
                        entries.add(ModItems.ENGY_HAMMER);
                        if (ModItems.DEBUG_WAND != null) {
                            entries.add(ModItems.DEBUG_WAND);
                        }
                        entries.add(ModItems.RIFT_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.RIFT_SKELETON_SPAWN_EGG);
                        entries.add(ModItems.RIFT_SPIDER_SPAWN_EGG);
                        entries.add(ModItems.RIFT_CREEPER_SPAWN_EGG);
                        entries.add(ModItems.RIFT_THROWER_SPAWN_EGG);
                        entries.add(ModItems.RIFT_PIG_ENGINEER_SPAWN_EGG);
                        entries.add(ModItems.RIFT_BURROWER_SPAWN_EGG);
                        entries.add(ModItems.RIFT_IMP_SPAWN_EGG);
                        entries.add(ModItems.RIFT_ZOMBIE_PIGLIN_SPAWN_EGG);
                        entries.add(ModItems.RIFT_WOLF_SPAWN_EGG);
                        entries.add(ModItems.RIFT_BIRD_SPAWN_EGG);
                        entries.add(ModItems.RIFT_GIANT_BIRD_SPAWN_EGG);
                        entries.add(ModItems.RIFT_EGG_SPAWN_EGG);
                        entries.add(ModItems.RIFT_SPAWN_PROXY_SPAWN_EGG);
                    })
                    .build()
    );

    private ModItemGroups() {
    }

    public static void registerItemGroups() {
        InvasionMod.LOGGER.info("Registering item groups for {}", InvasionMod.MOD_ID);
    }
}
