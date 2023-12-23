/*
 Copyright (c) 2023 Mikndesu

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

import org.jline.utils.InputStreamReader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.UUID;
import java.util.Optional;
import java.nio.file.Path;
import java.io.*;

public class ParseLegacyFile {
    public static Optional<RespawnData> getMatchingDataIfExists(Path path, UUID uuid) {
        Optional<RespawnData> optional = Optional.empty();
        Gson gson = new Gson();
        try {
            JsonObject elm = readJsonObject(path, gson);
            if (elm.isJsonArray()) {
                JsonArray array = elm.getAsJsonArray();
                for (int i = 0; i < array.size(); ++i) {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    if (uuid.equals(UUID.fromString(obj.get("uuid").getAsString()))) {
                        Data data = gson.fromJson(obj.get("data"), Data.class);
                        RespawnData respawnData = new RespawnData(getDimension(data.dimension()), data.blockpos().intoBlockPos(),
                                Float.parseFloat(data.respawnAngle()));
                        array.remove(i);
                        removeConvertedElement(array, path);
                        optional = Optional.of(respawnData);
                    }
                }
            }
        } catch (Exception e) {
            EndRespawnAnchor.LOGGER.error(e);
        }
        return optional;
    }

    private static JsonObject readJsonObject(Path path, Gson gson) throws FileNotFoundException {
        InputStreamReader isr = new InputStreamReader(new FileInputStream(path.toFile()));
        JsonReader reader = new JsonReader(isr);
        JsonObject root = gson.fromJson(reader, JsonObject.class);
        return root;
    }

    private static void removeConvertedElement(JsonArray array, Path path) throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("list", array);
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile())))) {
            pw.write(obj.toString());
        }
    }

    private static ResourceKey<Level> getDimension(String dimension) {
        if (dimension.equals("overworld")) {
            return Level.OVERWORLD;
        } else if (dimension.equals("nether")) {
            return Level.NETHER;
        } else if (dimension.equals("end")) {
            return Level.END;
        }
        return Level.OVERWORLD;
    }
}


record Blockpos(int x, int y, int z) {
    public BlockPos intoBlockPos() {
        return new BlockPos(x, y, z);
    }
}


record Data(Blockpos blockpos, String dimension, String respawnAngle) {

}
