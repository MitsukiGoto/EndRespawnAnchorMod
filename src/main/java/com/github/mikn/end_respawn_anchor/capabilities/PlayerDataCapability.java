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
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class PlayerDataCapability implements IPlayerDataCapability {
    public static final Capability<IPlayerDataCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
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
        if(this.respawnData != null) {
            tag.putString(NBT_KEY_PLAYER_SPAWN_DIMENSION, this.respawnData.dimension().location().toString());
            tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_X, this.respawnData.blockPos().getX());
            tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Y, this.respawnData.blockPos().getY());
            tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Z, this.respawnData.blockPos().getZ());
            tag.putFloat(NBT_KEY_PLAYER_SPAWN_ANGLE, this.respawnData.respawnAngle());
        } else {
            tag.putString(NBT_KEY_PLAYER_SPAWN_DIMENSION, Level.OVERWORLD.location().toString());
            tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_X, 0);
            tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Y, 0);
            tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Z, 0);
            tag.putFloat(NBT_KEY_PLAYER_SPAWN_ANGLE, 0.0f);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC
                .parse(NbtOps.INSTANCE, nbt.get(NBT_KEY_PLAYER_SPAWN_DIMENSION))
                .resultOrPartial(EndRespawnAnchor.LOGGER::error).orElse(Level.OVERWORLD);
        int posX = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_X);
        int posY = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_Y);
        int posZ = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_Z);
        float angle = nbt.getFloat(NBT_KEY_PLAYER_SPAWN_ANGLE);
        BlockPos blockPos = (posX == 0 && posY == 0 && posZ == 0) ? null : new BlockPos(posX, posY, posZ);
        this.respawnData = new RespawnData(dimension, blockPos, angle);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IPlayerDataCapability.class);
    }
}
