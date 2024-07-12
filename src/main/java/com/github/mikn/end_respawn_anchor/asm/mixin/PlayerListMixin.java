/*
 Copyright (c) 2022 Mikndesu

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.mikn.end_respawn_anchor.asm.mixin;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.IServerPlayerMixin;
import com.github.mikn.end_respawn_anchor.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Debug(export = true)
@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getRespawnPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos redirect_position(ServerPlayer player) {
        var originalRespawnPosition = player.getRespawnPosition();
        if (!shouldOverrideSpawnData(player)) {
            return originalRespawnPosition;
        }
        var p = (IServerPlayerMixin) (Object) player;
        Optional<BlockPos> optional = Optional.ofNullable(p.end_respawn_anchor$getPreBlockPos());
        return optional.orElse(originalRespawnPosition);
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getRespawnAngle()F"))
    private float redirect_f(ServerPlayer player) {
        var originalRespawnAngle = player.getRespawnAngle();
        if (!shouldOverrideSpawnData(player)) {
            return originalRespawnAngle;
        }
        var p = (IServerPlayerMixin) (Object) player;
        Optional<Float> optional = Optional.ofNullable(p.end_respawn_anchor$getPreRespawnAngle());
        return optional.orElse(originalRespawnAngle);
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel redirect_serverlevel(MinecraftServer server, ResourceKey<Level> pDimension, ServerPlayer player,
            boolean pKeepEverything) {
        if (!shouldOverrideSpawnData(player)) {
            return server.getLevel(pDimension);
        }
        var p = (IServerPlayerMixin) (Object) player;
        Optional<BlockPos> optional = Optional.ofNullable(p.end_respawn_anchor$getPreBlockPos());
        // if saved block pos is valid, use saved respawn dimension.
        return server.getLevel(optional.isPresent() ? p.end_respawn_anchor$getPreRespawnDimension() : pDimension);
    }

    @ModifyArgs(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
    private void modifyArgs_setRespawnPosition(Args args, ServerPlayer oldPlayer,
            boolean pKeepEverything) {
        if (shouldOverrideSpawnData(oldPlayer)) {
            args.set(0, oldPlayer.getRespawnDimension());
            args.set(1, oldPlayer.getRespawnPosition());
            args.set(2, oldPlayer.getRespawnAngle());
            args.set(3, oldPlayer.isRespawnForced());
        }
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean redirect_is(BlockState blockState, Block block) {
        return blockState.is(Blocks.RESPAWN_ANCHOR) || blockState.is(BlockInit.END_RESPAWN_ANCHOR);
    }

    @Unique
    private boolean shouldOverrideSpawnData(ServerPlayer player) {
        // Both Respawn Dimension and position should be overridden when players have
        // set their spawn point in the End.
        return EndRespawnAnchor.HOLDER.shouldOverrideSpawnData && player.isChangingDimension()
                && player.level().dimension() == Level.END && player.getRespawnDimension() == Level.END;
    }
}
