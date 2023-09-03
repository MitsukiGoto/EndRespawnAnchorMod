package com.github.mikn.end_respawn_anchor.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class PlayerDataCapabilityAttacher {
    public static void attach(final AttachCapabilitiesEvent<Entity> event) {
        final PlayerDataCapabilityProvider provider = new PlayerDataCapabilityProvider();
        event.addCapability(PlayerDataCapabilityProvider.IDENTIFIER, provider);
    }
}
