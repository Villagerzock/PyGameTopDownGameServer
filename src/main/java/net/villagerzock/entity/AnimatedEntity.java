package net.villagerzock.entity;

public interface AnimatedEntity extends Entity{
    int getAnimation();
    void setAnimation(int animation);

    int getAnimationTick();
    void setAnimationTick(int animationTick);
}
