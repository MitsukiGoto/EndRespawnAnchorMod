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

package com.github.mikn.end_respawn_anchor.capabilities;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.RespawnData;

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
        tag.putString(NBT_KEY_PLAYER_SPAWN_DIMENSION, this.respawnData.dimension().location().toString());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_X, this.respawnData.blockPos().getX());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Y, this.respawnData.blockPos().getY());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Z, this.respawnData.blockPos().getZ());
        tag.putFloat(NBT_KEY_PLAYER_SPAWN_ANGLE, this.respawnData.respawnAngle());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, nbt.get(NBT_KEY_PLAYER_SPAWN_DIMENSION))
                .resultOrPartial(EndRespawnAnchor.LOGGER::error).orElse(Level.OVERWORLD);
        int posX = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_X);
        int posY = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_Y);
        int posZ = nbt.getInt(NBT_KEY_PLAYER_SPAWN_POS_Z);
        float angle = nbt.getFloat(NBT_KEY_PLAYER_SPAWN_ANGLE);
        BlockPos blockPos = new BlockPos(posX, posY, posZ);
        this.respawnData = new RespawnData(dimension, blockPos, angle);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IPlayerDataCapability.class);
    }
}
