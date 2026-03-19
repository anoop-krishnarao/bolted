package dev.anoopkrishnarao.bolted.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public class BoltedRenderEvents {

    public static void register() {
        WorldRenderEvents.END_MAIN.register(BoltedRenderEvents::onWorldRenderEnd);
    }

    private static void onWorldRenderEnd(WorldRenderContext context) {
        LockIndicatorRenderer.render(
                context.matrices(),
                context.consumers()
        );
    }
}