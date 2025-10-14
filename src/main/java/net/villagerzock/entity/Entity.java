package net.villagerzock.entity;


import net.villagerzock.GameMath;
import net.villagerzock.Rect;

import java.util.UUID;

public interface Entity {
    GameMath.Vector2i getPosition();
    String getDimension();
    void setDimension(String dimension);
    int getHealth();
    void setHealth(int health);
    void damage(int damage);

    byte[] getPacketData(byte packetType);

    UUID getUUID();

    int getDirection();
    void setDirection(int direction);

    void tick();

    Rect getBounds();
    String getType();
}
