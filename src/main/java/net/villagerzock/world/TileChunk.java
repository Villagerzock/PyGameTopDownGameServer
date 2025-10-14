package net.villagerzock.world;

import java.util.ArrayList;
import java.util.List;

public class TileChunk {
    private int size = 32;
    private final int[] tiles;

    public int[] getTiles() {
        return tiles;
    }

    public  TileChunk(int size, int[] tiles) {
        this.size = size;
        this.tiles = new int[size*size];
        for (int i = 0; i < size*size; i++) {
            if (i<tiles.length) {
                this.tiles[i] = tiles[i];
            }else {
                this.tiles[i] = -1;
            }
        }
    }
    public TileChunk(int[] tiles) {
        this.tiles = new int[size*size];
        for (int i = 0; i < size*size; i++) {
            if (i<tiles.length) {
                this.tiles[i] = tiles[i];
            }else {
                this.tiles[i] = -1;
            }
        }
    }
    public TileChunk() {
        this.tiles = new int[size*size];
        for (int i = 0; i < size*size; i++) {
            this.tiles[i] = -1;
        }
    }

    public int getSize() {
        return size;
    }
    public TileType getType(int index) {
        return TileRegistry.getTile(tiles[index]);
    }
    public TileType getType(int x, int y) {
        return TileRegistry.getTile(tiles[y*size+x]);
    }
}
