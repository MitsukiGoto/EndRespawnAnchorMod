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

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.IServerPlayerMixin;
import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Debug(export = true)
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IServerPlayerMixin {
    @Shadow
    public abstract ServerLevel serverLevel();

    @Unique
    private BlockPos end_respawn_anchor$preSpawnPos = null;
    @Unique
    private ResourceKey<Level> end_respawn_anchor$preSpawnDimension = Level.OVERWORLD;
    @Unique
    private float end_respawn_anchor$preSpawnAngle = 0.0f;
    @Unique
    private static final String NBT_KEY_PLAYER_SPAWN_DIMENSION = "preSpawnDimension";
    @Unique
    private static final String NBT_KEY_PLAYER_SPAWN_POS_X = "preSpawnPosX";
    @Unique
    private static final String NBT_KEY_PLAYER_SPAWN_POS_Y = "preSpawnPosY";
    @Unique
    private static final String NBT_KEY_PLAYER_SPAWN_POS_Z = "preSpawnPosZ";
    @Unique
    private static final String NBT_KEY_PLAYER_SPAWN_ANGLE = "preSpawnAngle";

    @Unique
    @Override
    public void end_respawn_anchor$setPreBlockPos(BlockPos blockPos) {
        this.end_respawn_anchor$preSpawnPos = blockPos;
    }

    @Unique
    @Override
    public BlockPos end_respawn_anchor$getPreBlockPos() {
        return this.end_respawn_anchor$preSpawnPos;
    }

    @Unique
    @Override
    public void end_respawn_anchor$setPreRespawnDimension(ResourceKey<Level> dimension) {
        this.end_respawn_anchor$preSpawnDimension = dimension;
    }

    @Unique
    @Override
    public ResourceKey<Level> end_respawn_anchor$getPreRespawnDimension() {
        return this.end_respawn_anchor$preSpawnDimension;
    }

    @Unique
    @Override
    public void end_respawn_anchor$setPreRespawnAngle(float f) {
        this.end_respawn_anchor$preSpawnAngle = f;
    }

    @Unique
    @Override
    public float end_respawn_anchor$getPreRespawnAngle() {
        return this.end_respawn_anchor$preSpawnAngle;
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void end_respawn_anchor$addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        var p = (IServerPlayerMixin) (Object) this;
        if (p.end_respawn_anchor$getPreBlockPos() != null) {
            CompoundTag element = new CompoundTag();
            element.putInt(NBT_KEY_PLAYER_SPAWN_POS_X, p.end_respawn_anchor$getPreBlockPos().getX());
            element.putInt(NBT_KEY_PLAYER_SPAWN_POS_Y, p.end_respawn_anchor$getPreBlockPos().getY());
            element.putInt(NBT_KEY_PLAYER_SPAWN_POS_Z, p.end_respawn_anchor$getPreBlockPos().getZ());
            element.putString(NBT_KEY_PLAYER_SPAWN_DIMENSION,
                    p.end_respawn_anchor$getPreRespawnDimension().location().toString());
            element.putFloat(NBT_KEY_PLAYER_SPAWN_ANGLE, p.end_respawn_anchor$getPreRespawnAngle());
            compound.put(EndRespawnAnchor.MODID, element);
        }
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void end_respawn_anchor$readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains(EndRespawnAnchor.MODID)) {
            var p = (IServerPlayerMixin) (Object) this;
            CompoundTag tag = compound.getCompound(EndRespawnAnchor.MODID);
            p.end_respawn_anchor$setPreBlockPos(new BlockPos(tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_X),
                    tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_Y), tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_Z)));
            p.end_respawn_anchor$setPreRespawnDimension(Level.RESOURCE_KEY_CODEC
                    .parse(NbtOps.INSTANCE, tag.get(NBT_KEY_PLAYER_SPAWN_DIMENSION))
                    .resultOrPartial(EndRespawnAnchor.LOGGER::error).orElse(Level.OVERWORLD));
            p.end_respawn_anchor$setPreRespawnAngle(tag.getFloat(NBT_KEY_PLAYER_SPAWN_ANGLE));
        }
    }

    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void end_respawn_anchor$restoreFrom(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        var newPlayer = (IServerPlayerMixin) this;
        var oldPlayer = (IServerPlayerMixin) that;
        if (oldPlayer.end_respawn_anchor$getPreRespawnDimension() != null) {
            newPlayer.end_respawn_anchor$setPreBlockPos(oldPlayer.end_respawn_anchor$getPreBlockPos());
            newPlayer.end_respawn_anchor$setPreRespawnDimension(oldPlayer.end_respawn_anchor$getPreRespawnDimension());
            newPlayer.end_respawn_anchor$setPreRespawnAngle(oldPlayer.end_respawn_anchor$getPreRespawnAngle());
        }
    }

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
                        Integer.valueOf(blockState.getValue(EndRespawnAnchorBlock.CHARGE) - 1)), 3);
            }
            cir.setReturnValue(optional.map(vec3 -> ServerPlayer.RespawnPosAngle.of(vec3, blockPos)));
        }
    }

    @ModifyArgs(method = "findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;"))
    private void modifyArgs(Args args, boolean keepInventory,
            DimensionTransition.PostDimensionTransition postDimensionTransition) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        if (shouldOverrideSpawnData(serverPlayer)) {
            var p = (IServerPlayerMixin) serverPlayer;
            Optional<BlockPos> optionalBlockPos = Optional.ofNullable((p.end_respawn_anchor$getPreBlockPos()));
            Optional<Float> optionalAngle = Optional.ofNullable((p.end_respawn_anchor$getPreRespawnAngle()));
            ResourceKey<Level> respawnDimension = p.end_respawn_anchor$getPreRespawnDimension();
            optionalBlockPos.ifPresent(blockPos -> optionalAngle.ifPresent(angle -> {
                serverPlayer.getServer().sendSystemMessage(Component
                        .literal(String.valueOf(serverPlayer.getServer().getLevel(respawnDimension).dimension())));
                args.set(0, serverPlayer.getServer().getLevel(respawnDimension));
                args.set(1, blockPos);
                args.set(2, angle);
            }));
        }
    }

    @ModifyArgs(method = "findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;", at = @At(value = "INVOKE", target = "net/minecraft/world/level/portal/DimensionTransition.<init> (Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FFLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)V"))
    private void modifyReturnedServerLevel(Args args) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        if (shouldOverrideSpawnData(serverPlayer)) {
            var p = (IServerPlayerMixin) serverPlayer;
            Optional<BlockPos> optionalBlockPos = Optional.ofNullable((p.end_respawn_anchor$getPreBlockPos()));
            Optional<Float> optionalAngle = Optional.ofNullable((p.end_respawn_anchor$getPreRespawnAngle()));
            ResourceKey<Level> respawnDimension = p.end_respawn_anchor$getPreRespawnDimension();
            if (optionalBlockPos.isPresent() && optionalAngle.isPresent()) {
                args.set(0, serverPlayer.getServer().getLevel(respawnDimension));
            }
        }
    }

    @Unique
    private boolean shouldOverrideSpawnData(ServerPlayer serverPlayer) {
        // Both Respawn Dimension and position should be overridden when players have
        // set their spawn point in the End.
        return EndRespawnAnchor.HOLDER.shouldOverrideSpawnData && isInsidePortal()
                && serverPlayer.level().dimension() == Level.END && serverPlayer.getRespawnDimension() == Level.END;
    }

    @Unique
    private boolean isInsidePortal() {
        return ((ServerPlayer) (Object) this).portalProcess != null;
    }
}
