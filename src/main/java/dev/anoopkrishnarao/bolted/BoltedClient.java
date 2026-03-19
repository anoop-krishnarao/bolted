package dev.anoopkrishnarao.bolted;

import dev.anoopkrishnarao.bolted.keybind.BoltedKeybinds;
import dev.anoopkrishnarao.bolted.network.LockPacketClient;
import dev.anoopkrishnarao.bolted.render.BoltedRenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BoltedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BoltedKeybinds.register();
        BoltedRenderEvents.register();
        registerTickHandler();
        Bolted.LOGGER.info("[Bolted] Client initialized.");
    }

    private void registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (BoltedKeybinds.LOCK_KEY.consumeClick()) {
                if (client.player == null || client.level == null) return;

                HitResult hit = client.hitResult;
                if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

                BlockHitResult blockHit = (BlockHitResult) hit;
                LockPacketClient.send(blockHit.getBlockPos());
            }
        });
    }
}