package dev.anoopkrishnarao.bolted.network;

import dev.anoopkrishnarao.bolted.Bolted;
import dev.anoopkrishnarao.bolted.lock.LockManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class LockPacket implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Bolted.MOD_ID, "lock_block");
    public static final CustomPacketPayload.Type<LockPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, LockPacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBlockPos(packet.pos),
            buf -> new LockPacket(buf.readBlockPos())
    );

    private final BlockPos pos;

    public LockPacket(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            context.server().execute(() -> {
                LockManager.onLockKeyPressed(
                        context.player(),
                        context.player().level(),
                        payload.getPos()
                );
            });
        });
    }
}