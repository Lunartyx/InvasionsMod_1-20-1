package com.lunartyx.invasionmod.entity;

import net.minecraft.util.math.BlockPos;

/**
 * Marks mobs that should track the Nexus that spawned them. The invasion
 * manager tags all spawned mobs so they path back to the structure when they do
 * not have an active target, mirroring the behaviour of the legacy AI stack.
 */
public interface NexusBoundMob {
    void invasionmod$setNexus(BlockPos pos);

    BlockPos invasionmod$getNexus();
}
