package com.lunartyx.invasionmod.nexus;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

/**
 * Immutable description of a mob batch that should spawn over the course of a
 * wave. The entry mirrors the data exposed by the classic Forge implementation
 * and adds simple Fabric-friendly helpers so the invasion manager can serialise
 * and resume wave progress.
 */
public record WaveEntry(Identifier entityId,
                        EntityType<? extends MobEntity> entityType,
                        SpawnType spawnType,
                        int amount,
                        int minDelay,
                        int maxDelay) {

    public WaveEntry(EntityType<? extends MobEntity> entityType,
                     SpawnType spawnType,
                     int amount,
                     int minDelay,
                     int maxDelay) {
        this(Registries.ENTITY_TYPE.getId(entityType), entityType, spawnType, amount, minDelay, maxDelay);
    }

    public int nextDelay(Random random) {
        int lower = Math.max(1, minDelay);
        int upper = Math.max(lower, maxDelay);
        return random.nextBetween(lower, upper);
    }

    public boolean matches(Identifier identifier) {
        return entityId.equals(identifier);
    }
}
