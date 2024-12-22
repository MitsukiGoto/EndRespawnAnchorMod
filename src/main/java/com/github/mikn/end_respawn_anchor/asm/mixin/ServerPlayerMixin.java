/*
 Copyright (c) 2023 Mikndesu

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

import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import com.github.mikn.end_respawn_anchor.config.EndRespawnAnchorConfig;
import com.github.mikn.end_respawn_anchor.init.DataAttachmentInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Debug(export = true)
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Shadow
    public abstract ServerLevel serverLevel();

    @Inject(method = "findRespawnAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void checkIfEndRespawnAnchor(ServerLevel level, BlockPos blockPos, float angle, boolean forced,
            boolean keepInventory, CallbackInfoReturnable<Optional<ServerPlayer.RespawnPosAngle>> cir) {
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof EndRespawnAnchorBlock
                && (forced || blockState.getValue(EndRespawnAnchorBlock.CHARGE) > 0)
                && EndRespawnAnchorBlock.canSetSpawn(level)) {
            Optional<Vec3> optional = EndRespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, blockPos);
            if (!forced && !keepInventory && optional.isPresent()) {
                level.setBlock(blockPos, blockState.setValue(EndRespawnAnchorBlock.CHARGE,
                        blockState.getValue(EndRespawnAnchorBlock.CHARGE) - 1), 3);
            }
            cir.setReturnValue(optional.map(vec3 -> ServerPlayer.RespawnPosAngle.of(vec3, blockPos)));
        }
    }

    @ModifyArgs(method = "findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;"))
    private void modifyArgs(Args args) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        if (shouldOverrideSpawnData(serverPlayer)) {
            ResourceKey<Level> resourceKey;
            BlockPos blockPos;
            float respawnAngle;
            if (serverPlayer.hasData(DataAttachmentInit.RESPAWN_DATA)) {
                var data = serverPlayer.getData(DataAttachmentInit.RESPAWN_DATA);
                resourceKey = data.getDimension();
                blockPos = data.getBlockPos();
                respawnAngle = data.getRespawnAngle();
            } else {
                resourceKey = Level.OVERWORLD;
                blockPos = serverLevel().getSharedSpawnPos();
                respawnAngle = serverPlayer.getRespawnAngle();
            }
            args.set(0, serverPlayer.getServer().getLevel(resourceKey));
            args.set(1, blockPos);
            args.set(2, respawnAngle);
        }
    }

    @ModifyArgs(method = "findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
    private void modifyReturnedServerLevel(Args args) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        if (shouldOverrideSpawnData(serverPlayer)) {
            ResourceKey<Level> resourceKey;
            if (serverPlayer.hasData(DataAttachmentInit.RESPAWN_DATA)) {
                var data = serverPlayer.getData(DataAttachmentInit.RESPAWN_DATA);
                resourceKey = data.getDimension();
            } else {
                resourceKey = Level.OVERWORLD;
            }
            args.set(0, resourceKey);
        }
    }

    @Unique
    private boolean shouldOverrideSpawnData(ServerPlayer serverPlayer) {
        // Both Respawn Dimension and position should be overridden when players have
        // set their spawn point in the End.
        return EndRespawnAnchorConfig.shouldChangeSpawnInfo.get() && isInsidePortal()
                && serverLevel().dimension() == Level.END && serverPlayer.getRespawnDimension() == Level.END;
    }

    @Unique
    private boolean isInsidePortal() {
        return ((ServerPlayer) (Object) this).portalProcess != null;
    }
}
