package com.github.mikn.end_respawn_anchor.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class EndRespawnAnchorData extends SavedData {

    private String positionsString;

    public String getPositionsString() {
        return this.positionsString;
    }

    public void setPositionsString(String test) {
        this.positionsString = test;
        this.setDirty();
    }

    public static EndRespawnAnchorData create() {
        return new EndRespawnAnchorData();
    }

    public static EndRespawnAnchorData load(CompoundTag tag) {
        EndRespawnAnchorData data = create();
        String string = tag.getString("positionsString");
        data.positionsString = string;
        return data;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("positionsString", positionsString);
        return tag;
    }

    public static void computeIfAbsent(MinecraftServer server) {
        server.overworld().getDataStorage().computeIfAbsent(EndRespawnAnchorData::load, EndRespawnAnchorData::create, "positionsString");
    }

}
