package com.lunartyx.invasionmod.registry;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    public static final BlockEntityType<NexusBlockEntity> NEXUS = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(InvasionMod.MOD_ID, "nexus"),
            FabricBlockEntityTypeBuilder.create(NexusBlockEntity::new, ModBlocks.NEXUS).build(null)
    );

    private ModBlockEntities() {
    }

    public static void registerBlockEntities() {
        InvasionMod.LOGGER.info("Registering block entities for {}", InvasionMod.MOD_ID);
    }
}
