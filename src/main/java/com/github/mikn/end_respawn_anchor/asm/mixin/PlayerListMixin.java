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
import com.github.mikn.end_respawn_anchor.util.RespawnData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    private ServerPlayer _serverPlayer;
    private boolean _p_11238_;
    private boolean isDifferentWithDefault = false;
    private RespawnData spawnPosition = null;
    private boolean isAlive;
    private ServerLevel serverlevel;
    private BlockPos blockpos;
    private ResourceKey<Level> dimension;
    private final PlayerList playerList = (PlayerList) (Object) this;
    private Optional<Vec3> optional;

    @ModifyVariable(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"), ordinal = 0)
    private ServerPlayer capture_serverplayer(ServerPlayer serverPlayer) {
        _serverPlayer = serverPlayer;
        dimension = serverPlayer.serverLevel().dimension();
        blockpos = serverPlayer.getRespawnPosition();
        serverlevel = playerList.server.getLevel(serverPlayer.getRespawnDimension());
        isAlive = !serverPlayer.isDeadOrDying();
        return serverPlayer;
    }

    @ModifyVariable(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"), ordinal = 0)
    private boolean capture_boolean(boolean p_11238_) {
        _p_11238_ = p_11238_;
        return p_11238_;
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel redirect(MinecraftServer instance, ResourceKey<Level> level) {
        float f = _serverPlayer.getRespawnAngle();
        boolean flag = _serverPlayer.isRespawnForced();
        if (serverlevel != null && blockpos != null) {
            if ((isAlive && dimension == Level.END && serverlevel.dimension() == Level.END
                    && EndRespawnAnchor.spawnPositions.entrySet().stream()
                            .anyMatch(entry -> entry.getKey().equals(_serverPlayer.getUUID())))) {
                // expecting that players use End portal with their respawn position being in the End.
                RespawnData position = EndRespawnAnchor.spawnPositions.get(_serverPlayer.getUUID());
                spawnPosition = new RespawnData(_serverPlayer.getRespawnDimension(),
                        _serverPlayer.getRespawnPosition(), _serverPlayer.getRespawnAngle());
                _serverPlayer.setRespawnPosition(position.dimension(), position.blockPos(),
                        position.respawnAngle(), flag, false);
                blockpos = _serverPlayer.getRespawnPosition();
                serverlevel = playerList.server.getLevel(_serverPlayer.getRespawnDimension());
                isDifferentWithDefault = true;
            }
            optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag, _p_11238_);
        } else {
            optional = Optional.empty();
        }
        return instance.getLevel(level);
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
    private void inject(ServerPlayer serverPlayer, ResourceKey<Level> level, BlockPos blockPos, float f, boolean flag,
            boolean flag2) {
        ServerLevel serverlevel1 = serverlevel != null && optional.isPresent() ? serverlevel : playerList.server.overworld();
        if (isDifferentWithDefault) {
            serverPlayer.setRespawnPosition(spawnPosition.dimension(), spawnPosition.blockPos(),
                    spawnPosition.respawnAngle(), flag, false);
        } else {
            serverPlayer.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag, false);
        }
    }

    @Redirect(method="respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;"))
    private Optional<Vec3> inject(ServerLevel optional, BlockPos flag, float flag1, boolean p_36131_, boolean p_36132_) {
        /**
         *  bypassing method calling {@link Player#findRespawnPositionAndUseSpawnBlock(ServerLevel,BlockPos,float,boolean,boolean)}
         *  as it will be called here {@link PlayerListMixin#redirect(MinecraftServer,ResourceKey<Level>)}
         */
        return Optional.empty();
    }

    @ModifyVariable(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At("STORE"), ordinal = 0)
    private Optional<Vec3> inject_to_optional(Optional<Vec3> o) {
        return optional;
    }

    @ModifyVariable(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At("STORE"), ordinal = 1)
    private ServerLevel inject_to_serverlevel1(ServerLevel o) {
        return optional.isPresent() ? playerList.server.getLevel(_serverPlayer.getRespawnDimension()) : playerList.server.overworld();
    }
}
