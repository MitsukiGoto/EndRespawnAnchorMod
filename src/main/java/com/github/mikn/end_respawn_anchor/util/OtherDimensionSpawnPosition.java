package com.github.mikn.end_respawn_anchor.util;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class OtherDimensionSpawnPosition {
    public ResourceKey<Level> dimension;
    public BlockPos blockPos;
    public float respawnAngle;

    public OtherDimensionSpawnPosition(ResourceKey<Level> dimension, BlockPos blockPos, float respawnAngle) {
        this.dimension = dimension;
        this.blockPos = blockPos;
        this.respawnAngle = respawnAngle;
    }

    public void printAll() {
        EndRespawnAnchor.LOGGER.error("dimension: " + dimension);
        EndRespawnAnchor.LOGGER.error("blockPos: " + blockPos);
        EndRespawnAnchor.LOGGER.error("respawnAngle: " + respawnAngle);
    }

}
