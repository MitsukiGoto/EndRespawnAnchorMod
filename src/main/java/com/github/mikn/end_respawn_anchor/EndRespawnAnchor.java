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

package com.github.mikn.end_respawn_anchor;

import com.github.mikn.end_respawn_anchor.config.EndRespawnAnchorConfig;
import com.github.mikn.end_respawn_anchor.init.BlockInit;
import com.github.mikn.end_respawn_anchor.init.ItemInit;
import com.github.mikn.end_respawn_anchor.util.EndRespawnAnchorData;
import com.github.mikn.end_respawn_anchor.util.RespawnData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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

@Mod(EndRespawnAnchor.MODID)
public class EndRespawnAnchor {
    public static final String MODID = "end_respawn_anchor";
    public static final Logger LOGGER = LogManager.getLogger("EndRespawnAnchor/Main");
    public static Map<UUID, RespawnData> spawnPositions = null;
    private Path path;

    public EndRespawnAnchor() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::registerCreativeTabs);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EndRespawnAnchorConfig.SPEC, "end_respawn_anchor-common.toml");
        BlockInit.BLOCKS.register(bus);
        ItemInit.ITEMS.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCreativeTabs(final BuildCreativeModeTabContentsEvent evt) {
        if(evt.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            evt.accept(ItemInit.END_RESPAWN_ANCHOR);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null) {
            this.path = server.getWorldPath(LevelResource.LEVEL_DATA_FILE).getParent().resolve("data/end_respawn_anchor.json");
            EndRespawnAnchorData data = new EndRespawnAnchorData(this.path);
            spawnPositions = data.read();
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null) {
            EndRespawnAnchorData data = new EndRespawnAnchorData(this.path);
            data.save(spawnPositions);
        }
    }
}
