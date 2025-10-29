package com.lunartyx.invasionmod.nexus;

/**
 * Describes the preferred spawn medium for a wave entry. The current Fabric
 * port only differentiates between ground and air spawns but the enum mirrors
 * the legacy structure so bespoke behaviours can be added when custom mobs are
 * ported.
 */
public enum SpawnType {
    GROUND,
    AIR
}
