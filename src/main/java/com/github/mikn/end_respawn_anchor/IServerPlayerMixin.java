package com.github.mikn.end_respawn_anchor;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IServerPlayerMixin {
    public void end_respawn_anchor$setPreBlockPos(BlockPos blockPos);
    public BlockPos end_respawn_anchor$getPreBlockPos();
    public void end_respawn_anchor$setPreRespawnDimension(ResourceKey<Level> dimension);
    public ResourceKey<Level> end_respawn_anchor$getPreRespawnDimension();
    public void end_respawn_anchor$setPreRespawnAngle(float f);
    public float end_respawn_anchor$getPreRespawnAngle();
}
