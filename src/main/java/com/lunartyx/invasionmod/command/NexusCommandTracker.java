package com.lunartyx.invasionmod.command;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NexusCommandTracker {
    private static final Map<UUID, FocusedNexus> FOCUSED_NEXUS = new ConcurrentHashMap<>();

    private NexusCommandTracker() {
    }

    public static void setFocus(ServerPlayerEntity player, BlockPos pos) {
        if (player == null || pos == null) {
            return;
        }
        FOCUSED_NEXUS.put(player.getUuid(), new FocusedNexus(player.getWorld().getRegistryKey(), pos.toImmutable()));
    }

    public static Optional<FocusedNexus> getFocus(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(FOCUSED_NEXUS.get(playerId));
    }

    public static void clearFocus(UUID playerId) {
        if (playerId != null) {
            FOCUSED_NEXUS.remove(playerId);
        }
    }

    public record FocusedNexus(RegistryKey<World> dimension, BlockPos pos) {
    }
}
