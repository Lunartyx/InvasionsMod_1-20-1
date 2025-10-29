package com.lunartyx.invasionmod.client.network;

import com.lunartyx.invasionmod.network.InvasionNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class InvasionClientNetwork {
    private InvasionClientNetwork() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(InvasionNetwork.NEXUS_EFFECT, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            Identifier soundId = buf.readIdentifier();
            SoundCategory category = buf.readEnumConstant(SoundCategory.class);
            float volume = buf.readFloat();
            float pitch = buf.readFloat();
            client.execute(() -> {
                ClientWorld world = client.world;
                if (world == null) {
                    return;
                }
                SoundEvent sound = Registries.SOUND_EVENT.get(soundId);
                if (sound == null) {
                    sound = SoundEvent.of(soundId);
                }
                double x = pos.getX() + 0.5D;
                double y = pos.getY() + 0.5D;
                double z = pos.getZ() + 0.5D;
                world.playSound(x, y, z, sound, category, volume, pitch, false);
            });
        });
    }
}
