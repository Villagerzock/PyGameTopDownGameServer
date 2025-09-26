import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.UUID;

public interface Player {
    GameMath.Vector2i getPosition();
    String getName();
    void send(DatagramPacket packet);

    byte[] getPacketData(byte packetType);

    InetSocketAddress getAddress();

    UUID getUUID();

    int getDirection();
    void setDirection(int direction);

    int getAnimation();
    void setAnimation(int animation);

    int getAnimationTick();
    void setAnimationTick(int animationTick);
}
