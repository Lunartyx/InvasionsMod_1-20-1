package com.lunartyx.invasionmod.nexus;

import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Port of the legacy {@code IMWaveBuilder}. The adaptive builder exposes the
 * classic difficulty/tier inputs while emitting {@link WaveDefinition}
 * instances that the Fabric invasion manager can consume. The implementation
 * keeps a registry of mob patterns so newly ported entities can hook into the
 * wave logic without rewriting the builder.
 */
public final class AdaptiveWaveBuilder {
    private static final float GROUP_POOL_WEIGHT = 0.8333333F;
    private static final float SPECIAL_POOL_WEIGHT = 0.1666667F;

    private final Map<String, MobPattern> patternRegistry = new HashMap<>();

    public AdaptiveWaveBuilder() {
        registerDefaultPatterns();
    }

    /**
     * Calculates tuned parameters for the requested wave number. The values are
     * based on the pacing of the original mod and scale difficulty, tier, and
     * simulated duration as waves progress.
     */
    public Parameters parametersForWave(int waveNumber, boolean continuous) {
        int wave = Math.max(1, waveNumber);
        float difficulty = Math.max(0.8F, 1.0F + (wave - 1) * 0.17F);
        float tierLevel = MathHelper.clamp(0.9F + (wave - 1) * 0.22F, 0.9F, 4.5F);
        int baseLength = MathHelper.clamp(140 + wave * 10, 140, 360);
        int lengthSeconds = continuous ? Math.max(100, baseLength - 20) : baseLength;
        return new Parameters(difficulty, tierLevel, lengthSeconds, wave);
    }

    /**
     * Builds a wave using a deterministic seed.
     */
    public WaveDefinition build(long seed, Parameters parameters, boolean continuous) {
        return build(Random.create(seed), parameters, continuous);
    }

    /**
     * Builds a wave using the supplied random instance.
     */
    public WaveDefinition build(Random random, Parameters parameters, boolean continuous) {
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(parameters, "parameters");

        float difficulty = Math.max(0.1F, parameters.difficulty());
        float tierLevel = Math.max(0.1F, parameters.tierLevel());
        int lengthSeconds = Math.max(60, parameters.lengthSeconds());

        float mobsPerSecond = 0.12F * difficulty;
        int numberOfGroups = 7;
        int numberOfBigGroups = 1;
        float proportionInGroups = 0.5F;

        int mobsPerGroup = Math.max(1, Math.round(proportionInGroups * mobsPerSecond * lengthSeconds / (numberOfGroups + numberOfBigGroups * 2)));
        int mobsPerBigGroup = Math.max(1, mobsPerGroup * 2);
        int totalTarget = Math.max(1, Math.round(mobsPerSecond * lengthSeconds));
        int remainingMobs = Math.max(0, totalTarget - mobsPerGroup * numberOfGroups - mobsPerBigGroup * numberOfBigGroups);
        int mobsPerSteady = Math.max(0, Math.round(0.7F * remainingMobs / numberOfGroups));
        int extraMobsForFinale = Math.max(0, Math.round(0.3F * remainingMobs));
        int extraMobsForCleanup = Math.max(0, Math.round(totalTarget * 0.2F));

        WeightedPool<MobPattern> groupPool = createGroupPool(tierLevel);
        WeightedPool<MobPattern> steadyPool = createSteadyPool(tierLevel);
        WeightedPool<MobPattern> finalePool = createGroupPool(tierLevel + 0.5F);

        DelayRange groupDelay = DelayRange.fromGranularity(500);
        DelayRange steadyDelay = DelayRange.fromGranularity(2000);
        DelayRange finaleDelay = DelayRange.fromGranularity(500);

        List<WaveEntry> entries = new ArrayList<>();

        for (int i = 0; i < numberOfGroups; i++) {
            boolean groupFirst = random.nextBoolean();
            if (groupFirst) {
                appendEntries(entries, sampleCounts(groupPool, mobsPerGroup, random), groupDelay);
                appendEntries(entries, sampleCounts(steadyPool, mobsPerSteady, random), steadyDelay);
            } else {
                appendEntries(entries, sampleCounts(steadyPool, mobsPerSteady, random), steadyDelay);
                appendEntries(entries, sampleCounts(groupPool, mobsPerGroup, random), groupDelay);
            }
        }

        Map<MobPattern, Integer> finaleCounts = new LinkedHashMap<>();
        MobPattern thrower = patternRegistry.get("thrower_t1");
        if (thrower != null) {
            finaleCounts.put(thrower, Math.max(1, mobsPerBigGroup / 5));
        }
        sampleInto(finaleCounts, finalePool, mobsPerBigGroup, random);
        appendEntries(entries, finaleCounts, finaleDelay);

        int finaleExtras = Math.max(0, extraMobsForFinale / 2);
        for (int i = 0; i < 3; i++) {
            appendEntries(entries, sampleCounts(steadyPool, finaleExtras, random), finaleDelay);
        }

        appendEntries(entries, sampleCounts(steadyPool, extraMobsForCleanup, random), finaleDelay);

        int baseBreak = continuous ? 160 : 240;
        int reductionPerWave = continuous ? 4 : 8;
        int minimumBreak = continuous ? 60 : 140;
        int breakTicks = Math.max(minimumBreak, baseBreak - reductionPerWave * (parameters.waveNumber() - 1));

        return new WaveDefinition(parameters.waveNumber(), entries, breakTicks);
    }

