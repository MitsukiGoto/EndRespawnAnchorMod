package com.github.mikn.end_respawn_anchor.asm.mixin;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method= "findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;",at=@At("HEAD"), cancellable = true)
    private static void inject(ServerLevel level, BlockPos blockPos, float flag1, boolean p_36131_, boolean p_36132_, CallbackInfoReturnable<Optional<Vec3>> cir) {
        BlockState blockstate = level.getBlockState(blockPos);
        Block block = blockstate.getBlock();
        if (block instanceof EndRespawnAnchorBlock && blockstate.getValue(EndRespawnAnchorBlock.CHARGE) > 0 && EndRespawnAnchorBlock.canSetSpawn(level)) {
            Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, blockPos);
            if (!p_36132_ && optional.isPresent()) {
                level.setBlock(blockPos, blockstate.setValue(RespawnAnchorBlock.CHARGE, blockstate.getValue(RespawnAnchorBlock.CHARGE) - 1), 3);
            }
            cir.setReturnValue(optional);
        }
    }
}
