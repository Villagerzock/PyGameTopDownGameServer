package net.villagerzock.world;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class TileRegistry {
    public static List<TileType> tiles;
    public static final TileType empty = new TileType(new boolean[]{false, false, false, false, false, false, false, false, false});
    public static void loadTiles(String path) throws Exception {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<TileType>>() {}.getType();
        tiles = gson.fromJson(new FileReader(path), listType);
    }

    public static TileType getTile(int typeId) {
        if (typeId == -1){
            return empty;
        }
        return tiles.get(typeId);
    }
}
