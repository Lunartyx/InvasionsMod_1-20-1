package com.lunartyx.invasionmod.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Serializable configuration payload that mirrors the Forge-era
 * {@code ConfigInvasion}. The structure is intentionally verbose so that we can
 * retain backwards-compatible field names while still offering strong typing to
 * the rest of the Fabric port.
 */
public final class InvasionConfig {
    private General general;
    private ContinuousMode continuousMode;
    private NightSpawns nightSpawns;
    private MobHealth invasionMobHealth;
    private MobHealth nightMobHealth;
    private NightSpawnPools nightSpawnPools;
    private Map<String, Float> blockStrengthOverrides;

    public static InvasionConfig createDefault() {
        InvasionConfig config = new InvasionConfig();
        config.general = General.createDefault();
        config.continuousMode = ContinuousMode.createDefault();
        config.nightSpawns = NightSpawns.createDefault();
        config.invasionMobHealth = MobHealth.createDefault(defaultInvasionHealth());
        config.nightMobHealth = MobHealth.createDefault(defaultNightHealth());
        config.nightSpawnPools = NightSpawnPools.createDefault();
        config.blockStrengthOverrides = new LinkedHashMap<>();
        return config;
    }

    public void applyFallbacks() {
        if (general == null) {
            general = General.createDefault();
        } else {
            general.applyFallbacks();
        }
        if (continuousMode == null) {
            continuousMode = ContinuousMode.createDefault();
        } else {
            continuousMode.applyFallbacks();
        }
        if (nightSpawns == null) {
            nightSpawns = NightSpawns.createDefault();
        } else {
            nightSpawns.applyFallbacks();
        }
        if (invasionMobHealth == null) {
            invasionMobHealth = MobHealth.createDefault(defaultInvasionHealth());
        } else {
            invasionMobHealth.applyFallbacks(defaultInvasionHealth());
        }
        if (nightMobHealth == null) {
            nightMobHealth = MobHealth.createDefault(defaultNightHealth());
        } else {
            nightMobHealth.applyFallbacks(defaultNightHealth());
        }
        if (nightSpawnPools == null) {
            nightSpawnPools = NightSpawnPools.createDefault();
        } else {
            nightSpawnPools.applyFallbacks();
        }
        if (blockStrengthOverrides == null) {
            blockStrengthOverrides = new LinkedHashMap<>();
        }
    }

    public General general() {
        return general;
    }

    public ContinuousMode continuousMode() {
        return continuousMode;
    }

    public NightSpawns nightSpawns() {
        return nightSpawns;
    }

    public MobHealth invasionMobHealth() {
        return invasionMobHealth;
    }

    public MobHealth nightMobHealth() {
        return nightMobHealth;
    }

    public NightSpawnPools nightSpawnPools() {
        return nightSpawnPools;
    }

    public Map<String, Float> blockStrengthOverrides() {
        return blockStrengthOverrides;
    }

    private static Map<String, Integer> defaultInvasionHealth() {
        Map<String, Integer> defaults = new LinkedHashMap<>();
        defaults.put("IMCreeper-T1-invasionSpawn-health", 20);
        defaults.put("IMVulture-T1-invasionSpawn-health", 20);
        defaults.put("IMImp-T1-invasionSpawn-health", 20);
        defaults.put("IMPigManEngineer-T1-invasionSpawn-health", 20);
        defaults.put("IMSkeleton-T1-invasionSpawn-health", 20);
        defaults.put("IMSpider-T1-Spider-invasionSpawn-health", 18);
        defaults.put("IMSpider-T1-Baby-Spider-invasionSpawn-health", 3);
        defaults.put("IMSpider-T2-Jumping-Spider-invasionSpawn-health", 18);
        defaults.put("IMSpider-T2-Mother-Spider-invasionSpawn-health", 23);
        defaults.put("IMThrower-T1-invasionSpawn-health", 50);
        defaults.put("IMThrower-T2-invasionSpawn-health", 70);
        defaults.put("IMZombie-T1-invasionSpawn-health", 20);
        defaults.put("IMZombie-T2-invasionSpawn-health", 30);
        defaults.put("IMZombie-T3-invasionSpawn-health", 65);
        defaults.put("IMZombiePigman-T1-invasionSpawn-health", 20);
        defaults.put("IMZombiePigman-T2-invasionSpawn-health", 30);
        defaults.put("IMZombiePigman-T3-invasionSpawn-health", 65);
        return defaults;
    }

