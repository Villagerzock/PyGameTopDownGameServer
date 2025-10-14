package net.villagerzock.entity;

import net.villagerzock.*;
import net.villagerzock.world.CollisionHelper;
import net.villagerzock.world.World;

import javax.swing.text.Position;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpinnerEntity extends AbstractAnimatedEntity {
    private boolean charging = false;
    private Direction chargeDir = null;
    private int speed = 8; // Charge-Speed in Pixel pro Tick
    private int cooldownTicks = 0; // Cooldown in Ticks

    // wie viele Tiles das "Kreuz" weit schaut
    private static final int VISION_TILES = 10;

    @Override
    protected int getMaxHealth() {
        return 50;
    }

    public SpinnerEntity(UUID uuid) {
        super(uuid);
    }
    public SpinnerEntity(UUID uuid, GameMath.Vector2i pos, String dimension) {
        super(uuid);
        this.setPosition(pos);
        this.setDimension(dimension);
    }

    @Override
    protected GameMath.Vector2i getSize() {
        return new GameMath.Vector2i(1, 1);
    }

    public void tick() {

        if (cooldownTicks > 0) {
            cooldownTicks--; // abwarten
            return;
        }

        if (charging) {
            moveCharge();

        } else {
            setAnimation(1);
            PlayerDirection target = getPlayerInCross(VISION_TILES * CollisionHelper.TILE_SIZE);
            if (target != null) {
                startCharge(target);
            }
        }
    }

    @Override
    public String getType() {
        return "spinner";
    }

    @Override
    public byte[] getPacketData(byte packetType) {
        this.setAnimationTick(getAnimationTick() + 1);
        return super.getPacketData(packetType);
    }

    private void startCharge(PlayerDirection target) {
        this.chargeDir = target.direction;
        if (directionBlockMap.getOrDefault(chargeDir,false)) {
            charging = false;
            return;
        }
        setAnimation(0);
        this.charging = true;
    }

    private PlayerDirection getPlayerInCross(int i) {
        Rect UP = new Rect(getPosition().x,getPosition().y-i,64,i);
        Rect DOWN = new Rect(getPosition().x,getPosition().y+64,64,i);
        Rect LEFT = new Rect(getPosition().x-i,getPosition().y,i,64);
        Rect RIGHT = new Rect(getPosition().x+64,getPosition().y,i,64);
        for (Player p : Main.players.values()) {
            if (p.getBounds().colliderect(UP)){
                return new PlayerDirection(p,Direction.UP);
            }else if (p.getBounds().colliderect(DOWN)){
                return new PlayerDirection(p,Direction.DOWN);
            }else if (p.getBounds().colliderect(LEFT)){
                return new PlayerDirection(p,Direction.LEFT);
            }else if (p.getBounds().colliderect(RIGHT)){
                return new PlayerDirection(p,Direction.RIGHT);
            }
        }
        return null;
    }
    private final Map<Direction, Boolean> directionBlockMap = new HashMap<>();
    private void moveCharge() {
        GameMath.Vector2i new_pos = getPosition();
        if (directionBlockMap.getOrDefault(chargeDir,false)) {
            charging = false;
            return;
        }
        switch (chargeDir) {
            case RIGHT:
                new_pos.x += speed;
            case LEFT:
                new_pos.x -= speed;
            case UP:
                new_pos.y -= speed;
            case DOWN:
                new_pos.y += speed;
        }
        boolean canMove = setPosition(new_pos);
        for (Player p : Main.players.values()) {
            if (p.getBounds().colliderect(this.getBounds())){
                System.out.println("Player Bounds: " + p.getBounds() + " Spinner Bounds: " + this.getBounds());
                cooldownTicks = 60;
                charging = false;
                p.damage(6);
            }
        }
        if (canMove) {
            directionBlockMap.clear();
        }else {
            directionBlockMap.put(chargeDir,true);
            cooldownTicks = 60;
            charging = false;
        }
    }


    /**
     * Hilfsklasse für Spieler + Richtung.
     */
    public static class PlayerDirection {
        public final Player player;
        public final Direction direction;

        public PlayerDirection(Player player, Direction direction) {
            this.player = player;
            this.direction = direction;
        }
    }
}
