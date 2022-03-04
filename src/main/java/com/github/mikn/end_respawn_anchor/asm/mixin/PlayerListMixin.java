package com.github.mikn.end_respawn_anchor.asm.mixin;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.util.OtherDimensionSpawnPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    /**
     * @reason it is hard to make it done without directly modifying the vanilla code
     * @author Mikn */
    @Overwrite
    public ServerPlayer respawn(ServerPlayer p_11237_, boolean p_11238_) {
        boolean isDeadAtTheEnd = false;
        UUID uuid = p_11237_.getUUID();
        if(p_11237_.getLevel().dimension()==Level.END) {
            isDeadAtTheEnd = true;
        }
        PlayerList playerList = (PlayerList) (Object) this;
        playerList.players.remove(p_11237_);
        p_11237_.getLevel().removePlayerImmediately(p_11237_, Entity.RemovalReason.DISCARDED);
        final BlockPos blockpos = p_11237_.getRespawnPosition();
        final float f = p_11237_.getRespawnAngle();
        final ResourceKey<Level> dimension = p_11237_.getRespawnDimension();
        ServerLevel serverlevel = playerList.server.getLevel(dimension);
        boolean flag = p_11237_.isRespawnForced();
        Optional<Vec3> optional;
        if (serverlevel != null && blockpos != null) {
            if (!isDeadAtTheEnd && !EndRespawnAnchor.spawnPositions.isEmpty()) {
                OtherDimensionSpawnPosition position = EndRespawnAnchor.spawnPositions.get(uuid);
                p_11237_.setRespawnPosition(position.dimension, position.blockPos, position.respawnAngle, false, true);
            }
            optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag, p_11238_);
        } else {
            optional = Optional.empty();
        }

        ServerLevel serverlevel1 = serverlevel != null && optional.isPresent() ? serverlevel : playerList.server.overworld();
        ServerPlayer serverplayer = new ServerPlayer(playerList.server, serverlevel1, p_11237_.getGameProfile());
        serverplayer.connection = p_11237_.connection;
        serverplayer.restoreFrom(p_11237_, p_11238_);
        serverplayer.setId(p_11237_.getId());
        serverplayer.setMainArm(p_11237_.getMainArm());

        for(String s : p_11237_.getTags()) {
            serverplayer.addTag(s);
        }

        boolean flag2 = false;
        if (optional.isPresent()) {
            BlockState blockstate = serverlevel1.getBlockState(blockpos);
            boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
            Vec3 vec3 = optional.get();
            float f1;
            if (!blockstate.is(BlockTags.BEDS) && !flag1) {
                f1 = f;
            } else {
                Vec3 vec31 = Vec3.atBottomCenterOf(blockpos).subtract(vec3).normalize();
                f1 = (float) Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double)(180F / (float)Math.PI) - 90.0D);
            }

            serverplayer.moveTo(vec3.x, vec3.y, vec3.z, f1, 0.0F);
            serverplayer.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag, false);
            flag2 = !p_11238_ && flag1;
        } else if (blockpos != null) {
            serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }

        while(!serverlevel1.noCollision(serverplayer) && serverplayer.getY() < (double)serverlevel1.getMaxBuildHeight()) {
            serverplayer.setPos(serverplayer.getX(), serverplayer.getY() + 1.0D, serverplayer.getZ());
        }

        LevelData leveldata = serverplayer.level.getLevelData();
        serverplayer.connection.send(new ClientboundRespawnPacket(serverplayer.level.dimensionType(), serverplayer.level.dimension(), BiomeManager.obfuscateSeed(serverplayer.getLevel().getSeed()), serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer(), serverplayer.getLevel().isDebug(), serverplayer.getLevel().isFlat(), p_11238_));
        serverplayer.connection.teleport(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), serverplayer.getYRot(), serverplayer.getXRot());
        serverplayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverlevel1.getSharedSpawnPos(), serverlevel1.getSharedSpawnAngle()));
        serverplayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        serverplayer.connection.send(new ClientboundSetExperiencePacket(serverplayer.experienceProgress, serverplayer.totalExperience, serverplayer.experienceLevel));
        playerList.sendLevelInfo(serverplayer, serverlevel1);
        playerList.sendPlayerPermissionLevel(serverplayer);
        serverlevel1.addRespawnedPlayer(serverplayer);
        playerList.addPlayer(serverplayer);
        playerList.playersByUUID.put(serverplayer.getUUID(), serverplayer);
        serverplayer.initInventoryMenu();
        serverplayer.setHealth(serverplayer.getHealth());
        net.minecraftforge.event.ForgeEventFactory.firePlayerRespawnEvent(serverplayer, p_11238_);
        if (flag2) {
            serverplayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0F, 1.0F));
        }
        p_11237_.setRespawnPosition(dimension, blockpos, f, false, true);
        return serverplayer;
    }
}