    /**
     * Registers or replaces a pattern in the wave builder.
     */
    public void registerPattern(String key, MobPattern pattern) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Pattern key cannot be empty");
        }
        patternRegistry.put(key, Objects.requireNonNull(pattern, "pattern"));
    }

    private void registerDefaultPatterns() {
        registerPattern("zombie_t1_any", new MobPattern(ModEntityTypes.RIFT_ZOMBIE, SpawnType.GROUND, 0));
        registerPattern("zombie_t2_any_basic", new MobPattern(ModEntityTypes.RIFT_ZOMBIE, SpawnType.GROUND, 4));
        registerPattern("zombie_t2_pigman", new MobPattern(ModEntityTypes.RIFT_ZOMBIE_PIGLIN, SpawnType.GROUND, 12));
        registerPattern("spider_t1_any", new MobPattern(ModEntityTypes.RIFT_SPIDER, SpawnType.GROUND, 6));
        registerPattern("spider_t2_any", new MobPattern(ModEntityTypes.RIFT_SPIDER, SpawnType.GROUND, 8));
        registerPattern("skeleton_t1_any", new MobPattern(ModEntityTypes.RIFT_SKELETON, SpawnType.GROUND, 6));
        registerPattern("pigengy_t1_any", new MobPattern(ModEntityTypes.RIFT_PIG_ENGINEER, SpawnType.GROUND, 14));
        registerPattern("thrower_t1", new MobPattern(ModEntityTypes.RIFT_THROWER, SpawnType.GROUND, 10));
        registerPattern("zombie_t3_any", new MobPattern(ModEntityTypes.RIFT_ZOMBIE_PIGLIN, SpawnType.GROUND, 18));
        registerPattern("creeper_t1_basic", new MobPattern(ModEntityTypes.RIFT_CREEPER, SpawnType.GROUND, 12));
    }

    private WeightedPool<MobPattern> createGroupPool(float tierLevel) {
        float[] weights = tierWeights(tierLevel);

        WeightedPool<MobPattern> zombiePool = new WeightedPool<>();
        zombiePool.add(patternRegistry.get("zombie_t1_any"), 1.0F * weights[0]);
        zombiePool.add(patternRegistry.get("zombie_t2_any_basic"), 2.0F * weights[2]);
        zombiePool.add(patternRegistry.get("zombie_t2_pigman"), 1.0F * weights[3]);

        WeightedPool<MobPattern> spiderPool = new WeightedPool<>();
        spiderPool.add(patternRegistry.get("spider_t1_any"), 1.0F * weights[0]);
        spiderPool.add(patternRegistry.get("spider_t2_any"), 2.0F * weights[2]);

        WeightedPool<MobPattern> basicPool = new WeightedPool<>();
        basicPool.addFrom(zombiePool, 3.1F);
        basicPool.addFrom(spiderPool, 0.7F);
        basicPool.add(patternRegistry.get("skeleton_t1_any"), 0.8F);

        WeightedPool<MobPattern> specialPool = new WeightedPool<>();
        specialPool.add(patternRegistry.get("pigengy_t1_any"), 4.0F);
        specialPool.add(patternRegistry.get("thrower_t1"), 1.1F * weights[4]);
        specialPool.add(patternRegistry.get("zombie_t3_any"), 1.1F * weights[5]);
        specialPool.add(patternRegistry.get("creeper_t1_basic"), 0.7F * weights[3]);

        WeightedPool<MobPattern> pool = new WeightedPool<>();
        pool.addFrom(basicPool, GROUP_POOL_WEIGHT * 6.0F);
        pool.addFrom(specialPool, SPECIAL_POOL_WEIGHT * 6.0F);
        return pool;
    }

    private WeightedPool<MobPattern> createSteadyPool(float tierLevel) {
        float[] weights = tierWeights(tierLevel);

        WeightedPool<MobPattern> zombiePool = new WeightedPool<>();
        zombiePool.add(patternRegistry.get("zombie_t1_any"), 1.0F * weights[0]);
        zombiePool.add(patternRegistry.get("zombie_t2_any_basic"), 2.0F * weights[2]);
        zombiePool.add(patternRegistry.get("zombie_t2_pigman"), 1.0F * weights[3]);

        WeightedPool<MobPattern> spiderPool = new WeightedPool<>();
        spiderRegistryFallback(spiderPool, weights);

        WeightedPool<MobPattern> basicPool = new WeightedPool<>();
        basicPool.addFrom(zombiePool, 3.1F);
        basicPool.addFrom(spiderPool, 0.7F);
        basicPool.add(patternRegistry.get("skeleton_t1_any"), 0.8F);

        WeightedPool<MobPattern> specialPool = new WeightedPool<>();
        specialPool.add(patternRegistry.get("pigengy_t1_any"), 3.0F);
        specialPool.add(patternRegistry.get("zombie_t3_any"), 1.1F * weights[5]);
        specialPool.add(patternRegistry.get("creeper_t1_basic"), 0.8F * weights[3]);

        WeightedPool<MobPattern> pool = new WeightedPool<>();
        pool.addFrom(basicPool, 9.0F);
        pool.addFrom(specialPool, 1.0F);
        return pool;
    }

    private void spiderRegistryFallback(WeightedPool<MobPattern> spiderPool, float[] weights) {
        spiderPool.add(patternRegistry.get("spider_t1_any"), 1.0F * weights[0]);
        spiderPool.add(patternRegistry.get("spider_t2_any"), 2.0F * weights[2]);
    }

    private float[] tierWeights(float tierLevel) {
        float[] weights = new float[6];
        for (int i = 0; i < weights.length; i++) {
            float value = tierLevel - i * 0.5F;
            if (value > 0.0F) {
                float capCheck = tierLevel - i;
                weights[i] = capCheck <= 1.0F ? value : 1.0F;
            }
        }
        return weights;
    }

    private Map<MobPattern, Integer> sampleCounts(WeightedPool<MobPattern> pool, int amount, Random random) {
        Map<MobPattern, Integer> counts = new LinkedHashMap<>();
        sampleInto(counts, pool, amount, random);
        return counts;
    }

    private void sampleInto(Map<MobPattern, Integer> counts, WeightedPool<MobPattern> pool, int amount, Random random) {
        if (amount <= 0 || pool.isEmpty()) {
            return;
        }
        for (int i = 0; i < amount; i++) {
            MobPattern pattern = pool.next(random);
            if (pattern != null) {
                counts.merge(pattern, 1, Integer::sum);
            }
        }
    }

    private void appendEntries(List<WaveEntry> entries, Map<MobPattern, Integer> counts, DelayRange baseDelay) {
        if (counts.isEmpty()) {
            return;
        }
        for (Map.Entry<MobPattern, Integer> entry : counts.entrySet()) {
            MobPattern pattern = entry.getKey();
            int amount = entry.getValue();
            if (pattern == null || amount <= 0) {
                continue;
            }
            DelayRange delay = baseDelay.offset(pattern.delayOffset());
            entries.add(new WaveEntry(pattern.entityType(), pattern.spawnType(), amount, delay.min(), delay.max()));
        }
    }

    /**
     * Holds the parameter set used to construct a wave.
     */
    public record Parameters(float difficulty, float tierLevel, int lengthSeconds, int waveNumber) {
        public Parameters {
            difficulty = Math.max(0.1F, difficulty);
            tierLevel = Math.max(0.1F, tierLevel);
            lengthSeconds = Math.max(60, lengthSeconds);
            waveNumber = Math.max(1, waveNumber);
        }

        public static Parameters of(float difficulty, float tierLevel, int lengthSeconds) {
            int wave = Math.max(1, MathHelper.floor(difficulty * 5.0F + tierLevel * 3.0F));
            return new Parameters(difficulty, tierLevel, lengthSeconds, wave);
        }

        public Parameters withWaveNumber(int wave) {
            return new Parameters(difficulty, tierLevel, lengthSeconds, wave);
        }
    }

    /**
     * Basic mob pattern information mirrored from the legacy registry.
     */
    public record MobPattern(EntityType<? extends MobEntity> entityType, SpawnType spawnType, int delayOffset) {
        public MobPattern {
            Objects.requireNonNull(entityType, "entityType");
            Objects.requireNonNull(spawnType, "spawnType");
        }

        public Identifier id() {
            return EntityType.getId(entityType);
        }
    }

    private static final class WeightedPool<T> {
        private final List<Entry<T>> entries = new ArrayList<>();
        private float totalWeight;

        void add(T value, float weight) {
            if (value == null || weight <= 0.0F) {
                return;
            }
            entries.add(new Entry<>(value, weight));
            totalWeight += weight;
        }

        void addFrom(WeightedPool<T> other, float multiplier) {
            if (other == null || multiplier <= 0.0F) {
                return;
            }
            for (Entry<T> entry : other.entries) {
                add(entry.value(), entry.weight() * multiplier);
            }
        }

        boolean isEmpty() {
            return entries.isEmpty();
        }

        T next(Random random) {
            if (entries.isEmpty()) {
                return null;
            }
            float total = totalWeight;
            if (total <= 0.0F) {
                return entries.get(random.nextInt(entries.size())).value();
            }
            float choice = random.nextFloat() * total;
            float cumulative = 0.0F;
            for (Entry<T> entry : entries) {
                cumulative += entry.weight();
                if (choice <= cumulative) {
                    return entry.value();
                }
            }
            return entries.get(entries.size() - 1).value();
        }

        private record Entry<T>(T value, float weight) {
        }
    }

    private record DelayRange(int min, int max) {
        DelayRange {
            int lower = Math.max(1, min);
            int upper = Math.max(lower, max);
            min = lower;
            max = upper;
        }

        static DelayRange fromGranularity(int milliseconds) {
            int base = Math.max(1, Math.round(milliseconds / 50.0F));
            int offset = Math.max(1, base / 2);
            return new DelayRange(Math.max(1, base - offset), base + offset);
        }

        DelayRange offset(int extra) {
            int adjustedMin = Math.max(1, min + extra);
            int adjustedMax = Math.max(adjustedMin, max + extra);
            return new DelayRange(adjustedMin, adjustedMax);
        }
    }
}
