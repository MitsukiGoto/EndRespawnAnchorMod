package com.github.mikn.end_respawn_anchor.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.world.World.*;

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
        map.forEach((key, value) -> datas.add(new DataModel(key.toString(), new Data(new Blockpos(value.getBlockPos()), provideDimension(value.getDimension()), String.valueOf(value.getRespawnAngle())))));
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.write(gson.toJson(datas));
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

    private RegistryKey<World> getDimension(String dimension) {
        if (dimension.equals("overworld")) {
            return OVERWORLD;
        } else if (dimension.equals("nether")) {
            return NETHER;
        } else if (dimension.equals("end")) {
            return END;
        }
        return OVERWORLD;
    }

    private String provideDimension(RegistryKey<World> level) {
        if (level.equals(OVERWORLD)) {
            return "overworld";
        } else if (level.equals(NETHER)) {
            return "nether";
        } else if (level.equals(END)) {
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
