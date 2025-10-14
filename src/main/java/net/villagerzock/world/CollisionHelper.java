package net.villagerzock.world;

public class CollisionHelper {
    public static final int TILE_SIZE = 64;

    /**
     * Prüft, ob an einer Weltposition (Pixel-Koordinaten) eine Kollision existiert.
     */
    public static boolean isSolidAt(double worldX, double worldY, int tileX, int tileY, int tileTypeId) {
        TileType type = TileRegistry.getTile(tileTypeId);
        if (type == null || type.collision == null) return false;

        // Lokale Position innerhalb des Tiles (0..64)
        double localX = worldX - tileX * TILE_SIZE;
        double localY = worldY - tileY * TILE_SIZE;

        // Normalisieren auf 0..1
        double nx = localX / TILE_SIZE;
        double ny = localY / TILE_SIZE;

        // In 3x3 Felder einordnen
        int cx = (int)Math.floor(nx * 3); // 0,1,2
        int cy = (int)Math.floor(ny * 3); // 0,1,2

        int index = cy * 3 + cx;

        if (index < 0 || index >= type.collision.length) return false;
        return type.collision[index];
    }
}

