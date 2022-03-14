package com.github.mikn.end_respawn_anchor;

import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import com.github.mikn.end_respawn_anchor.config.EndRespawnAnchorConfig;
import com.github.mikn.end_respawn_anchor.event.FindRespawnPositionAndUseSpawnBlockEvent;
import com.github.mikn.end_respawn_anchor.init.BlockInit;
import com.github.mikn.end_respawn_anchor.init.ItemInit;
import com.github.mikn.end_respawn_anchor.util.EndRespawnAnchorData;
import com.github.mikn.end_respawn_anchor.util.OtherDimensionSpawnPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

import java.nio.file.Path;
import java.util.*;

import static net.minecraft.world.storage.FolderName.LEVEL_DATA_FILE;

@Mod(EndRespawnAnchor.MODID)
public class EndRespawnAnchor {
    public static final String MODID = "end_respawn_anchor";
    public static final Logger LOGGER = LogManager.getLogger("EndRespawnAnchor/Main");
    public static Map<UUID, OtherDimensionSpawnPosition> spawnPositions = null;
    private Path path;
    private boolean onceLoad = true;
    private boolean onceUnload = true;

    public EndRespawnAnchor() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EndRespawnAnchorConfig.SPEC, "end_respawn_anchor-common.toml");
        BlockInit.BLOCKS.register(bus);
        ItemInit.ITEMS.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();
            MinecraftServer server = serverWorld.getServer();
            if(onceLoad) {
                this.path = server.getWorldPath(LEVEL_DATA_FILE).getParent().resolve("data/end_respawn_anchor.json");
                EndRespawnAnchorData data = new EndRespawnAnchorData(this.path);
                spawnPositions = data.read();
                onceLoad = false;
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if(event.getWorld() instanceof ServerWorld && onceUnload) {
            EndRespawnAnchorData data = new EndRespawnAnchorData(this.path);
            data.save(spawnPositions);
            onceUnload = false;
        }

    }

    @SubscribeEvent
    public void FindRespawnEvent(FindRespawnPositionAndUseSpawnBlockEvent evt) {
        World level = evt.getLevel();
        BlockPos blockPos = evt.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof EndRespawnAnchorBlock && blockState.getValue(EndRespawnAnchorBlock.CHARGE) > 0 && EndRespawnAnchorBlock.isEnd(level)) {
            Optional<Vector3d> optional = EndRespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, blockPos);
            if (!evt.getFlag() && optional.isPresent()) {
                level.setBlock(blockPos, blockState.setValue(EndRespawnAnchorBlock.CHARGE, blockState.getValue(EndRespawnAnchorBlock.CHARGE) - 1), 3);
                evt.setResult(Event.Result.ALLOW);
                return;
            }
        }
        evt.setResult(Event.Result.DENY);
    }
}
