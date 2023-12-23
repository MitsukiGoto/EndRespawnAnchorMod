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

import com.github.mikn.end_respawn_anchor.capabilities.PlayerDataCapability;
import com.github.mikn.end_respawn_anchor.capabilities.PlayerDataCapabilityAttacher;
import com.github.mikn.end_respawn_anchor.config.EndRespawnAnchorConfig;
import com.github.mikn.end_respawn_anchor.init.BlockInit;
import com.github.mikn.end_respawn_anchor.init.ItemInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Optional;
import java.nio.file.Path;

@Mod(EndRespawnAnchor.MODID)
public class EndRespawnAnchor {
    public static final String MODID = "end_respawn_anchor";
    public static final Logger LOGGER = LogManager.getLogger("EndRespawnAnchor/Main");

    public EndRespawnAnchor() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EndRespawnAnchorConfig.SPEC,
                "end_respawn_anchor-common.toml");
        BlockInit.BLOCKS.register(bus);
        ItemInit.ITEMS.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void attachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            PlayerDataCapabilityAttacher.attach(event);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(final PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerDataCapability.INSTANCE).ifPresent(cap -> event.getEntity()
                .getCapability(PlayerDataCapability.INSTANCE).ifPresent(c -> c.deserializeNBT(cap.serializeNBT())));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public void onPlayerLogIn(final PlayerEvent.PlayerLoggedInEvent event) {
        Entity entity = event.getEntity();
        Path path = entity.getServer().getWorldPath(LevelResource.LEVEL_DATA_FILE).getParent()
                .resolve("data/end_respawn_anchor.json");
        if (path.toFile().exists() && entity instanceof ServerPlayer serverplayer) {
            serverplayer.getCapability(PlayerDataCapability.INSTANCE, null).ifPresent(cap -> {
                Optional<RespawnData> data = ParseLegacyFile.getMatchingDataIfExists(path, serverplayer.getUUID());
                data.ifPresent(d -> {
                    cap.setValue(d);
                    LOGGER.info("{}'s respawn data is successfully converted", serverplayer.getName());
                });
            });
        }
    }
}
