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

package com.github.mikn.end_respawn_anchor.data_attachment;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class RespawnData implements INBTSerializable<CompoundTag> {

    private ResourceKey<Level> dimension;
    private BlockPos blockPos;
    private float respawnAngle;

    public static final String NBT_KEY_PLAYER_SPAWN_DIMENSION = "preSpawnDimension";
    public static final String NBT_KEY_PLAYER_SPAWN_POS_X = "preSpawnPosX";
    public static final String NBT_KEY_PLAYER_SPAWN_POS_Y = "preSpawnPosY";
    public static final String NBT_KEY_PLAYER_SPAWN_POS_Z = "preSpawnPosZ";
    public static final String NBT_KEY_PLAYER_SPAWN_ANGLE = "preSpawnAngle";

    public RespawnData(ResourceKey<Level> dimension, BlockPos blockPos, float respawnAngle) {
        this.dimension = dimension;
        this.blockPos = blockPos;
        this.respawnAngle = respawnAngle;
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public float getRespawnAngle() {
        return this.respawnAngle;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_KEY_PLAYER_SPAWN_DIMENSION, this.dimension.location().toString());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_X, this.blockPos.getX());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Y, this.blockPos.getY());
        tag.putInt(NBT_KEY_PLAYER_SPAWN_POS_Z, this.blockPos.getZ());
        tag.putFloat(NBT_KEY_PLAYER_SPAWN_ANGLE, this.respawnAngle);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC
                .parse(NbtOps.INSTANCE, tag.get(NBT_KEY_PLAYER_SPAWN_DIMENSION))
                .resultOrPartial(EndRespawnAnchor.LOGGER::error).orElse(Level.OVERWORLD);
        int posX = tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_X);
        int posY = tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_Y);
        int posZ = tag.getInt(NBT_KEY_PLAYER_SPAWN_POS_Z);
        float angle = tag.getFloat(NBT_KEY_PLAYER_SPAWN_ANGLE);
        BlockPos blockPos = new BlockPos(posX, posY, posZ);
        this.dimension = dimension;
        this.blockPos = blockPos;
        this.respawnAngle = angle;
    }
}
