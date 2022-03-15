package com.github.mikn.end_respawn_anchor.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EndRespawnAnchorData {

    private final File file;
    private final Gson gson;

    public EndRespawnAnchorData(Path path) {
        this.file = new File(path.toString());
        createIfAbsent(file);
        gson = new Gson();
    }

    public void save(Map<UUID, OtherDimensionSpawnPosition> map) {
        Datas datas = new Datas();
        map.forEach((key, value) -> datas.add(new DataModel(key.toString(), new Data(new Blockpos(value.blockPos()), provideDimension(value.dimension()), String.valueOf(value.respawnAngle())))));
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            var json = gson.toJson(datas);
            pw.write(json);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, OtherDimensionSpawnPosition> read() {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonReader jsr = new JsonReader(isr);
        Datas datas = new Datas();
        datas = gson.fromJson(jsr, Datas.class);
        Map<UUID, OtherDimensionSpawnPosition> map = new HashMap<>();
        if(datas != null) {
            for (DataModel model : datas.getList()) {
                map.put(UUID.fromString(model.uuid), new OtherDimensionSpawnPosition(getDimension(model.data.dimension), model.data.blockpos.getBlockPos(), Float.parseFloat(model.data.respawnAngle)));
            }
        }
        return map;
    }

    private ResourceKey<Level> getDimension(String dimension) {
        if (dimension.equals("overworld")) {
            return Level.OVERWORLD;
        } else if (dimension.equals("nether")) {
            return Level.NETHER;
        } else if (dimension.equals("end")) {
            return Level.END;
        }
        return Level.OVERWORLD;
    }

    private String provideDimension(ResourceKey<Level> level) {
        if (level == Level.OVERWORLD) {
            return "overworld";
        } else if (level == Level.NETHER) {
            return "nether";
        } else if (level == Level.END) {
            return "end";
        }
        return "overworld";
    }

    private void createIfAbsent(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
