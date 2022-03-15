package com.github.mikn.end_respawn_anchor.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record OtherDimensionSpawnPosition(ResourceKey<Level> dimension, BlockPos blockPos, float respawnAngle) {
    @Override
    public String toString() {
        return "Dimension: " + provideDimension(dimension) + " BlockPos: " + blockPos.toString();
    }

    private String provideDimension(ResourceKey<Level> level) {
        if (level == Level.OVERWORLD) {
            return "overworld";
        } else if (level == Level.NETHER) {
            return "nether";
        } else if (level == Level.END) {
            return "end";
        }
        return "Other dimension";
    }
}
