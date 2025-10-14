package net.villagerzock.entity;

import net.villagerzock.GameMath;

import java.util.UUID;

public interface EntityFactory<T extends Entity> {
    T construct(UUID uuid, GameMath.Vector2i position, String dimension);
}
