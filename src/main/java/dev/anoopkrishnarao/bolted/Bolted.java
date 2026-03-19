package dev.anoopkrishnarao.bolted;

import dev.anoopkrishnarao.bolted.config.BoltedConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bolted implements ModInitializer {

    public static final String MOD_ID = "bolted";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static BoltedConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = BoltedConfig.loadOrCreate();
        LOGGER.info("[Bolted] Initialized. Lock and load.");
    }
}