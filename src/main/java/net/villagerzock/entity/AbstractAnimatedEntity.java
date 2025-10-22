package net.villagerzock.entity;

import net.villagerzock.ChatHandler;
import net.villagerzock.GameMath;
import net.villagerzock.PacketByteBuffer;
import net.villagerzock.Rect;
import net.villagerzock.world.TileChunk;
import net.villagerzock.world.TileRegistry;
import net.villagerzock.world.TileType;
import net.villagerzock.world.World;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.UUID;

public abstract class AbstractAnimatedEntity implements AnimatedEntity {
    private int animationTick = 0;
    private int animation = 0;
    private GameMath.Vector2i position = GameMath.Vector2i.ZERO;
    private int direction = 0;
    private final UUID uuid;
    private String dimension = "overworld";
    private int maxHealth = getMaxHealth();
    private int health = maxHealth;

    protected abstract int getMaxHealth();

    protected AbstractAnimatedEntity(UUID uuid) {
        this.uuid = uuid;
    }
    protected abstract GameMath.Vector2i getSize();
    protected void writePacketData(PacketByteBuffer buf){

    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public void damage(int damage) {
        this.health -= damage;
    }

    @Override
    public byte[] getPacketData(byte packetType) {
        PacketByteBuffer buf = new PacketByteBuffer(packetType);

        buf.putString(uuid.toString());
        buf.putInt(animationTick);
        buf.putInt(animation);
        buf.putInt(position.x);
        buf.putInt(position.y);
        buf.putInt(direction);
        buf.putString(dimension);

        writePacketData(buf);
        return buf.array();
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int animationTick) {
        this.animationTick = animationTick;
    }

    @Override
    public int getAnimation() {
        return animation;
    }

    @Override
    public void setAnimation(int animation) {
        if (animation != this.animation) {
            this.animationTick = 0;
        }
        this.animation = animation;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int getDirection() {
        return direction;
    }

    @Override
    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public GameMath.Vector2i getPosition() {
        return position;
    }

    public boolean setPosition(GameMath.Vector2i newPos) {
        World world   = World.getWorld(dimension);
        int chunkSize = world.getChunkSize();           // Tiles pro Chunk-Seite (z.B. 32)
        int tileSize  = 64;

        // Aktuelles Bounds und Zielverschiebung
        Rect curr = getBounds();
        int dx = newPos.x - this.position.x;
        int dy = newPos.y - this.position.y;

        // Wie im Python-Code: aktuelles, X-verschobenes und Y-verschobenes Rect
        Rect rectCurrent = curr.move(dx, dy);   // entspricht local_rect_current
        Rect rectX       = curr.move(dx, 0);    // entspricht local_rect_x
        Rect rectY       = curr.move(0, dy);    // entspricht local_rect_y

        boolean canMoveX = true;
        boolean canMoveY = true;

        // Wir schauen wie im Python-Code in einem 3x3-Fenster um die Zielposition
        int baseX = newPos.x;
        int baseY = newPos.y;

        outer:
        for (int ox = -1; ox <= 1; ox++) {
            for (int oy = -1; oy <= 1; oy++) {

                int wx = baseX + ox * tileSize;
                int wy = baseY + oy * tileSize;

                // Welt-Tile-Koordinaten (floor-div für negative Koords!)
                int worldTileX = Math.floorDiv(wx, tileSize);
                int worldTileY = Math.floorDiv(wy, tileSize);

                // Chunk + lokaler Index (row-major)
                int chunkX = Math.floorDiv(worldTileX, chunkSize);
                int chunkY = Math.floorDiv(worldTileY, chunkSize);
                TileChunk chunk = world.getChunk(chunkX, chunkY);
                if (chunk == null) continue;

                int localX = Math.floorMod(worldTileX, chunkSize);
                int localY = Math.floorMod(worldTileY, chunkSize);
                int localIndex = localY * chunkSize + localX;

                int[] tiles = chunk.getTiles();
                if (tiles == null || localIndex < 0 || localIndex >= tiles.length) continue;

                int tileId = tiles[localIndex];
                boolean[] collision = TileRegistry.getTile(tileId).collision;
                if (collision == null || collision.length == 0) continue;

                // Tile-Rect in Pixeln (für "enter/leave" o.ä. – optional)
                Rect tileRect = new Rect(worldTileX * tileSize, worldTileY * tileSize, tileSize, tileSize);
                // Beispiel: wenn du Enter/Leave brauchst
                // if (insideTilesLastFrame.contains(new Pair(worldTileX, worldTileY))) { ... }

                // 3x3 Kollisionspunkte (TOP_LEFT: links->rechts, oben->unten)
                int count = Math.min(9, collision.length);
                for (int i = 0; i < count; i++) {
                    if (!collision[i]) continue;

                    float px = (i % 3) / 2.0f;   // 0, 0.5, 1.0
                    float py = (i / 3) / 2.0f;

                    float worldPx = (worldTileX + px) * tileSize;
                    float worldPy = (worldTileY + py) * tileSize;

                    // Achsen-separierte Tests wie im Python-Code
                    if (rectX.collidepoint(worldPx, worldPy)) canMoveX = false;
                    if (rectY.collidepoint(worldPx, worldPy)) canMoveY = false;

                    if (!canMoveX && !canMoveY) break outer;
                }
            }
        }

        // Bewegung wie üblich achsenweise „sliden“
        int finalX = this.position.x + (canMoveX ? dx : 0);
        int finalY = this.position.y + (canMoveY ? dy : 0);
        this.position = new GameMath.Vector2i(finalX, finalY);

        // True, wenn wenigstens eine Achse bewegen durfte
        return (canMoveX || canMoveY);
    }
    @Override
    public String getDimension() {
        return dimension;
    }

    @Override
    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public final Rect getBounds() {
        return new Rect(getPosition().x,getPosition().y,this.getSize().x,this.getSize().y);
    }
}
