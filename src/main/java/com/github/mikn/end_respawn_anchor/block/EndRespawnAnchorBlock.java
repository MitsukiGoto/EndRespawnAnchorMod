package com.github.mikn.end_respawn_anchor.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Random;

public class EndRespawnAnchorBlock extends Block {
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
    private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
    private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = (new ImmutableList.Builder<Vec3i>()).addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator()).add(new Vec3i(0, 1, 0)).build();

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
        } else if (!isEnd(level)) {
            if (!level.isClientSide) {
//                this.explode(blockState, level, blockPos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            if (!level.isClientSide) {
                ServerPlayer serverplayer = (ServerPlayer) player;
                if (serverplayer.getRespawnDimension() != level.dimension() || !blockPos.equals(serverplayer.getRespawnPosition())) {
                    serverplayer.setRespawnPosition(level.dimension(), blockPos, 0.0F, false, true);
                    level.playSound((Player) null, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (blockState.getValue(CHARGE) != 0) {
            if (random.nextInt(100) == 0) {
                level.playSound((Player) null, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            double d0 = (double) blockPos.getX() + 0.5D + (0.5D - random.nextDouble());
            double d1 = (double) blockPos.getY() + 1.0D;
            double d2 = (double) blockPos.getZ() + 0.5D + (0.5D - random.nextDouble());
            double d3 = (double) random.nextFloat() * 0.04D;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, d0, d1, d2, 0.0D, d3, 0.0D);
        }
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> p_55840_, CollisionGetter p_55841_, BlockPos p_55842_) {
        Optional<Vec3> optional = findStandUpPosition(p_55840_, p_55841_, p_55842_, true);
        return optional.isPresent() ? optional : findStandUpPosition(p_55840_, p_55841_, p_55842_, false);
    }

    private static Optional<Vec3> findStandUpPosition(EntityType<?> p_55844_, CollisionGetter p_55845_, BlockPos p_55846_, boolean p_55847_) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for(Vec3i vec3i : RESPAWN_OFFSETS) {
            blockpos$mutableblockpos.set(p_55846_).move(vec3i);
            Vec3 vec3 = DismountHelper.findSafeDismountLocation(p_55844_, p_55845_, blockpos$mutableblockpos, p_55847_);
            if (vec3 != null) {
                return Optional.of(vec3);
            }
        }
        return Optional.empty();
    }

    public static boolean isEnd(Level level) {
        return level.dimension() == Level.END;
    }

    private static boolean canBeCharged(BlockState blockState) {
        return blockState.getValue(CHARGE) < 4;
    }

    public static void charge(Level level, BlockPos blockPos, BlockState blockState) {
        level.setBlock(blockPos, blockState.setValue(CHARGE, blockState.getValue(CHARGE) + 1), 3);
        level.playSound((Player) null, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static boolean isRespawnFuel(ItemStack itemStack) {
        return itemStack.is(Items.ENDER_EYE);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    public static int getScaledChargeLevel(BlockState p_55862_, int p_55863_) {
        return Mth.floor((float) (p_55862_.getValue(CHARGE)) / 4.0F * (float) p_55863_);
    }
}
