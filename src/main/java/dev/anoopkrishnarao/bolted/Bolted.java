package dev.anoopkrishnarao.bolted;

import dev.anoopkrishnarao.bolted.config.BoltedConfig;
import dev.anoopkrishnarao.bolted.lock.LockStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.anoopkrishnarao.bolted.network.LockPacket;

public class Bolted implements ModInitializer {

    public static final String MOD_ID = "bolted";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static BoltedConfig CONFIG;
    public static LockStorage LOCK_STORAGE;

    @Override
    public void onInitialize() {
        CONFIG = BoltedConfig.loadOrCreate();
        LockPacket.register();  // ← add this line

        ServerWorldEvents.LOAD.register((MinecraftServer server, ServerLevel world) -> {
            if (LOCK_STORAGE == null) {
                String worldId = server.getWorldData().getLevelName();
                LOCK_STORAGE = new LockStorage(server.getServerDirectory(), worldId);
                LOCK_STORAGE.load();
                LOGGER.info("[Bolted] LockStorage initialized for world: {}", worldId);
            }
        });

        ServerWorldEvents.UNLOAD.register((MinecraftServer server, ServerLevel world) -> {
            if (LOCK_STORAGE != null) {
                LOCK_STORAGE.save();
                LOCK_STORAGE = null;
                LOGGER.info("[Bolted] LockStorage saved and cleared.");
            }
        });

        LOGGER.info("[Bolted] Initialized. Lock and load.");
    }
}