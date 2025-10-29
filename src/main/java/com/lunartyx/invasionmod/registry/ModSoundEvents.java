package com.lunartyx.invasionmod.registry;

import com.lunartyx.invasionmod.InvasionMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSoundEvents {
    public static final SoundEvent BIG_ZOMBIE_1 = register("bigzombie1");
    public static final SoundEvent CHIME_1 = register("chime1");
    public static final SoundEvent EGG_HATCH_1 = register("egghatch1");
    public static final SoundEvent EGG_HATCH_2 = register("egghatch2");
    public static final SoundEvent FIREBALL_1 = register("fireball1");
    public static final SoundEvent RUMBLE_1 = register("rumble1");
    public static final SoundEvent SCRAPE_1 = register("scrape1");
    public static final SoundEvent SCRAPE_2 = register("scrape2");
    public static final SoundEvent SCRAPE_3 = register("scrape3");
    public static final SoundEvent V_DEATH_1 = register("v_death1");
    public static final SoundEvent V_HISS_1 = register("v_hiss1");
    public static final SoundEvent V_LONGSCREECH_1 = register("v_longscreech1");
    public static final SoundEvent V_SCREECH_1 = register("v_screech1");
    public static final SoundEvent V_SCREECH_2 = register("v_screech2");
    public static final SoundEvent V_SCREECH_3 = register("v_screech3");
    public static final SoundEvent V_SQUAWK_1 = register("v_squawk1");
    public static final SoundEvent V_SQUAWK_2 = register("v_squawk2");
    public static final SoundEvent V_SQUAWK_3 = register("v_squawk3");
    public static final SoundEvent V_SQUAWK_4 = register("v_squawk4");
    public static final SoundEvent ZAP_1 = register("zap1");
    public static final SoundEvent ZAP_2 = register("zap2");
    public static final SoundEvent ZAP_3 = register("zap3");

    private ModSoundEvents() {
    }

    private static SoundEvent register(String name) {
        Identifier id = new Identifier(InvasionMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSoundEvents() {
        InvasionMod.LOGGER.info("Registering sound events for {}", InvasionMod.MOD_ID);
    }
}
