package com.github.mikn.end_respawn_anchor.util;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;

public class Datas {
    private ArrayList<DataModel> list = new ArrayList<>();
    public void add(DataModel model) {
        list.add(model);
    }
    public ArrayList<DataModel> getList() {
        return this.list;
    }
}

class DataModel {
    String uuid;
    Data data;
    public DataModel(String uuid, Data data) {
        this.uuid = uuid;
        this.data = data;
    }
}

class Blockpos {

    int x;
    int y;
    int z;

    public Blockpos(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x,y,z);
    }
}

class Data {
    Blockpos blockpos;
    String dimension;
    String respawnAngle;
    public Data(Blockpos blockpos, String dimension, String respawnAngle) {
        this.blockpos = blockpos;
        this.dimension = dimension;
        this.respawnAngle = respawnAngle;
    }
}

