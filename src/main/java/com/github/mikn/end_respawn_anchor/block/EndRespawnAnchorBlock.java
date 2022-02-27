package com.github.mikn.end_respawn_anchor.block;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class EndRespawnAnchorBlock extends Block {
    public static final int MIN_CHARGES = 0;
    public static final int MAX_CHARGES = 4;
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
    public EndRespawnAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, 0));
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }
    public static int getScaledChargeLevel(BlockState p_55862_, int p_55863_) {
        return Mth.floor((float)(p_55862_.getValue(CHARGE)) / 4.0F * (float)p_55863_);
    }
}
