package com.github.mikn.end_respawn_anchor.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record OtherDimensionSpawnPosition(UUID uuid, ResourceKey<Level> dimension, BlockPos blockPos, float respawnAngle) {

}
