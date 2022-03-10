package com.github.mikn.end_respawn_anchor;

import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import com.github.mikn.end_respawn_anchor.config.EndRespawnAnchorConfig;
import com.github.mikn.end_respawn_anchor.event.FindRespawnPositionAndUseSpawnBlockEvent;
import com.github.mikn.end_respawn_anchor.init.BlockInit;
import com.github.mikn.end_respawn_anchor.init.ItemInit;
import com.github.mikn.end_respawn_anchor.util.EndRespawnAnchorData;
import com.github.mikn.end_respawn_anchor.util.OtherDimensionSpawnPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.NbtComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod(EndRespawnAnchor.MODID)
public class EndRespawnAnchor {
    public static final String MODID = "end_respawn_anchor";
    public static final Logger LOGGER = LogManager.getLogger("EndRespawnAnchor/Main");
    public static Map<UUID, OtherDimensionSpawnPosition> spawnPositions = new HashMap<>();
    public static String string;

    public EndRespawnAnchor() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EndRespawnAnchorConfig.SPEC, "end_respawn_anchor-common.toml");
        BlockInit.BLOCKS.register(bus);
        ItemInit.ITEMS.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        EndRespawnAnchorData.computeIfAbsent(event.getWorld().getServer());
        CompoundTag tag = new CompoundTag();
        EndRespawnAnchorData test = EndRespawnAnchorData.load(tag);
        test.setPositionsString("aaa");
        string = test.getPositionsString();
        LOGGER.error(string);
    }

    @SubscribeEvent
    public void FindRespawnEvent(FindRespawnPositionAndUseSpawnBlockEvent evt) {
        Level level = evt.getLevel();
        BlockPos blockPos = evt.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof EndRespawnAnchorBlock && blockState.getValue(EndRespawnAnchorBlock.CHARGE) > 0 && EndRespawnAnchorBlock.isEnd(level)) {
            Optional<Vec3> optional = EndRespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, blockPos);
            if (!evt.getFlag() && optional.isPresent()) {
                level.setBlock(blockPos, blockState.setValue(EndRespawnAnchorBlock.CHARGE, blockState.getValue(EndRespawnAnchorBlock.CHARGE) - 1), 3);
                evt.setResult(Event.Result.ALLOW);
                return;
            }
        }
        evt.setResult(Event.Result.DENY);
    }
}
