import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ServerPlayer implements Player{
    GameMath.Vector2i position = new GameMath.Vector2i(0,0);
    String name;
    UUID uuid;
    DatagramSocket clientSocket;
    InetSocketAddress ip;
    int direction = 0;
    int animation = 0;
    int animationTick = 0;


    public ServerPlayer(String name, UUID uuid, DatagramSocket clientSocket, InetSocketAddress ip) {
        this.name = name;
        this.uuid = uuid;
        this.clientSocket = clientSocket;
        this.ip = ip;
    }

    @Override
    public GameMath.Vector2i getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void send(DatagramPacket packet) {
        try {
            this.clientSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getPacketData(byte packetType) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(packetType);
        buffer.putInt(this.uuid.toString().length());
        buffer.put(this.uuid.toString().getBytes());
        buffer.putInt(this.name.length());
        buffer.put(this.name.getBytes());
        buffer.putInt(this.position.x);
        buffer.putInt(this.position.y);
        buffer.putInt(this.direction);
        buffer.putInt(this.animation);
        buffer.putInt(this.animationTick);
        return buffer.array();
    }

    @Override
    public InetSocketAddress getAddress() {
        return ip;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public int getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public int getAnimation() {
        return animation;
    }

    @Override
    public void setAnimation(int animation) {
        this.animation = animation;
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int animationTick) {
        this.animationTick = animationTick;
    }
}
