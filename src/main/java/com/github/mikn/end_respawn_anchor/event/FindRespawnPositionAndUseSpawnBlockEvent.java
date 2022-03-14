package com.github.mikn.end_respawn_anchor.event;

import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

import java.util.Optional;

@Event.HasResult
public class FindRespawnPositionAndUseSpawnBlockEvent extends Event {
    private final World level;
    private final BlockPos blockPos;
    private final boolean flag;

    public World getLevel() {
        return this.level;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public boolean getFlag() {
        return this.flag;
    }

    public FindRespawnPositionAndUseSpawnBlockEvent(World level, BlockPos blockPos, boolean flag) {
        this.level = level;
        this.blockPos = blockPos;
        this.flag = flag;
    }

    public Optional<Vector3d> getRespawnPosition() {
        return EndRespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, blockPos);
    }

}
