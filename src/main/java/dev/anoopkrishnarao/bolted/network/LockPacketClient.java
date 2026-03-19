package dev.anoopkrishnarao.bolted.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;

public class LockPacketClient {

    public static void send(BlockPos pos) {
        ClientPlayNetworking.send(new LockPacket(pos));
    }
}