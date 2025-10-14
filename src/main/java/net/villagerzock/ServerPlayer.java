package net.villagerzock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ServerPlayer implements Player{
    GameMath.Vector2i position = new GameMath.Vector2i(512,512);
    String name;
    UUID uuid;
    DatagramSocket clientSocket;
    InetSocketAddress ip;
    int direction = 0;
    int animation = 0;
    int animationTick = 0;
    int skin;
    String dimension = "dungeons";
    private int maxHearts = 3;
    private int health = maxHearts * 4;


    public ServerPlayer(String name, UUID uuid, DatagramSocket clientSocket, InetSocketAddress ip, int skin) {
        this.name = name;
        this.uuid = uuid;
        this.clientSocket = clientSocket;
        this.ip = ip;
        this.skin = skin;
        File playerDataFile = new File("./PlayerData/"+uuid.toString()+".json");
        if (playerDataFile.exists()) {
            try (FileReader reader = new FileReader(playerDataFile)) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                this.position = new GameMath.Vector2i(jsonObject.get("position").getAsJsonArray().get(0).getAsInt(),jsonObject.get("position").getAsJsonArray().get(1).getAsInt());
                this.direction = jsonObject.get("direction").getAsInt();
                this.dimension = jsonObject.get("dimension").getAsString();
                this.maxHearts = jsonObject.get("maxHearts").getAsInt();
                this.setHealth(jsonObject.get("health").getAsInt());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }else {

            try {
                playerDataFile.getParentFile().mkdirs();
                playerDataFile.createNewFile();
                try (FileWriter writer = new FileWriter(playerDataFile)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonObject object = new JsonObject();
                    object.add("position",this.position.toJson());
                    object.addProperty("direction",this.direction);
                    object.addProperty("dimension",this.dimension);
                    object.addProperty("maxHearts",this.maxHearts);
                    object.addProperty("health",this.health);
                    gson.toJson(object, writer);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public GameMath.Vector2i getPosition() {
        return position;
    }

    @Override
    public String getDimension() {
        return this.dimension;
    }

    @Override
    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public int getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
        updateHealth();
    }

    @Override
    public void damage(int damage) {
        this.health -= damage;
        updateHealth();
    }
    private void updateHealth(){
        PacketByteBuffer buffer = new PacketByteBuffer((byte) 0x07);
        buffer.putInt(this.health);
        buffer.sendTo(this);
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
        buffer.putInt(this.dimension.length());
        buffer.put(this.dimension.getBytes(StandardCharsets.UTF_8));
        return buffer.array();
    }

    @Override
    public InetSocketAddress getAddress() {
        return ip;
    }

    @Override
    public int getSkin() {
        return this.skin;
    }

    @Override
    public void setSkin(int skin) {
        this.skin = skin;
    }

    @Override
    public void save() {
        System.out.println("Saving PlayerData...");
        File playerDataFile = new File("./PlayerData/"+uuid.toString()+".json");
        try {
            if (!playerDataFile.exists()) {
                playerDataFile.getParentFile().mkdirs();
                playerDataFile.createNewFile();
            }
            try (FileWriter writer = new FileWriter(playerDataFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject object = new JsonObject();
                object.add("position",this.position.toJson());
                object.addProperty("direction",this.direction);
                object.addProperty("dimension",this.dimension);
                object.addProperty("maxHearts",this.maxHearts);
                object.addProperty("health",this.health);
                gson.toJson(object, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public String getType() {
        return "";
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
