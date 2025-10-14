package net.villagerzock.world;

public class TileType {
    public boolean[] collision; // Länge 9, 3x3

    public TileType(boolean[] collision) {
        this.collision = collision;
    }
}