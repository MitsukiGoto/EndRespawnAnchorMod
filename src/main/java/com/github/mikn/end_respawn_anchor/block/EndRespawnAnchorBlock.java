/*
 Copyright (c) 2022 Mikndesu

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.mikn.end_respawn_anchor.block;

import com.github.mikn.end_respawn_anchor.IServerPlayerMixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EndRespawnAnchorBlock extends RespawnAnchorBlock {

    public EndRespawnAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (isRespawnFuel(stack) && super.canBeCharged(state)) {
            charge(player, level, pos, state);
            stack.consume(1, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return hand == InteractionHand.MAIN_HAND && isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))
                    && super.canBeCharged(state) ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                            : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (state.getValue(CHARGE) == 0) {
            return InteractionResult.PASS;
        }
        if (canSetSpawn(level)) {
            ServerPlayer serverPlayer;
            if (!(level.isClientSide
                    || (serverPlayer = (ServerPlayer) player).getRespawnDimension() == level.dimension()
                            && pos.equals((Object) serverPlayer.getRespawnPosition()))) {
                if (serverPlayer.getRespawnDimension() != Level.END) {
                    var p = (IServerPlayerMixin) (Object) serverPlayer;
                    p.end_respawn_anchor$setPreBlockPos(serverPlayer.getRespawnPosition());
                    p.end_respawn_anchor$setPreRespawnDimension(serverPlayer.getRespawnDimension());
                    p.end_respawn_anchor$setPreRespawnAngle(serverPlayer.getRespawnAngle());
                }
                serverPlayer.setRespawnPosition(level.dimension(), pos, 0.0f, false, true);
                level.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5,
                        SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            super.explode(state, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isRespawnFuel(ItemStack stack) {
        return stack.is(Items.ENDER_EYE) || stack.is(Items.ENDER_PEARL);
    }

    public static boolean canSetSpawn(Level level) {
        return level.dimension().equals(Level.END);
    }
}
