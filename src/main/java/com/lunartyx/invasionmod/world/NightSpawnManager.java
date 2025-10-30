package com.lunartyx.invasionmod.world;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.config.InvasionConfig;
import com.lunartyx.invasionmod.config.InvasionConfigManager;
import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class NightSpawnManager {
    private static final Map<String, EntityType<? extends MobEntity>> PATTERN_MAP = createPatternMap();

    private static WeightedSelector<EntityType<? extends MobEntity>> selector = WeightedSelector.empty();
    private static InvasionConfig.NightSpawns settings;
    private static boolean spawnRulesRegistered;
    private static boolean worldEventsRegistered;

    private NightSpawnManager() {}

    public static void initialize() {
        InvasionConfig config = InvasionConfigManager.getConfig();
        settings = config.nightSpawns();
        selector = buildSelector(config.nightSpawnPools().pool1());

        if (!settings.nightSpawnsEnabled()) {
            InvasionMod.LOGGER.info("Night spawns disabled via config; skipping spawn injection");
            return;
        }

        registerBiomeSpawns(settings);
        registerWorldEvents(settings);
        InvasionMod.LOGGER.info("Enabled night spawn manager with {} configured patterns", selector.size());
    }

    public static void handleProxySpawn(ServerWorld world, BlockPos origin, Random random) {
        if (!isActive()) return;

        int maxGroup = Math.max(1, settings.nightMobMaxGroupSize());
        int mobCount = random.nextInt(maxGroup) + 1;
        for (int i = 0; i < mobCount; i++) {
            EntityType<? extends MobEntity> type = selector.pick(random);
            if (type == null) continue;
            spawnConfiguredMob(world, origin, random, type);
        }
    }

    private static boolean isActive() {
        return settings != null && settings.nightSpawnsEnabled();
    }

    private static void spawnConfiguredMob(ServerWorld world, BlockPos origin, Random random, EntityType<? extends MobEntity> type) {
        BlockPos spawnPos = offsetSpawnPos(world, origin, random);
        MobEntity mob = type.spawn(world, null, null, spawnPos, SpawnReason.EVENT, true, true);
        if (mob == null) return;

        mob.setPersistent();
        applyFollowRange(mob, settings.nightMobSightRange());
        applyStatScaling(mob, settings.nightMobStatsScaling());
        applyBurnBehaviour(mob, settings.nightMobsBurnInDay());
    }

    private static BlockPos offsetSpawnPos(ServerWorld world, BlockPos origin, Random random) {
        int dx = random.nextBetween(-2, 2);
        int dz = random.nextBetween(-2, 2);
        int dy = random.nextBetween(-1, 1);
        BlockPos.Mutable mutable = origin.mutableCopy().move(dx, dy, dz);
        if (!world.isAir(mutable)) {
            while (!world.isAir(mutable) && mutable.getY() < world.getTopY()) {
                mutable.move(0, 1, 0);
            }
        }
        return mutable.toImmutable();
    }

    private static void applyFollowRange(MobEntity mob, int followRange) {
        EntityAttributeInstance attribute = mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (attribute != null && followRange > 0) {
            attribute.setBaseValue(Math.max(attribute.getBaseValue(), followRange));
        }
    }

    private static void applyStatScaling(MobEntity mob, float scale) {
        if (scale == 1.0F) return;

        EntityAttributeInstance health = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * scale);
            mob.setHealth((float) health.getValue());
        }

        EntityAttributeInstance damage = mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getBaseValue() * scale);
        }
    }

    // --- FIX: 1.20.1 hat keinen Setter für "Burn in Day" ---
    private static void applyBurnBehaviour(MobEntity mob, boolean burnsInDay) {
        if (!burnsInDay && (mob instanceof ZombieEntity || mob instanceof AbstractSkeletonEntity)) {
            mob.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE,
                    20 * 60 * 60, // 1 Stunde
                    0,
                    true,  // ambient
                    false, // keine Partikel
                    false  // kein Icon
            ));
        }
    }

    private static void registerBiomeSpawns(InvasionConfig.NightSpawns settings) {
        if (spawnRulesRegistered) return;

        var biomeSelector = BiomeSelectors.includeByKey(
                BiomeKeys.PLAINS,
                BiomeKeys.WINDSWEPT_HILLS,
                BiomeKeys.FOREST,
                BiomeKeys.TAIGA,
                BiomeKeys.SWAMP,
                BiomeKeys.WINDSWEPT_FOREST,
                BiomeKeys.SNOWY_TAIGA,
                BiomeKeys.JUNGLE,
                BiomeKeys.SPARSE_JUNGLE
        );

        BiomeModifications.addSpawn(biomeSelector, SpawnGroup.MONSTER, ModEntityTypes.RIFT_SPAWN_PROXY, settings.nightMobSpawnChance(), 1, 1);
        BiomeModifications.addSpawn(biomeSelector, SpawnGroup.MONSTER, ModEntityTypes.RIFT_ZOMBIE, 1, 1, 1);
        BiomeModifications.addSpawn(biomeSelector, SpawnGroup.MONSTER, ModEntityTypes.RIFT_SPIDER, 1, 1, 1);
        BiomeModifications.addSpawn(biomeSelector, SpawnGroup.MONSTER, ModEntityTypes.RIFT_SKELETON, 1, 1, 1);

        spawnRulesRegistered = true;
    }

    // --- FIX: 1.20.1 hat kein GameRule "SPAWN_CAP_MONSTER" ---
    private static void registerWorldEvents(InvasionConfig.NightSpawns settings) {
        if (worldEventsRegistered) return;

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (!isActive() || world.getRegistryKey() != World.OVERWORLD) return;

            int override = settings.mobLimitOverride();
            if (override > 0) {
                InvasionMod.LOGGER.warn(
                        "mobLimitOverride={} konfiguriert, aber auf MC 1.20.1 nicht verfügbar – wird ignoriert.",
                        override
                );
            }
        });

        worldEventsRegistered = true;
    }

    private static WeightedSelector<EntityType<? extends MobEntity>> buildSelector(List<InvasionConfig.NightSpawnEntry> entries) {
        WeightedSelector<EntityType<? extends MobEntity>> selector = WeightedSelector.empty();
        if (entries == null || entries.isEmpty()) {
            selector.add(ModEntityTypes.RIFT_ZOMBIE, 1.0F);
            return selector;
        }

        for (InvasionConfig.NightSpawnEntry entry : entries) {
            if (entry == null) continue;
            EntityType<? extends MobEntity> type = resolvePattern(entry.pattern());
            if (type == null) {
                InvasionMod.LOGGER.warn("Unknown night spawn pattern '{}'; skipping", entry.pattern());
                continue;
            }
            if (entry.weight() <= 0.0F) continue;
            selector.add(type, entry.weight());
        }

        if (selector.isEmpty()) selector.add(ModEntityTypes.RIFT_ZOMBIE, 1.0F);
        return selector;
    }

    private static EntityType<? extends MobEntity> resolvePattern(String pattern) {
        if (pattern == null) return null;
        EntityType<? extends MobEntity> mapped = PATTERN_MAP.get(pattern.toLowerCase(Locale.ROOT));
        if (mapped != null) return mapped;

        Identifier identifier = Identifier.tryParse(pattern);
        if (identifier != null) {
            EntityType<?> direct = Registries.ENTITY_TYPE.getOrEmpty(identifier).orElse(null);
            if (direct != null && MobEntity.class.isAssignableFrom(direct.getBaseClass())) {
                @SuppressWarnings("unchecked")
                EntityType<? extends MobEntity> mobType = (EntityType<? extends MobEntity>) direct;
                return mobType;
            }
        }
        return null;
    }

    private static Map<String, EntityType<? extends MobEntity>> createPatternMap() {
        Map<String, EntityType<? extends MobEntity>> map = new HashMap<>();
        map.put("zombie_t1_any", ModEntityTypes.RIFT_ZOMBIE);
        map.put("zombie_t2_any_basic", ModEntityTypes.RIFT_ZOMBIE);
        map.put("zombie_t2_plain", ModEntityTypes.RIFT_ZOMBIE);
        map.put("zombie_t2_tar", ModEntityTypes.RIFT_ZOMBIE);
        map.put("zombie_t2_pigman", ModEntityTypes.RIFT_ZOMBIE_PIGLIN);
        map.put("zombie_t3_any", ModEntityTypes.RIFT_ZOMBIE_PIGLIN);
        map.put("spider_t1_any", ModEntityTypes.RIFT_SPIDER);
        map.put("spider_t2_any", ModEntityTypes.RIFT_SPIDER);
        map.put("pigengy_t1_any", ModEntityTypes.RIFT_PIG_ENGINEER);
        map.put("skeleton_t1_any", ModEntityTypes.RIFT_SKELETON);
        map.put("thrower_t1", ModEntityTypes.RIFT_THROWER);
        map.put("thrower_t2", ModEntityTypes.RIFT_THROWER);
        map.put("creeper_t1_basic", ModEntityTypes.RIFT_CREEPER);
        map.put("imp_t1", ModEntityTypes.RIFT_IMP);
        return map;
    }

    private static final class WeightedSelector<T> {
        private final List<Entry<T>> entries;
        private float totalWeight;

        private WeightedSelector() { this.entries = new ArrayList<>(); }

        static <T> WeightedSelector<T> empty() { return new WeightedSelector<>(); }

        void add(T value, float weight) {
            if (value == null || weight <= 0.0F) return;
            entries.add(new Entry<>(value, weight));
            totalWeight += weight;
        }

        boolean isEmpty() { return entries.isEmpty(); }

        int size() { return entries.size(); }

        T pick(Random random) {
            if (entries.isEmpty()) return null;
            float target = random.nextFloat() * (totalWeight <= 0.0F ? entries.size() : totalWeight);
            if (totalWeight <= 0.0F) {
                int index = Math.min(entries.size() - 1, (int) target);
                return entries.get(index).value();
            }
            float cumulative = 0.0F;
            for (Entry<T> entry : entries) {
                cumulative += entry.weight();
                if (target <= cumulative) return entry.value();
            }
            return entries.get(entries.size() - 1).value();
        }

        private record Entry<T>(T value, float weight) {
            Entry { Objects.requireNonNull(value, "value"); }
        }
    }
}
