package com.github.mikn.end_respawn_anchor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class EndRespawnAnchorBlock extends Block {
    public static final int MIN_CHARGES = 0;
    public static final int MAX_CHARGES = 4;
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
    public EndRespawnAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, 0));
    }
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (interactionHand == InteractionHand.MAIN_HAND && !isRespawnFuel(itemstack) && isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionResult.PASS;
        } else if (isRespawnFuel(itemstack) && canBeCharged(blockState)) {
            charge(level, blockPos, blockState);
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (blockState.getValue(CHARGE) == 0) {
            return InteractionResult.PASS;
        } else if (!canSetSpawn(level)) {
            if (!level.isClientSide) {
//                this.explode(blockState, level, blockPos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            if (!level.isClientSide) {
                ServerPlayer serverplayer = (ServerPlayer)player;
                if (serverplayer.getRespawnDimension() != level.dimension() || !blockPos.equals(serverplayer.getRespawnPosition())) {
                    serverplayer.setRespawnPosition(level.dimension(), blockPos, 0.0F, false, true);
                    level.playSound((Player)null, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.CONSUME;
        }
    }
    public static boolean canSetSpawn(Level level) {
        return level.dimensionType().respawnAnchorWorks();
    }
    private static boolean canBeCharged(BlockState blockState) {
        return blockState.getValue(CHARGE) < 4;
    }
    public static void charge(Level level, BlockPos blockPos, BlockState blockState) {
        level.setBlock(blockPos, blockState.setValue(CHARGE, blockState.getValue(CHARGE) + 1), 3);
        level.playSound((Player)null, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    private static boolean isRespawnFuel(ItemStack itemStack) {
        return itemStack.is(Items.GLOWSTONE);
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }
    public static int getScaledChargeLevel(BlockState p_55862_, int p_55863_) {
        return Mth.floor((float)(p_55862_.getValue(CHARGE)) / 4.0F * (float)p_55863_);
    }
}
