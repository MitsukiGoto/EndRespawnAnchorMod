package com.github.mikn.end_respawn_anchor.asm.mixin;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import com.github.mikn.end_respawn_anchor.event.FindRespawnPositionAndUseSpawnBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void inject(ServerLevel level, BlockPos blockPos, float respawnAngle, boolean isRespawnForced, boolean flag, CallbackInfoReturnable<Optional<Vec3>> cir) {
        FindRespawnPositionAndUseSpawnBlockEvent evt = new FindRespawnPositionAndUseSpawnBlockEvent(level, blockPos, flag);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW) {
            EndRespawnAnchor.LOGGER.debug("Called FindRespawnPositionAndUseSpawnBlockEvent");
            BlockState blockState = evt.getBlockState();
            cir.setReturnValue(evt.getRespawnPosition());
        }
    }
}
