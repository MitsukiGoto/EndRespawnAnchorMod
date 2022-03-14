package com.github.mikn.end_respawn_anchor.block;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.config.EndRespawnAnchorConfig;
import com.github.mikn.end_respawn_anchor.util.OtherDimensionSpawnPosition;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.*;

import java.util.Optional;
import java.util.Random;

import static net.minecraft.world.World.END;

public class EndRespawnAnchorBlock extends Block {
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
    private static final ImmutableList<Vector3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vector3i(0, 0, -1), new Vector3i(-1, 0, 0), new Vector3i(0, 0, 1), new Vector3i(1, 0, 0), new Vector3i(-1, 0, -1), new Vector3i(1, 0, -1), new Vector3i(-1, 0, 1), new Vector3i(1, 0, 1));
    private static final ImmutableList<Vector3i> RESPAWN_OFFSETS = (new ImmutableList.Builder<Vector3i>()).addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vector3i::below).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vector3i::above).iterator()).add(new Vector3i(0, 1, 0)).build();

    public EndRespawnAnchorBlock(AbstractBlock.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, 0));
    }

    public static Optional<Vector3d> findStandUpPosition(EntityType<?> p_235560_0_, ICollisionReader p_235560_1_, BlockPos p_235560_2_) {
        Optional<Vector3d> optional = findStandUpPosition(p_235560_0_, p_235560_1_, p_235560_2_, true);
        return optional.isPresent() ? optional : findStandUpPosition(p_235560_0_, p_235560_1_, p_235560_2_, false);
    }

    private static Optional<Vector3d> findStandUpPosition(EntityType<?> p_242678_0_, ICollisionReader p_242678_1_, BlockPos p_242678_2_, boolean p_242678_3_) {
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        for(Vector3i vector3i : RESPAWN_OFFSETS) {
            blockpos$mutable.set(p_242678_2_).move(vector3i);
            Vector3d vector3d = TransportationHelper.findSafeDismountLocation(p_242678_0_, p_242678_1_, blockpos$mutable, p_242678_3_);
            if (vector3d != null) {
                return Optional.of(vector3d);
            }
        }
        return Optional.empty();
    }

    public static boolean isEnd(World level) {
        return level.dimension() == END;
    }

    private static boolean canBeCharged(BlockState blockState) {
        return blockState.getValue(CHARGE) < 4;
    }

    public static void charge(World level, BlockPos blockPos, BlockState blockState) {
        level.setBlock(blockPos, blockState.setValue(CHARGE, blockState.getValue(CHARGE) + 1), 3);
        level.playSound(null, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private static boolean isRespawnFuel(ItemStack itemStack) {
        return itemStack.getItem() == Items.ENDER_EYE;
    }

    public static int getScaledChargeLevel(BlockState p_55862_, int p_55863_) {
        return MathHelper.floor((float) (p_55862_.getValue(CHARGE)) / 4.0F * (float) p_55863_);
    }

    private static boolean isWaterThatWouldFlow(BlockPos blockPos, World level) {
        FluidState fluidstate = level.getFluidState(blockPos);
        if (!fluidstate.is(FluidTags.WATER)) {
            return false;
        } else if (fluidstate.isSource()) {
            return true;
        } else {
            float f = (float) fluidstate.getAmount();
            if (f < 2.0F) {
                return false;
            } else {
                FluidState fluidstate1 = level.getFluidState(blockPos.below());
                return !fluidstate1.is(FluidTags.WATER);
            }
        }
    }

    public ActionResultType use(BlockState blockState, World level, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (hand == Hand.MAIN_HAND && !isRespawnFuel(itemstack) && isRespawnFuel(player.getItemInHand(Hand.OFF_HAND))) {
            return ActionResultType.PASS;
        } else if (isRespawnFuel(itemstack) && canBeCharged(blockState)) {
            charge(level, blockPos, blockState);
            if (!player.abilities.instabuild) {
                itemstack.shrink(1);
            }

            return ActionResultType.sidedSuccess(level.isClientSide);
        } else if (blockState.getValue(CHARGE) == 0) {
            return ActionResultType.PASS;
        } else if (!isEnd(level)) {
            if (!level.isClientSide && EndRespawnAnchorConfig.isExplode.get()) {
                this.explode(blockState, level, blockPos);
            }
            return ActionResultType.sidedSuccess(level.isClientSide);
        } else {
            if (!level.isClientSide) {
                ServerPlayerEntity serverplayer = (ServerPlayerEntity) player;
                if (serverplayer.getRespawnDimension() != level.dimension() || !blockPos.equals(serverplayer.getRespawnPosition())) {
                    if (serverplayer.getRespawnDimension() != END) {
                        EndRespawnAnchor.spawnPositions.put(serverplayer.getUUID(), new OtherDimensionSpawnPosition(serverplayer.getRespawnDimension(), serverplayer.getRespawnPosition(), serverplayer.getRespawnAngle()));
                    }
                    serverplayer.setRespawnPosition(level.dimension(), blockPos, 0.0F, false, true);
                    level.playSound(null, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResultType.SUCCESS;
                }
            }
            return ActionResultType.CONSUME;
        }
    }

    public void animateTick(BlockState blockState, World level, BlockPos blockPos, Random random) {
        if (blockState.getValue(CHARGE) != 0) {
            if (random.nextInt(100) == 0) {
                level.playSound(null, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            double d0 = blockPos.getX() + 0.5D + (0.5D - random.nextDouble());
            double d1 = (double) blockPos.getY() + 1.0D;
            double d2 = blockPos.getZ() + 0.5D + (0.5D - random.nextDouble());
            double d3 = (double) random.nextFloat() * 0.04D;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, d0, d1, d2, 0.0D, d3, 0.0D);
        }
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    private void explode(BlockState p_235567_1_, World p_235567_2_, final BlockPos p_235567_3_) {
        p_235567_2_.removeBlock(p_235567_3_, false);
        boolean flag = Direction.Plane.HORIZONTAL.stream().map(p_235567_3_::relative).anyMatch((p_235563_1_) -> {
            return isWaterThatWouldFlow(p_235563_1_, p_235567_2_);
        });
        final boolean flag1 = flag || p_235567_2_.getFluidState(p_235567_3_.above()).is(FluidTags.WATER);
        ExplosionContext explosioncontext = new ExplosionContext() {
            public Optional<Float> getBlockExplosionResistance(Explosion p_230312_1_, IBlockReader p_230312_2_, BlockPos p_230312_3_, BlockState p_230312_4_, FluidState p_230312_5_) {
                return p_230312_3_.equals(p_235567_3_) && flag1 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance(p_230312_1_, p_230312_2_, p_230312_3_, p_230312_4_, p_230312_5_);
            }
        };
        p_235567_2_.explode((Entity)null, DamageSource.badRespawnPointExplosion(), explosioncontext, (double)p_235567_3_.getX() + 0.5D, (double)p_235567_3_.getY() + 0.5D, (double)p_235567_3_.getZ() + 0.5D, 5.0F, true, Explosion.Mode.DESTROY);
    }

}
