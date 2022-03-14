package com.github.mikn.end_respawn_anchor.util;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OtherDimensionSpawnPosition {
    private final RegistryKey<World> dimension;
    private final BlockPos blockPos;
    private final float respawnAngle;

    public OtherDimensionSpawnPosition(RegistryKey<World> dimension, BlockPos blockPos, float respawnAngle) {
        this.dimension = dimension;
        this.blockPos = blockPos;
        this.respawnAngle = respawnAngle;
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public float getRespawnAngle() {
        return respawnAngle;
    }
}
