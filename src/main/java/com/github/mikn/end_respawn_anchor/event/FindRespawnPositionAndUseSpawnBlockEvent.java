package com.github.mikn.end_respawn_anchor.event;

import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

import java.util.Optional;

@Event.HasResult
public class FindRespawnPositionAndUseSpawnBlockEvent extends Event {
    private final Level level;
    private final BlockPos blockPos;
    private final boolean flag;
    private final BlockState blockState;

    public Level getLevel() {
        return this.level;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public boolean getFlag() {
        return this.flag;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public FindRespawnPositionAndUseSpawnBlockEvent(Level level, BlockPos blockPos, boolean flag) {
        this.level = level;
        this.blockPos = blockPos;
        this.flag = flag;
        this.blockState = level.getBlockState(blockPos);
    }

    public Optional<Vec3> getRespawnPosition() {
        return EndRespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, blockPos);
    }

}
