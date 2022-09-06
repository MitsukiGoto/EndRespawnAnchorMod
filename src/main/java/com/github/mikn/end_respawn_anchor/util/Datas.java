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

