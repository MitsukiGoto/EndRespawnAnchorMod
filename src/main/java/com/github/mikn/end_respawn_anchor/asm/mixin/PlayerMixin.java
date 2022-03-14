package com.github.mikn.end_respawn_anchor.asm.mixin;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.event.FindRespawnPositionAndUseSpawnBlockEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerMixin {
    @Inject(method = "findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void inject(ServerWorld level, BlockPos blockPos, float respawnAngle, boolean isRespawnForced, boolean flag, CallbackInfoReturnable<Optional<Vector3d>> cir) {
        FindRespawnPositionAndUseSpawnBlockEvent evt = new FindRespawnPositionAndUseSpawnBlockEvent(level, blockPos, flag);
        MinecraftForge.EVENT_BUS.post(evt);
        EndRespawnAnchor.LOGGER.error(evt.getResult());
        if (evt.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW) {
            cir.setReturnValue(evt.getRespawnPosition());
        }
    }
}
