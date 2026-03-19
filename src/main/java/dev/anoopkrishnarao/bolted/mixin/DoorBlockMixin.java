package dev.anoopkrishnarao.bolted.mixin;

import dev.anoopkrishnarao.bolted.Bolted;
import dev.anoopkrishnarao.bolted.lock.LockManager;
import dev.anoopkrishnarao.bolted.lock.LockState;
import dev.anoopkrishnarao.bolted.lock.LockStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public class DoorBlockMixin {

    @Inject(
            method = "useWithoutItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bolted$onDoorUse(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!(level instanceof ServerLevel)) return;

        BlockPos canonicalPos = LockManager.getCanonicalDoorPos(state, pos);
        String dimension = level.dimension().identifier().toString();
        LockStorage storage = Bolted.LOCK_STORAGE;
        LockState lockState = storage.getState(dimension, canonicalPos);

        if (lockState == LockState.FULLY_LOCKED) {
            if (!storage.isOwner(dimension, canonicalPos, player.getUUID())) {
                player.displayClientMessage(
                        Component.literal("§cThis door is locked."), true
                );
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}