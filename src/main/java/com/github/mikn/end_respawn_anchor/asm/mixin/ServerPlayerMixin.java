package com.github.mikn.end_respawn_anchor.asm.mixin;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.IServerPlayerMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IServerPlayerMixin {
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

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at=@At("TAIL"))
    private void end_respawn_anchor$addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        var p = (IServerPlayerMixin)(Object) this;
        if(p.end_respawn_anchor$getPreBlockPos() != null) {
            CompoundTag element = new CompoundTag();
            element.putInt(NBT_KEY_PLAYER_SPAWN_POS_X, p.end_respawn_anchor$getPreBlockPos().getX());
            element.putInt(NBT_KEY_PLAYER_SPAWN_POS_Y, p.end_respawn_anchor$getPreBlockPos().getY());
            element.putInt(NBT_KEY_PLAYER_SPAWN_POS_Z, p.end_respawn_anchor$getPreBlockPos().getZ());
            element.putString(NBT_KEY_PLAYER_SPAWN_DIMENSION, p.end_respawn_anchor$getPreRespawnDimension().location().toString());
            element.putFloat(NBT_KEY_PLAYER_SPAWN_ANGLE, p.end_respawn_anchor$getPreRespawnAngle());
            compound.put(EndRespawnAnchor.MODID, element);
        }
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at=@At("TAIL"))
    private void end_respawn_anchor$readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if(compound.contains(EndRespawnAnchor.MODID)) {
            var p = (IServerPlayerMixin)(Object) this;
            CompoundTag tag = compound.getCompound(EndRespawnAnchor.MODID);
            p.end_respawn_anchor$setPreBlockPos(new BlockPos(tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_X), tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_Y), tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_Z)));
            p.end_respawn_anchor$setPreRespawnDimension(Level.RESOURCE_KEY_CODEC
                    .parse(NbtOps.INSTANCE, tag.get(NBT_KEY_PLAYER_SPAWN_DIMENSION))
                    .resultOrPartial(EndRespawnAnchor.LOGGER::error).orElse(Level.OVERWORLD));
            p.end_respawn_anchor$setPreRespawnAngle(tag.getFloat(NBT_KEY_PLAYER_SPAWN_ANGLE));
        }
    }

    @Inject(method= "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at=@At("TAIL"))
    private void setEnd_respawn_anchor$restoreFrom(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        var newPlayer = (IServerPlayerMixin)(Object) this;
        var oldPlayer = (IServerPlayerMixin)(Object) that;
        if(oldPlayer.end_respawn_anchor$getPreRespawnDimension()!=null) {
            newPlayer.end_respawn_anchor$setPreBlockPos(oldPlayer.end_respawn_anchor$getPreBlockPos());
            newPlayer.end_respawn_anchor$setPreRespawnDimension(oldPlayer.end_respawn_anchor$getPreRespawnDimension());
            newPlayer.end_respawn_anchor$setPreRespawnAngle(oldPlayer.end_respawn_anchor$getPreRespawnAngle());
        }
    }
}