    private static Map<String, Integer> defaultNightHealth() {
        Map<String, Integer> defaults = new LinkedHashMap<>();
        defaults.put("IMCreeper-T1-nightSpawn-health", 20);
        defaults.put("IMVulture-T1-nightSpawn-health", 20);
        defaults.put("IMImp-T1-nightSpawn-health", 20);
        defaults.put("IMPigManEngineer-T1-nightSpawn-health", 20);
        defaults.put("IMSkeleton-T1-nightSpawn-health", 20);
        defaults.put("IMSpider-T1-Spider-nightSpawn-health", 18);
        defaults.put("IMSpider-T1-Baby-Spider-nightSpawn-health", 3);
        defaults.put("IMSpider-T2-Jumping-Spider-nightSpawn-health", 18);
        defaults.put("IMSpider-T2-Mother-Spider-nightSpawn-health", 23);
        defaults.put("IMThrower-T1-nightSpawn-health", 50);
        defaults.put("IMThrower-T2-nightSpawn-health", 70);
        defaults.put("IMZombie-T1-nightSpawn-health", 20);
        defaults.put("IMZombie-T2-nightSpawn-health", 30);
        defaults.put("IMZombie-T3-nightSpawn-health", 65);
        defaults.put("IMZombiePigman-T1-nightSpawn-health", 20);
        defaults.put("IMZombiePigman-T2-nightSpawn-health", 30);
        defaults.put("IMZombiePigman-T3-nightSpawn-health", 65);
        return defaults;
    }

    public static final class General {
        private Boolean destructedBlocksDrop;
        private Boolean updateMessagesEnabled;
        private Boolean craftItemsEnabled;
        private Boolean debugMode;
        private Integer guiIdNexus;

        private static General createDefault() {
            General general = new General();
            general.destructedBlocksDrop = Boolean.TRUE;
            general.updateMessagesEnabled = Boolean.TRUE;
            general.craftItemsEnabled = Boolean.TRUE;
            general.debugMode = Boolean.FALSE;
            general.guiIdNexus = 76;
            return general;
        }

        private void applyFallbacks() {
            if (destructedBlocksDrop == null) {
                destructedBlocksDrop = Boolean.TRUE;
            }
            if (updateMessagesEnabled == null) {
                updateMessagesEnabled = Boolean.TRUE;
            }
            if (craftItemsEnabled == null) {
                craftItemsEnabled = Boolean.TRUE;
            }
            if (debugMode == null) {
                debugMode = Boolean.FALSE;
            }
            if (guiIdNexus == null) {
                guiIdNexus = 76;
            }
        }

        public boolean destructedBlocksDrop() {
            return Boolean.TRUE.equals(destructedBlocksDrop);
        }

        public boolean updateMessagesEnabled() {
            return Boolean.TRUE.equals(updateMessagesEnabled);
        }

        public boolean craftItemsEnabled() {
            return Boolean.TRUE.equals(craftItemsEnabled);
        }

        public boolean debugMode() {
            return Boolean.TRUE.equals(debugMode);
        }

        public int guiIdNexus() {
            return guiIdNexus == null ? 76 : guiIdNexus;
        }
    }

    public static final class ContinuousMode {
        private Integer minDaysToAttack;
        private Integer maxDaysToAttack;

        private static ContinuousMode createDefault() {
            ContinuousMode mode = new ContinuousMode();
            mode.minDaysToAttack = 2;
            mode.maxDaysToAttack = 3;
            return mode;
        }

        private void applyFallbacks() {
            if (minDaysToAttack == null) {
                minDaysToAttack = 2;
            }
            if (maxDaysToAttack == null) {
                maxDaysToAttack = 3;
            }
        }

        public int minDaysToAttack() {
            return minDaysToAttack == null ? 2 : minDaysToAttack;
        }

        public int maxDaysToAttack() {
            return maxDaysToAttack == null ? 3 : maxDaysToAttack;
        }
    }

    public static final class NightSpawns {
        private Boolean nightSpawnsEnabled;
        private Integer nightMobSightRange;
        private Integer nightMobSenseRange;
        private Integer nightMobSpawnChance;
        private Integer nightMobMaxGroupSize;
        private Integer mobLimitOverride;
        private Float nightMobStatsScaling;
        private Boolean nightMobsBurnInDay;

        private static NightSpawns createDefault() {
            NightSpawns night = new NightSpawns();
            night.nightSpawnsEnabled = Boolean.FALSE;
            night.nightMobSightRange = 20;
            night.nightMobSenseRange = 12;
            night.nightMobSpawnChance = 30;
            night.nightMobMaxGroupSize = 3;
            night.mobLimitOverride = 70;
            night.nightMobStatsScaling = 1.0F;
            night.nightMobsBurnInDay = Boolean.TRUE;
            return night;
        }

