package dev.anoopkrishnarao.bolted.lock;

import dev.anoopkrishnarao.bolted.Bolted;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.UUID;

public class LockManager {

    public static void onLockKeyPressed(Player player, Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) return;

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DoorBlock)) return;

        BlockPos targetPos = getCanonicalDoorPos(state, pos);
        String dimension = level.dimension().identifier().toString();
        UUID playerUuid = player.getUUID();

        LockStorage storage = Bolted.LOCK_STORAGE;
        LockEntry existing = storage.get(dimension, targetPos);

        if (existing != null && existing.state == LockState.FULLY_LOCKED) {
            if (!existing.isOwner(playerUuid)) {
                player.displayClientMessage(
                        Component.literal("§cYou don't own this lock."), true
                );
                return;
            }
        }

        LockState current = storage.getState(dimension, targetPos);
        LockState next = current.next();
        UUID ownerUuid = next == LockState.UNLOCKED ? null : playerUuid;

        storage.set(dimension, targetPos, next, ownerUuid);

        String message = switch (next) {
            case UNLOCKED -> "§aUnlocked";
            case ENTITY_LOCKED -> "§eEntity-locked";
            case FULLY_LOCKED -> "§cFully locked";
        };
        player.displayClientMessage(Component.literal(message), true);

        Bolted.LOGGER.debug("[Bolted] {} set lock at {} to {}",
                player.getName().getString(), targetPos, next);
    }

    public static BlockPos getCanonicalDoorPos(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof DoorBlock) {
            if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                return pos.below();
            }
        }
        return pos;
    }

    public static boolean canInteract(Level level, BlockPos pos, Player player) {
        String dimension = level.dimension().identifier().toString();
        LockStorage storage = Bolted.LOCK_STORAGE;
        LockState lockState = storage.getState(dimension, pos);

        return switch (lockState) {
            case UNLOCKED -> true;
            case ENTITY_LOCKED -> true;
            case FULLY_LOCKED -> storage.isOwner(dimension, pos, player.getUUID());
        };
    }
}