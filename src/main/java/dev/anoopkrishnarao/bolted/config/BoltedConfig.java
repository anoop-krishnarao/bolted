package dev.anoopkrishnarao.bolted.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.anoopkrishnarao.bolted.Bolted;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class BoltedConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "bolted.json";

    // --- Config fields ---
    public boolean lockSound = true;
    public boolean showIndicator = true;
    public boolean showActionBarMessage = true;
    public boolean lockedBlocksDisableRedstone = true;
    public boolean entityLockedDisableRedstone = false;

    // --- Load or create ---
    public static BoltedConfig loadOrCreate() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);

        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                BoltedConfig loaded = GSON.fromJson(reader, BoltedConfig.class);
                if (loaded != null) {
                    Bolted.LOGGER.info("[Bolted] Config loaded from {}", configPath);
                    return loaded;
                }
            } catch (Exception e) {
                Bolted.LOGGER.warn("[Bolted] Failed to read config, regenerating. Cause: {}", e.getMessage());
            }
        }

        // Write defaults
        BoltedConfig defaults = new BoltedConfig();
        defaults.save(configPath);
        return defaults;
    }

    public void save() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        save(configPath);
    }

    private void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
                Bolted.LOGGER.info("[Bolted] Config saved to {}", path);
            }
        } catch (IOException e) {
            Bolted.LOGGER.error("[Bolted] Failed to save config: {}", e.getMessage());
        }
    }
}