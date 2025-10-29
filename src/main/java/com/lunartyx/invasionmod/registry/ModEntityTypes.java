package com.lunartyx.invasionmod.registry;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.entity.custom.RiftBirdEntity;
import com.lunartyx.invasionmod.entity.custom.RiftBoltEntity;
import com.lunartyx.invasionmod.entity.custom.RiftBoulderEntity;
import com.lunartyx.invasionmod.entity.custom.RiftBurrowerEntity;
import com.lunartyx.invasionmod.entity.custom.RiftCreeperEntity;
import com.lunartyx.invasionmod.entity.custom.RiftEggEntity;
import com.lunartyx.invasionmod.entity.custom.RiftGiantBirdEntity;
import com.lunartyx.invasionmod.entity.custom.RiftImpEntity;
import com.lunartyx.invasionmod.entity.custom.RiftPigEngineerEntity;
import com.lunartyx.invasionmod.entity.custom.RiftPrimedTntEntity;
import com.lunartyx.invasionmod.entity.custom.RiftSkeletonEntity;
import com.lunartyx.invasionmod.entity.custom.RiftSpawnProxyEntity;
import com.lunartyx.invasionmod.entity.custom.RiftSpiderEntity;
import com.lunartyx.invasionmod.entity.custom.RiftThrowerEntity;
import com.lunartyx.invasionmod.entity.custom.RiftTrapEntity;
import com.lunartyx.invasionmod.entity.custom.RiftWolfEntity;
import com.lunartyx.invasionmod.entity.custom.RiftZombieEntity;
import com.lunartyx.invasionmod.entity.custom.RiftZombiePiglinEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Centralised registration for all custom invasion mobs. The Fabric entity
 * builder handles hitbox dimensions and spawn rules while attributes are
 * registered through {@link FabricDefaultAttributeRegistry}.
 */
public final class ModEntityTypes {
    public static final EntityType<RiftZombieEntity> RIFT_ZOMBIE = register("rift_zombie",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftZombieEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(8)
                    .build());

    public static final EntityType<RiftSkeletonEntity> RIFT_SKELETON = register("rift_skeleton",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftSkeletonEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.99F))
                    .trackRangeBlocks(8)
                    .build());

    public static final EntityType<RiftSpiderEntity> RIFT_SPIDER = register("rift_spider",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftSpiderEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4F, 0.9F))
                    .trackRangeBlocks(8)
                    .build());

    public static final EntityType<RiftCreeperEntity> RIFT_CREEPER = register("rift_creeper",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftCreeperEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.7F))
                    .trackRangeBlocks(8)
                    .build());

    public static final EntityType<RiftThrowerEntity> RIFT_THROWER = register("rift_thrower",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftThrowerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(10)
                    .build());

    public static final EntityType<RiftPigEngineerEntity> RIFT_PIG_ENGINEER = register("rift_pig_engineer",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftPigEngineerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(10)
                    .build());

    public static final EntityType<RiftBurrowerEntity> RIFT_BURROWER = register("rift_burrower",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftBurrowerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.4F, 0.6F))
                    .trackRangeBlocks(8)
                    .build());

    public static final EntityType<RiftImpEntity> RIFT_IMP = register("rift_imp",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftImpEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 0.8F))
                    .trackRangeBlocks(10)
                    .build());

    public static final EntityType<RiftZombiePiglinEntity> RIFT_ZOMBIE_PIGLIN = register("rift_zombie_piglin",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftZombiePiglinEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(10)
                    .build());

    public static final EntityType<RiftWolfEntity> RIFT_WOLF = register("rift_wolf",
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RiftWolfEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 0.85F))
                    .trackRangeBlocks(8)
                    .build());

    public static final EntityType<RiftBirdEntity> RIFT_BIRD = register("rift_bird",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftBirdEntity::new)
                    .dimensions(EntityDimensions.fixed(0.7F, 0.9F))
                    .trackRangeBlocks(12)
                    .build());

    public static final EntityType<RiftGiantBirdEntity> RIFT_GIANT_BIRD = register("rift_giant_bird",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftGiantBirdEntity::new)
                    .dimensions(EntityDimensions.fixed(1.8F, 1.2F))
                    .trackRangeBlocks(14)
                    .build());

    public static final EntityType<RiftEggEntity> RIFT_EGG = register("rift_egg",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftEggEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 0.8F))
                    .trackRangeBlocks(8)
                    .build());

    public static final EntityType<RiftSpawnProxyEntity> RIFT_SPAWN_PROXY = register("rift_spawn_proxy",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RiftSpawnProxyEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(12)
                    .build());

    public static final EntityType<RiftBoltEntity> RIFT_BOLT = register("rift_bolt",
            FabricEntityTypeBuilder.<RiftBoltEntity>create(SpawnGroup.MISC, RiftBoltEntity::new)
                    .dimensions(EntityDimensions.fixed(0.1F, 0.1F))
                    .trackRangeBlocks(32)
                    .trackedUpdateRate(1)
                    .build());

    public static final EntityType<RiftBoulderEntity> RIFT_BOULDER = register("rift_boulder",
            FabricEntityTypeBuilder.<RiftBoulderEntity>create(SpawnGroup.MISC, RiftBoulderEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
                    .trackRangeBlocks(16)
                    .trackedUpdateRate(4)
                    .build());

    public static final EntityType<RiftTrapEntity> RIFT_TRAP = register("rift_trap",
            FabricEntityTypeBuilder.<RiftTrapEntity>create(SpawnGroup.MISC, RiftTrapEntity::new)
                    .dimensions(EntityDimensions.fixed(0.9F, 0.4F))
                    .trackRangeBlocks(12)
                    .build());

    public static final EntityType<RiftPrimedTntEntity> RIFT_PRIMED_TNT = register("rift_primed_tnt",
            FabricEntityTypeBuilder.<RiftPrimedTntEntity>create(SpawnGroup.MISC, RiftPrimedTntEntity::new)
                    .dimensions(EntityDimensions.fixed(0.98F, 0.98F))
                    .trackRangeBlocks(12)
                    .build());

    private ModEntityTypes() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(RIFT_ZOMBIE, ZombieEntity.createZombieAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_SKELETON, SkeletonEntity.createAbstractSkeletonAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_SPIDER, SpiderEntity.createSpiderAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_CREEPER, CreeperEntity.createCreeperAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_THROWER, PillagerEntity.createPillagerAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_PIG_ENGINEER, ZombifiedPiglinEntity.createZombifiedPiglinAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_BURROWER, SilverfishEntity.createSilverfishAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_IMP, VexEntity.createVexAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_ZOMBIE_PIGLIN, ZombifiedPiglinEntity.createZombifiedPiglinAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_WOLF, WolfEntity.createWolfAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_BIRD, RiftBirdEntity.createBirdAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_GIANT_BIRD, RiftGiantBirdEntity.createGiantBirdAttributes());
        FabricDefaultAttributeRegistry.register(RIFT_EGG, MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0D));
        FabricDefaultAttributeRegistry.register(RIFT_SPAWN_PROXY, HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D));
    }

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(InvasionMod.MOD_ID, name), type);
    }
}
