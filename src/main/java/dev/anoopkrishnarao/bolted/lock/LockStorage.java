package dev.anoopkrishnarao.bolted.lock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.anoopkrishnarao.bolted.Bolted;
import net.minecraft.core.BlockPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LockStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, LockEntry>>() {}.getType();

    // Key format: "dimension:x,y,z"  e.g. "minecraft:overworld:100,64,-200"
    private final Map<String, LockEntry> locks = new HashMap<>();
    private final Path savePath;

    public LockStorage(Path worldDir, String worldId) {
        this.savePath = worldDir
                .resolve(".minecraft")
                .resolve("bolted")
                .resolve(worldId)
                .resolve("locks.json");
    }

    // --- Key helpers ---

    private static String makeKey(String dimension, BlockPos pos) {
        return dimension + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    // --- Public API ---

    public LockEntry get(String dimension, BlockPos pos) {
        return locks.get(makeKey(dimension, pos));
    }

    public LockState getState(String dimension, BlockPos pos) {
        LockEntry entry = get(dimension, pos);
        return entry != null ? entry.state : LockState.UNLOCKED;
    }

    public void set(String dimension, BlockPos pos, LockState state, UUID ownerUuid) {
        if (state == LockState.UNLOCKED) {
            locks.remove(makeKey(dimension, pos));
        } else {
            locks.put(makeKey(dimension, pos), new LockEntry(state, ownerUuid));
        }
        save();
    }

    public void remove(String dimension, BlockPos pos) {
        locks.remove(makeKey(dimension, pos));
        save();
    }

    public boolean isOwner(String dimension, BlockPos pos, UUID playerUuid) {
        LockEntry entry = get(dimension, pos);
        if (entry == null) return true; // no lock = anyone can interact
        return entry.isOwner(playerUuid);
    }

    // --- Persistence ---

    public void load() {
        if (!Files.exists(savePath)) {
            Bolted.LOGGER.info("[Bolted] No lock data found at {}, starting fresh.", savePath);
            return;
        }
        try (Reader reader = Files.newBufferedReader(savePath)) {
            Map<String, LockEntry> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) {
                locks.clear();
                locks.putAll(loaded);
                Bolted.LOGGER.info("[Bolted] Loaded {} lock entries from {}", locks.size(), savePath);
            }
        } catch (Exception e) {
            Bolted.LOGGER.error("[Bolted] Failed to load lock data: {}", e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                GSON.toJson(locks, writer);
            }
        } catch (IOException e) {
            Bolted.LOGGER.error("[Bolted] Failed to save lock data: {}", e.getMessage());
        }
    }

    public void clear() {
        locks.clear();
    }
}