package com.github.mikn.end_respawn_anchor.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record OtherDimensionSpawnPosition(ResourceKey<Level> dimension, BlockPos blockPos, float respawnAngle) {
}
