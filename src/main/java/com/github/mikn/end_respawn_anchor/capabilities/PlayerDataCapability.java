package com.github.mikn.end_respawn_anchor.capabilities;

import java.util.List;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.util.RespawnData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class PlayerDataCapability implements IPlayerDataCapability{
    public static final Capability<IPlayerDataCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final String NBT_KEY_PLAYER_SPAWN_DIMENSION = "preSpawnDimension";
    public static final String NBT_KEY_PLAYER_SPAWN_POS_X = "preSpawnPosX";
    public static final String NBT_KEY_PLAYER_SPAWN_POS_Y = "preSpawnPosY";
    public static final String NBT_KEY_PLAYER_SPAWN_POS_Z = "preSpawnPosZ";
    public static final String NBT_KEY_PLAYER_SPAWN_ANGLE = "preSpawnAngle";

    private RespawnData respawnData;

    @Override
    public RespawnData getRespawnData() {
        return this.respawnData;
    }

    @Override
    public void setValue(RespawnData respawnData) {
        this.respawnData = respawnData;
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag tag = new CompoundTag();
        tag.putString(NBT_KEY_PLAYER_SPAWN_DIMENSION, this.respawnData.dimension().toString());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_X, this.respawnData.blockPos().getX());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Y, this.respawnData.blockPos().getY());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Z, this.respawnData.blockPos().getZ());
        tag.putFloat(NBT_KEY_PLAYER_SPAWN_ANGLE, this.respawnData.respawnAngle());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if(nbt.contains(NBT_KEY_PLAYER_SPAWN_POS_X)) {
            ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, nbt.get(NBT_KEY_PLAYER_SPAWN_DIMENSION)).resultOrPartial(EndRespawnAnchor.LOGGER::error).orElse(Level.OVERWORLD);
            int posX = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_X);
            int posY = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_Y);
            int posZ = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_Z);
            float angle = nbt.getFloat(NBT_KEY_PLAYER_SPAWN_ANGLE);
            this.respawnData = new RespawnData(dimension, new BlockPos(posX, posY, posZ), angle);
        }
    }
    
}
