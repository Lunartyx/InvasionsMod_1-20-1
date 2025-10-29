package com.lunartyx.invasionmod.command;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.nexus.AdaptiveWaveBuilder;
import com.lunartyx.invasionmod.nexus.WaveDefinition;
import com.lunartyx.invasionmod.nexus.WaveEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.Objects;

public class InvasionTester {
    private final AdaptiveWaveBuilder waveBuilder;
    private final Random random = Random.create();

    public InvasionTester() {
        this(new AdaptiveWaveBuilder());
    }

    public InvasionTester(AdaptiveWaveBuilder builder) {
        this.waveBuilder = Objects.requireNonNull(builder, "builder");
    }

    public void runSpawnerTest(int startWave, int endWave, boolean continuous) {
        int first = Math.min(startWave, endWave);
        int last = Math.max(startWave, endWave);
        InvasionMod.LOGGER.info("=== Invasion spawner diagnostics ({} -> {}, continuous: {}) ===", first, last, continuous);
        for (int wave = first; wave <= last; wave++) {
            AdaptiveWaveBuilder.Parameters parameters = waveBuilder.parametersForWave(wave, continuous);
            WaveDefinition definition = waveBuilder.build(random, parameters, continuous);
            int total = definition.totalMobCount();
            InvasionMod.LOGGER.info("Wave {} defines {} mobs across {} entries (difficulty {}, tier {}, length {}s)",
                    wave, total, definition.entries().size(),
                    String.format(java.util.Locale.ROOT, "%.2f", parameters.difficulty()),
                    String.format(java.util.Locale.ROOT, "%.2f", parameters.tierLevel()),
                    parameters.lengthSeconds());
            for (WaveEntry entry : definition.entries()) {
                InvasionMod.LOGGER.info(" -> {} x{} (delay {}-{} ticks, type {})", entry.entityId(), entry.amount(), entry.minDelay(), entry.maxDelay(), entry.spawnType());
            }
        }
    }

    public void runWaveBuilderTest(float difficulty, float tierLevel, int lengthSeconds) {
        int baseWave = Math.max(1, MathHelper.floor(difficulty * 5.0F + tierLevel * 3.0F));
        int simulatedDuration = Math.max(60, lengthSeconds);
        InvasionMod.LOGGER.info("=== Invasion wave builder diagnostics ===");
        InvasionMod.LOGGER.info("Inputs -> difficulty: {}, tier: {}, length: {}s", difficulty, tierLevel, lengthSeconds);
        AdaptiveWaveBuilder.Parameters parameters = AdaptiveWaveBuilder.Parameters.of(difficulty, tierLevel, simulatedDuration).withWaveNumber(baseWave);
        WaveDefinition invasion = waveBuilder.build(Random.create(baseWave * 341873128712L), parameters, false);
        WaveDefinition continuous = waveBuilder.build(Random.create((baseWave * 341873128712L) ^ 0x5DEECE66DL), parameters, true);
        InvasionMod.LOGGER.info("Selected base wave {} ({} total mobs, {} entries)", baseWave, invasion.totalMobCount(), invasion.entries().size());
        InvasionMod.LOGGER.info("Simulated duration {} ticks -> approx {} mobs per minute", simulatedDuration * 20, MathHelper.floor((float) invasion.totalMobCount() / (simulatedDuration / 60.0F)));
        logEntryComparison("Invasion", invasion);
        logEntryComparison("Continuous", continuous);
    }

    public void runSpawnPointSelectionTest(int radius, int samples) {
        int clampedRadius = MathHelper.clamp(radius, 8, 196);
        int totalSamples = MathHelper.clamp(samples, 8, 512);
        InvasionMod.LOGGER.info("=== Invasion spawn point diagnostics ===");
        InvasionMod.LOGGER.info("Sampling {} spawn candidates with radius {}", totalSamples, clampedRadius);
        for (int i = 0; i < totalSamples; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = random.nextBetween(Math.max(6, clampedRadius - 8), clampedRadius + 8);
            int x = MathHelper.floor(Math.cos(angle) * distance);
            int z = MathHelper.floor(Math.sin(angle) * distance);
            InvasionMod.LOGGER.info("Sample {} -> angle {}, offset ({}, {})", i + 1, String.format(java.util.Locale.ROOT, "%.2f°", Math.toDegrees(angle)), x, z);
        }
    }

    private void logEntryComparison(String label, WaveDefinition definition) {
        InvasionMod.LOGGER.info("{} wave summary:", label);
        for (WaveEntry entry : definition.entries()) {
            InvasionMod.LOGGER.info("  {} -> {} mobs, {}-{} tick delay, spawn type {}", entry.entityId(), entry.amount(), entry.minDelay(), entry.maxDelay(), entry.spawnType());
        }
    }
}