        private void applyFallbacks() {
            if (nightSpawnsEnabled == null) {
                nightSpawnsEnabled = Boolean.FALSE;
            }
            if (nightMobSightRange == null) {
                nightMobSightRange = 20;
            }
            if (nightMobSenseRange == null) {
                nightMobSenseRange = 12;
            }
            if (nightMobSpawnChance == null) {
                nightMobSpawnChance = 30;
            }
            if (nightMobMaxGroupSize == null) {
                nightMobMaxGroupSize = 3;
            }
            if (mobLimitOverride == null) {
                mobLimitOverride = 70;
            }
            if (nightMobStatsScaling == null) {
                nightMobStatsScaling = 1.0F;
            }
            if (nightMobsBurnInDay == null) {
                nightMobsBurnInDay = Boolean.TRUE;
            }
        }

        public boolean nightSpawnsEnabled() {
            return Boolean.TRUE.equals(nightSpawnsEnabled);
        }

        public int nightMobSightRange() {
            return nightMobSightRange == null ? 20 : nightMobSightRange;
        }

        public int nightMobSenseRange() {
            return nightMobSenseRange == null ? 12 : nightMobSenseRange;
        }

        public int nightMobSpawnChance() {
            return nightMobSpawnChance == null ? 30 : nightMobSpawnChance;
        }

        public int nightMobMaxGroupSize() {
            return nightMobMaxGroupSize == null ? 3 : nightMobMaxGroupSize;
        }

        public int mobLimitOverride() {
            return mobLimitOverride == null ? 70 : mobLimitOverride;
        }

        public float nightMobStatsScaling() {
            return nightMobStatsScaling == null ? 1.0F : nightMobStatsScaling;
        }

        public boolean nightMobsBurnInDay() {
            return Boolean.TRUE.equals(nightMobsBurnInDay);
        }
    }

    public static final class MobHealth {
        private Map<String, Integer> values;

        private static MobHealth createDefault(Map<String, Integer> defaults) {
            MobHealth health = new MobHealth();
            health.values = new LinkedHashMap<>(defaults);
            return health;
        }

        private void applyFallbacks(Map<String, Integer> defaults) {
            if (values == null) {
                values = new LinkedHashMap<>();
            }
            for (Map.Entry<String, Integer> entry : defaults.entrySet()) {
                values.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        public Map<String, Integer> values() {
            return values;
        }

        public int getOrDefault(String key, int defaultValue) {
            if (values == null) {
                return defaultValue;
            }
            return values.getOrDefault(key, defaultValue);
        }
    }

    public static final class NightSpawnPools {
        private List<NightSpawnEntry> pool1;

        private static NightSpawnPools createDefault() {
            NightSpawnPools pools = new NightSpawnPools();
            pools.pool1 = new ArrayList<>();
            String[] defaults = {
                    "zombie_t1_any",
                    "zombie_t2_any_basic",
                    "zombie_t2_plain",
                    "zombie_t2_tar",
                    "zombie_t2_pigman",
                    "zombie_t3_any",
                    "spider_t1_any",
                    "spider_t2_any",
                    "pigengy_t1_any",
                    "skeleton_t1_any",
                    "thrower_t1",
                    "thrower_t2",
                    "creeper_t1_basic",
                    "imp_t1"
            };
            float[] weights = {
                    1.0F,
                    1.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.5F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F
            };
            for (int i = 0; i < defaults.length; i++) {
                pools.pool1.add(new NightSpawnEntry(defaults[i], weights[i]));
            }
            return pools;
        }

        private void applyFallbacks() {
            if (pool1 == null) {
                pool1 = new ArrayList<>();
            }
            if (pool1.isEmpty()) {
                pool1.addAll(createDefault().pool1);
            } else {
                NightSpawnEntry[] defaults = createDefault().pool1.toArray(new NightSpawnEntry[0]);
                if (pool1.size() < defaults.length) {
                    for (int i = pool1.size(); i < defaults.length; i++) {
                        pool1.add(defaults[i]);
                    }
                }
                for (int i = 0; i < pool1.size(); i++) {
                    NightSpawnEntry entry = pool1.get(i);
                    if (entry == null || entry.pattern == null) {
                        pool1.set(i, defaults[Math.min(i, defaults.length - 1)]);
                    } else if (i < defaults.length && (Float.isNaN(entry.weight) || Float.isInfinite(entry.weight))) {
                        pool1.set(i, new NightSpawnEntry(entry.pattern, defaults[i].weight));
                    }
                }
            }
        }

        public List<NightSpawnEntry> pool1() {
            return pool1;
        }
    }

    public static final class NightSpawnEntry {
        private String pattern;
        private float weight;

        public NightSpawnEntry() {
        }

        private NightSpawnEntry(String pattern, float weight) {
            this.pattern = pattern;
            this.weight = weight;
        }

        public String pattern() {
            return pattern;
        }

        public float weight() {
            return weight;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s(%.2f)", Objects.toString(pattern, "none"), weight);
        }
    }
}
