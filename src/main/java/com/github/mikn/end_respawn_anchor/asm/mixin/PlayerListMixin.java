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

import com.github.mikn.end_respawn_anchor.capabilities.PlayerDataCapability;
import com.github.mikn.end_respawn_anchor.config.EndRespawnAnchorConfig;
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
import org.spongepowered.asm.mixin.injection.Redirect;

@Debug(export = true)
@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getRespawnPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos redirect_position(ServerPlayer player) {
        player.reviveCaps();
        var cap = player.getCapability(PlayerDataCapability.INSTANCE, null);
        Optional<BlockPos> blockPos = cap.resolve().flatMap(value -> value.getRespawnData().map(data -> data.blockPos()));
        return shouldReplaceSpawnInfo(player) && cap.isPresent() ? blockPos.orElse(player.getRespawnPosition())
                : player.getRespawnPosition();
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getRespawnAngle()F"))
    private float redirect_f(ServerPlayer player) {
        player.reviveCaps();
        var cap = player.getCapability(PlayerDataCapability.INSTANCE, null);
        Optional<Float> angle = cap.resolve().flatMap(value -> value.getRespawnData().map(data -> data.respawnAngle()));
        return shouldReplaceSpawnInfo(player) && cap.isPresent() ? angle.orElse(player.getRespawnAngle())
                : player.getRespawnAngle();
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel redirect_serverlevel(MinecraftServer server, ResourceKey<Level> pDimension, ServerPlayer player,
            boolean pKeepEverything) {
        player.reviveCaps();
        var cap = player.getCapability(PlayerDataCapability.INSTANCE, null);
        Optional<ResourceKey<Level>> level = cap.resolve().flatMap(value -> value.getRespawnData().map(data -> data.dimension()));
        return server.getLevel(shouldReplaceSpawnInfo(player) ? level.orElse(pDimension) : pDimension);
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
    private void redirect_setRespawnPosition(ServerPlayer newPlayer, ResourceKey<Level> dimension, BlockPos blockPos, float f,
            boolean flag, boolean sendMessage, ServerPlayer oldPlayer, boolean pKeepEverything) {
        if (shouldReplaceSpawnInfo(oldPlayer)) {
            newPlayer.setRespawnPosition(oldPlayer.getRespawnDimension(), oldPlayer.getRespawnPosition(),
                    oldPlayer.getRespawnAngle(), oldPlayer.isRespawnForced(), false);
        } else {
            newPlayer.setRespawnPosition(dimension, blockPos, f, flag, false);
        }
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean redirect_is(BlockState blockState, Block block) {
        return blockState.is(Blocks.RESPAWN_ANCHOR) || blockState.is(BlockInit.END_RESPAWN_ANCHOR.get());
    }

    @Unique
    private boolean shouldReplaceSpawnInfo(ServerPlayer player) {
        // Both Respawn Dimension and position should be changed when players have set
        // their spawn point in the End.
        return EndRespawnAnchorConfig.shouldChangeSpawnInfo.get() && player.isChangingDimension()
                && player.getLevel().dimension() == Level.END && player.getRespawnDimension() == Level.END;
    }
}
