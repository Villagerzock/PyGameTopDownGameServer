package net.villagerzock.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.villagerzock.GameMath;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class World {
    private static final Map<String, World> worlds = new HashMap<>();

    private final int chunkSize;
    private final Map<GameMath.Vector2i, TileChunk> chunks = new HashMap<>(); // Key: "cx,cy"

    private World(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public static World getWorld(String name) {
        if (worlds.containsKey(name)) {
            return worlds.get(name);
        }

        String path = "dim/" + name + ".json";
        try (FileReader reader = new FileReader(path)) {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            // --- Meta.chunk_size laden, default = 32 ---
            int chunkSize = 32;
            if (root.has("meta")) {
                JsonObject meta = root.getAsJsonObject("meta");
                if (meta.has("chunk_size")) {
                    chunkSize = meta.get("chunk_size").getAsInt();
                }
            }

            World world = new World(chunkSize);

            // --- Chunks laden ---
            JsonArray chunksArr = root.getAsJsonArray("chunks");
            for (int i = 0; i < chunksArr.size(); i++) {
                JsonObject chunkObj = chunksArr.get(i).getAsJsonObject();

                // Position
                JsonArray posArr = chunkObj.getAsJsonArray("pos");
                int cx = posArr.get(0).getAsInt();
                int cy = posArr.get(1).getAsInt();

                // Tiles
                JsonArray tilesArr = chunkObj.getAsJsonArray("tiles");
                int[] tiles = new int[tilesArr.size()];
                for (int t = 0; t < tilesArr.size(); t++) {
                    if (tilesArr.get(t).isJsonArray()) {
                        // Spezialtile: Array -> int + tile data obj
                        JsonArray special = tilesArr.get(t).getAsJsonArray();
                        tiles[t] = special.get(0).getAsInt(); // nur den int nutzen
                    } else {
                        tiles[t] = tilesArr.get(t).getAsInt();
                    }
                }

                TileChunk chunk = new TileChunk(chunkSize, tiles);
                world.chunks.put(new GameMath.Vector2i(cx,cy), chunk);
            }

            worlds.put(name, world);
            return world;
        } catch (Exception e) {
            e.printStackTrace();
            return new World(32); // Fallback: leere Welt
        }
    }
    public GameMath.Vector2i getChunkPos(GameMath.Vector2i globalPos) {
        return new GameMath.Vector2i(Math.floorDiv(Math.floorDiv(globalPos.x, 64), chunkSize), Math.floorDiv(Math.floorDiv(globalPos.y, 64), chunkSize));
    }

    public TileChunk getChunk(int cx, int cy) {
        for (GameMath.Vector2i pos : this.chunks.keySet()) {
            if (pos.x == cx && pos.y == cy) {
                return chunks.get(pos);
            }
        }
        return new TileChunk();
    }

    /**
     * Holt den Tiletyp an einer Welt-Tile-Koordinate.
     * @param tileX X-Koordinate in Tiles
     * @param tileY Y-Koordinate in Tiles
     */
    public TileType getTileType(int tileX, int tileY) {
        int cx = Math.floorDiv(tileX, chunkSize);
        int cy = Math.floorDiv(tileY, chunkSize);

        TileChunk chunk = getChunk(cx, cy);
        if (chunk == null) return TileRegistry.empty;

        int localX = Math.floorMod(tileX, chunkSize);
        int localY = Math.floorMod(tileY, chunkSize);

        return chunk.getType(localX, localY);
    }

    /**
     * Holt den Tiletyp an Pixel-Koordinaten.
     */
    public TileType getTileTypeAtPixel(int worldX, int worldY, int tileSize) {
        int tileX = Math.floorDiv(worldX, tileSize);
        int tileY = Math.floorDiv(worldY, tileSize);
        return getTileType(tileX, tileY);
    }
}