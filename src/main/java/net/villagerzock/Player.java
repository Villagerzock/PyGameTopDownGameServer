package net.villagerzock;

import net.villagerzock.entity.AnimatedEntity;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public interface Player extends AnimatedEntity {

    String getName();

    void send(DatagramPacket packet);

    InetSocketAddress getAddress();

    int getSkin();
    void setSkin(int skin);

    void save();
    default void tick(){

    }
    default Rect getBounds() {
        return new Rect(getPosition().x,getPosition().y,64,64);
    }
}
