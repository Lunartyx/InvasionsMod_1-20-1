package com.lunartyx.invasionmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.lunartyx.invasionmod.InvasionMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Thin wrapper around the {@link InvasionConfig} payload that loads from the
 * Fabric config directory and exposes the active settings to both logical
 * sides.
 */
public final class InvasionConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static InvasionConfig config = InvasionConfig.createDefault();
    private static Path configPath;
    private static boolean initialised;

    private InvasionConfigManager() {
    }

    public static synchronized void initialize() {
        if (initialised) {
            return;
        }

        Path configDir = FabricLoader.getInstance().getConfigDir();
        configPath = configDir.resolve("invasionmod.json");
        config = load();
        save();
        initialised = true;
    }

    public static synchronized void save() {
        if (configPath == null) {
            return;
        }
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            InvasionMod.LOGGER.error("Failed to save Invasion config", e);
        }
    }

    public static InvasionConfig getConfig() {
        return config;
    }

    private static InvasionConfig load() {
        InvasionConfig defaults = InvasionConfig.createDefault();
        if (configPath == null) {
            defaults.applyFallbacks();
            return defaults;
        }

        if (!Files.exists(configPath)) {
            defaults.applyFallbacks();
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            InvasionConfig loaded = GSON.fromJson(reader, InvasionConfig.class);
            if (loaded == null) {
                defaults.applyFallbacks();
                return defaults;
            }
            loaded.applyFallbacks();
            InvasionMod.LOGGER.info("Loaded Invasion config from {}", configPath.toAbsolutePath());
            return loaded;
        } catch (IOException | JsonParseException e) {
            InvasionMod.LOGGER.error("Failed to read Invasion config, falling back to defaults", e);
            defaults.applyFallbacks();
            return defaults;
        }
    }
}
