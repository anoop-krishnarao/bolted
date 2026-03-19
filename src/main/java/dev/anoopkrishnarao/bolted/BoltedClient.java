package dev.anoopkrishnarao.bolted;

import net.fabricmc.api.ClientModInitializer;

public class BoltedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Bolted.LOGGER.info("[Bolted] Client initialized.");
        // Keybinds and rendering registered in later milestones
    }
}