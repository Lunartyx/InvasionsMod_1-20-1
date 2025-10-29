package com.lunartyx.invasionmod.nexus;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the timing and mob makeup of a single invasion wave. Each wave
 * supplies a curated set of {@link WaveEntry} instances plus a rest period
 * between waves so the {@link InvasionManager} can recreate the pacing of the
 * original mod while remaining deterministic across server reloads.
 */
public final class WaveDefinition {
    private final int waveNumber;
    private final List<WaveEntry> entries;
    private final int breakTicks;
    private final Map<Identifier, WaveEntry> entryLookup;
    private final int totalMobCount;

    public WaveDefinition(int waveNumber, List<WaveEntry> entries, int breakTicks) {
        this.waveNumber = waveNumber;
        this.entries = List.copyOf(entries);
        this.breakTicks = Math.max(0, breakTicks);

        Map<Identifier, WaveEntry> lookup = new HashMap<>();
        int total = 0;
        for (WaveEntry entry : this.entries) {
            lookup.put(entry.entityId(), entry);
            total += Math.max(0, entry.amount());
        }
        this.entryLookup = Collections.unmodifiableMap(lookup);
        this.totalMobCount = total;
    }

    public int waveNumber() {
        return waveNumber;
    }

    public List<WaveEntry> entries() {
        return entries;
    }

    public int breakTicks() {
        return breakTicks;
    }

    public Map<Identifier, WaveEntry> entryLookup() {
        return entryLookup;
    }

    public int totalMobCount() {
        return totalMobCount;
    }
}
