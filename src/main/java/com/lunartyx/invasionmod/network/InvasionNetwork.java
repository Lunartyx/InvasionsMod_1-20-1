package com.lunartyx.invasionmod.network;

import com.lunartyx.invasionmod.InvasionMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Common networking utilities. Only server-side helpers live here; client
 * receivers are registered from {@code InvasionClientNetwork} inside the client
 * initializer to avoid loading client-only classes on a dedicated server.
 */
public final class InvasionNetwork {
    public static final Identifier NEXUS_EFFECT = new Identifier(InvasionMod.MOD_ID, "nexus_effect");

    private InvasionNetwork() {
    }

    public static void init() {
        // Reserved for future server-bound packet registration.
    }

    public static void sendNexusSound(ServerPlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category,
                                       float volume, float pitch) {
        if (player == null || sound == null || pos == null) {
            return;
        }
        Identifier id = Registries.SOUND_EVENT.getId(sound);
        if (id == null) {
            return;
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeIdentifier(id);
        buf.writeEnumConstant(category);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        ServerPlayNetworking.send(player, NEXUS_EFFECT, buf);
    }
}
