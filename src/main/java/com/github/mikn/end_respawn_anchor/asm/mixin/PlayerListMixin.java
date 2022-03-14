package com.github.mikn.end_respawn_anchor.asm.mixin;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import com.github.mikn.end_respawn_anchor.util.OtherDimensionSpawnPosition;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    /**
     * @reason it is hard to make it achieve without directly modifying the vanilla code
     * this is targeting respawn method
     * In the development environment, this works with method name, respawn
     * but when it runs on the production environment, it works with Notch Name
     * @author Mikn
     */
    @Overwrite
    public ServerPlayerEntity respawn(ServerPlayerEntity p_11237_, boolean p_11238_) {
        PlayerList playerList = (PlayerList) (Object) this;
        boolean isDead = p_11237_.isDeadOrDying();
        boolean isAlive = !isDead;
        boolean isDifferentWithDefault = false;

        RegistryKey<World> dimension = p_11237_.getLevel().dimension();
        ServerWorld serverlevel1;
        playerList.removePlayer(p_11237_);
        p_11237_.getLevel().removePlayer(p_11237_, true); // Forge: keep data until copyFrom called
        BlockPos blockpos = p_11237_.getRespawnPosition();
        float f = p_11237_.getRespawnAngle();
        boolean flag = p_11237_.isRespawnForced();
        ServerWorld serverWorld = playerList.server.getLevel(p_11237_.getRespawnDimension());
        Optional<Vector3d> optional;

        if (serverWorld == null || blockpos == null) {
            optional = Optional.empty();
            serverlevel1 = playerList.server.overworld();
        } else if ((isDead && dimension == World.END && serverWorld.dimension() == World.END) || (isDead && dimension == World.END && serverWorld.dimension() != World.END) || (isDead && dimension != World.END && serverWorld.dimension() != World.END))  {
            optional = PlayerEntity.findRespawnPositionAndUseSpawnBlock(serverWorld, blockpos, f, flag, p_11238_);
            serverlevel1 = optional.isPresent() ? serverWorld : playerList.server.overworld();
        } else if ((isAlive && dimension == World.END && serverWorld.dimension() == World.END && EndRespawnAnchor.spawnPositions.entrySet().stream().anyMatch(entry -> entry.getKey().equals(p_11237_.getUUID()))) || (isDead && dimension != World.END && serverWorld.dimension() == World.END && EndRespawnAnchor.spawnPositions.entrySet().stream().anyMatch(entry -> entry.getKey().equals(p_11237_.getUUID())))) {
            OtherDimensionSpawnPosition position = EndRespawnAnchor.spawnPositions.get(p_11237_.getUUID());
            optional = EndRespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, playerList.server.getLevel(position.getDimension()), position.getBlockPos());
            serverlevel1 = playerList.server.getLevel(position.getDimension());
            isDifferentWithDefault = true;
        } else if(isAlive && dimension == World.END && serverWorld.dimension() != World.END) {
            if (serverWorld.dimension() == World.NETHER) {
                p_11237_.sendMessage(new StringTextComponent("You spawn in Nether because you used RespawnAnchor"), p_11237_.getUUID());
            }
            optional = PlayerEntity.findRespawnPositionAndUseSpawnBlock(serverWorld, blockpos, f, flag, p_11238_);
            serverlevel1 = optional.isPresent() ? serverWorld : playerList.server.overworld();
        } else {
            optional = Optional.empty();
            serverlevel1 = playerList.server.overworld();
        }

        PlayerInteractionManager playerinteractionmanager;
        if (playerList.server.isDemo()) {
            playerinteractionmanager = new DemoPlayerInteractionManager(serverlevel1);
        } else {
            playerinteractionmanager = new PlayerInteractionManager(serverlevel1);
        }

        ServerPlayerEntity serverplayerentity = new ServerPlayerEntity(playerList.server, serverlevel1, p_11237_.getGameProfile(), playerinteractionmanager);
        serverplayerentity.connection = p_11237_.connection;
        serverplayerentity.restoreFrom(p_11237_, p_11238_);
        p_11237_.remove(false); // Forge: clone event had a chance to see old data, now discard it
        serverplayerentity.setId(p_11237_.getId());
        serverplayerentity.setMainArm(p_11237_.getMainArm());

        for(String s : p_11237_.getTags()) {
            serverplayerentity.addTag(s);
        }

        playerList.updatePlayerGameMode(serverplayerentity, p_11237_, serverlevel1);
        boolean flag2 = false;
        if (optional.isPresent()) {
            BlockState blockstate = serverlevel1.getBlockState(blockpos);
            boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
            Vector3d vector3d = optional.get();
            float f1;
            if (!blockstate.is(BlockTags.BEDS) && !flag1) {
                f1 = f;
            } else {
                Vector3d vector3d1 = Vector3d.atBottomCenterOf(blockpos).subtract(vector3d).normalize();
                f1 = (float)MathHelper.wrapDegrees(MathHelper.atan2(vector3d1.z, vector3d1.x) * (double)(180F / (float)Math.PI) - 90.0D);
            }

            serverplayerentity.moveTo(vector3d.x, vector3d.y, vector3d.z, f1, 0.0F);
            if(isDifferentWithDefault) {
                OtherDimensionSpawnPosition position = EndRespawnAnchor.spawnPositions.get(p_11237_.getUUID());
                serverplayerentity.setRespawnPosition(position.getDimension(), position.getBlockPos(), position.getRespawnAngle(), flag, false);
            } else {
                serverplayerentity.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag, false);
            }
            flag2 = !p_11238_ && flag1;
        } else if (blockpos != null) {
            serverplayerentity.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }

        while(!serverlevel1.noCollision(serverplayerentity) && serverplayerentity.getY() < 256.0D) {
            serverplayerentity.setPos(serverplayerentity.getX(), serverplayerentity.getY() + 1.0D, serverplayerentity.getZ());
        }

        IWorldInfo iworldinfo = serverplayerentity.level.getLevelData();
        serverplayerentity.connection.send(new SRespawnPacket(serverplayerentity.level.dimensionType(), serverplayerentity.level.dimension(), BiomeManager.obfuscateSeed(serverplayerentity.getLevel().getSeed()), serverplayerentity.gameMode.getGameModeForPlayer(), serverplayerentity.gameMode.getPreviousGameModeForPlayer(), serverplayerentity.getLevel().isDebug(), serverplayerentity.getLevel().isFlat(), p_11238_));
        serverplayerentity.connection.teleport(serverplayerentity.getX(), serverplayerentity.getY(), serverplayerentity.getZ(), serverplayerentity.yRot, serverplayerentity.xRot);
        serverplayerentity.connection.send(new SWorldSpawnChangedPacket(serverlevel1.getSharedSpawnPos(), serverlevel1.getSharedSpawnAngle()));
        serverplayerentity.connection.send(new SServerDifficultyPacket(iworldinfo.getDifficulty(), iworldinfo.isDifficultyLocked()));
        serverplayerentity.connection.send(new SSetExperiencePacket(serverplayerentity.experienceProgress, serverplayerentity.totalExperience, serverplayerentity.experienceLevel));
        playerList.sendLevelInfo(serverplayerentity, serverlevel1);
        playerList.sendPlayerPermissionLevel(serverplayerentity);
        serverlevel1.addRespawnedPlayer(serverplayerentity);
        playerList.addPlayer(serverplayerentity);
        playerList.playersByUUID.put(serverplayerentity.getUUID(), serverplayerentity);
        serverplayerentity.initMenu();
        serverplayerentity.setHealth(serverplayerentity.getHealth());
        net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerRespawnEvent(serverplayerentity, p_11238_);
        if (flag2) {
            serverplayerentity.connection.send(new SPlaySoundEffectPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0F, 1.0F));
        }

        return serverplayerentity;
    }
}
