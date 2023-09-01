package com.github.mikn.end_respawn_anchor.capabilities;

import com.github.mikn.end_respawn_anchor.util.RespawnData;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface IPlayerDataCapability extends INBTSerializable<CompoundTag> {
    RespawnData getRespawnData();
    void setValue(RespawnData respawnData);
}
