package net.villagerzock;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.villagerzock.entity.AnimatedEntity;
import net.villagerzock.entity.Entity;
import net.villagerzock.entity.EntityFactory;
import net.villagerzock.entity.SpinnerEntity;
import net.villagerzock.world.TileRegistry;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static Map<UUID, Entity> entities = new ConcurrentHashMap<>();
    public static Map<UUID,Player> players = new ConcurrentHashMap<>();
    public static Map<String, EntityFactory<?>> entityFactories = new HashMap<>();
    public static Player AuthenticatePlayer(String token, DatagramSocket socket, InetSocketAddress ip) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/me"))
                .header("Authorization", "Bearer " + token)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            JsonObject answer = new Gson().fromJson(response.body(), JsonObject.class);
            System.out.println(response.body());
            UUID uuid = UUID.fromString(answer.get("uuid").getAsString());
            String name = answer.get("playername").getAsString();
            int skin = answer.get("skin").getAsInt();
            return new ServerPlayer(name,uuid,socket, ip,skin);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
    public static void registerNewEntity(Entity entity){
        entities.put(entity.getUUID(),entity);
        System.out.println("New entity registered");
        for (Player p : players.values()){
            PacketByteBuffer buffer = new PacketByteBuffer((byte)0x06);
            buffer.putString(entity.getUUID().toString());
            buffer.putString(entity.getType());
            int animation = 0;
            int animationTick = 0;
            if (entity instanceof AnimatedEntity animatedEntity){
                animation = animatedEntity.getAnimation();
                animationTick = animatedEntity.getAnimationTick();
            }
            buffer.putInt(animationTick);
            buffer.putInt(animation);
            buffer.putInt(entity.getPosition().x);
            buffer.putInt(entity.getPosition().y);
            buffer.putInt(entity.getDirection());
            buffer.putString(entity.getDimension());
            buffer.sendTo(p);
        }
    }
    public static void main(String[] args) {

        entityFactories.put("spinner",SpinnerEntity::new);

        final int PORT = 443;
        final int BUFFER_SIZE = 2048;
        try {
            TileRegistry.loadTiles("tiles.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        UUID spinnerUUID = UUID.randomUUID();
        registerNewEntity(new SpinnerEntity(spinnerUUID));

        UdpPacketDispatcher dispatcher = new UdpPacketDispatcher();
        dispatcher.register(0,(id,payload,remote,client)->{
            byte[] token_bytes = new byte[payload.getInt()];
            for (int i = 0; i < token_bytes.length; i++) {
                token_bytes[i] = payload.get();
            }
            Player player = AuthenticatePlayer(new String(token_bytes, StandardCharsets.UTF_8),client,remote);
            UUID uuid = player.getUUID();
            System.out.println("Gave " + player.getName() + " UUID: " + uuid);
            players.put(uuid,player);
            ChatHandler.sendMessage("§e" + player.getName() + " joined the game.");
            String uuid_string = uuid.toString();
            ByteBuffer buffer = ByteBuffer.allocate(uuid_string.length() + player.getName().length() + 29 + player.getDimension().length());
            buffer.put((byte) 0x00);
            buffer.putInt(uuid_string.length());
            buffer.put(uuid_string.getBytes(StandardCharsets.UTF_8));
            buffer.putInt(player.getName().length());
            buffer.put(player.getName().getBytes(StandardCharsets.UTF_8));
            buffer.putInt(player.getPosition().x);
            buffer.putInt(player.getPosition().y);
            buffer.putInt(player.getDirection());
            buffer.putInt(player.getSkin());
            buffer.putInt(player.getDimension().length());
            buffer.put(player.getDimension().getBytes(StandardCharsets.UTF_8));

            client.send(new DatagramPacket(buffer.array(), buffer.array().length, remote));
            for (Entity entity : entities.values()){

                PacketByteBuffer b = new PacketByteBuffer((byte)0x06);
                b.putString(entity.getUUID().toString());
                b.putString(entity.getType());
                int animation = 0;
                int animationTick = 0;
                if (entity instanceof AnimatedEntity animatedEntity){
                    animation = animatedEntity.getAnimation();
                    animationTick = animatedEntity.getAnimationTick();
                }
                b.putInt(animationTick);
                b.putInt(animation);
                b.putInt(entity.getPosition().x);
                b.putInt(entity.getPosition().y);
                b.putInt(entity.getDirection());
                b.putString(entity.getDimension());
                b.sendTo(player);
            }
            for (Player p : players.values()) {
                if (p != player) {
                    ChatHandler.sendMessage("Sending Register Packet to " + p.getName());
                    ByteBuffer other_player_buffer = ByteBuffer.allocate(25 + p.getUUID().toString().length() + p.getName().length());
                    // Writing OPCode
                    other_player_buffer.put((byte) 0x01);
                    // Writing UUID
                    other_player_buffer.putInt(p.getUUID().toString().length());
                    other_player_buffer.put(p.getUUID().toString().getBytes(StandardCharsets.UTF_8));
                    // Writing Player Name
                    other_player_buffer.putInt(p.getName().length());
                    other_player_buffer.put(p.getName().getBytes(StandardCharsets.UTF_8));
                    // Writing Positional Data
                    other_player_buffer.putInt(p.getPosition().x);
                    other_player_buffer.putInt(p.getPosition().y);
                    // Writing Directional Data
                    other_player_buffer.putInt(p.getDirection());
                    // Writing Skin Data
                    other_player_buffer.putInt(p.getSkin());

                    player.send(new DatagramPacket(other_player_buffer.array(), other_player_buffer.array().length,remote));
                    ByteBuffer new_player_buffer = ByteBuffer.allocate(25 + player.getUUID().toString().length() + player.getName().length());
                    // Writing OPCode
                    new_player_buffer.put((byte) 0x01);
                    // Writing UUID
                    new_player_buffer.putInt(player.getUUID().toString().length());
                    new_player_buffer.put(player.getUUID().toString().getBytes(StandardCharsets.UTF_8));
                    // Writing Player Name
                    new_player_buffer.putInt(player.getName().length());
                    new_player_buffer.put(player.getName().getBytes(StandardCharsets.UTF_8));
                    // Writing Positional Data
                    new_player_buffer.putInt(player.getPosition().x);
                    new_player_buffer.putInt(player.getPosition().y);
                    // Writing Directional Data
                    new_player_buffer.putInt(player.getDirection());
                    // Writing Skin Data
                    new_player_buffer.putInt(player.getSkin());
                    p.send(new DatagramPacket(new_player_buffer.array(), new_player_buffer.array().length,p.getAddress()));
                }
            }
            System.out.println("Received net.villagerzock.Player Join Packet from Client called: " + player.getName());
        });
        dispatcher.register(1,new UdpPacketHandler() {
            @Override
            public void handle(byte packetId, PacketByteBuffer payload, InetSocketAddress remote, DatagramSocket socket) throws IOException {
                int uuid_length = payload.getInt();
                byte[] uuid_bytes = new byte[uuid_length];
                for (int i = 0; i < uuid_length; i++) {
                    uuid_bytes[i] = payload.get();
                }
                UUID uuid = UUID.fromString(new String(uuid_bytes, StandardCharsets.UTF_8));
                Player player = players.get(uuid);
                short playerSeq = payload.getShort();
                long tMs = Integer.toUnsignedLong(payload.getInt());

                int x = payload.getInt();
                int y = payload.getInt();
                int direction = payload.getInt();
                int animation = payload.getInt();
                int animationTick = payload.getInt();
                player.getPosition().x = x;
                player.getPosition().y = y;
                player.setDirection(direction);
                player.setAnimation(animation);
                player.setAnimationTick(animationTick);
                for (Player p : players.values()) {
                    if (p != player) {
                        byte[] other_player_bytes = p.getPacketData((byte)0x02);
                        player.send(new DatagramPacket(other_player_bytes, other_player_bytes.length,remote));
                    }
                }

            }
        });
        dispatcher.register(2,(packetId,payload,remote,client)->{
            int uuid_length = payload.getInt();
            byte[] uuid_bytes = new byte[uuid_length];
            for (int i = 0; i < uuid_length; i++) {
                uuid_bytes[i] = payload.get();
            }
            UUID uuid = UUID.fromString(new String(uuid_bytes, StandardCharsets.UTF_8));
            Player player = players.get(uuid);
            player.save();
            players.remove(uuid);
            ChatHandler.sendMessage("§e" + player.getName() + " left the game");
            ByteBuffer buffer = ByteBuffer.allocate(5 + uuid_bytes.length);
            buffer.put((byte) 0x03);
            buffer.putInt(uuid_length);
            buffer.put(uuid_bytes);
            for (Player p : players.values()){
                System.out.println("Sending Disconnect Packet to net.villagerzock.Player: " + p.getName());
                p.send(new DatagramPacket(buffer.array(), buffer.array().length,p.getAddress()));
            }
        });
        dispatcher.register(3,(packetId,payload,remote,client)->{
            int msg_length = payload.getInt();
            byte[] msg_bytes = new byte[msg_length];
            for (int i = 0; i < msg_length; i++) {
                msg_bytes[i] = payload.get();
            }
            String msg = new String(msg_bytes, StandardCharsets.UTF_8);
            ChatHandler.sendMessage(msg);
        });
        dispatcher.register(4, (packetId,payload,remote,client)->{
            int uuid_length = payload.getInt();
            byte[] uuid_bytes = new byte[uuid_length];
            for (int i = 0; i < uuid_length; i++) {
                uuid_bytes[i] = payload.get();
            }
            UUID uuid = UUID.fromString(new String(uuid_bytes, StandardCharsets.UTF_8));
            Player player = players.get(uuid);
            int dimension_length = payload.getInt();
            byte[] dimension_bytes = new byte[dimension_length];
            for (int i = 0; i < dimension_length; i++) {
                dimension_bytes[i] = payload.get();
            }
            String dim = new String(dimension_bytes, StandardCharsets.UTF_8);
            player.setDimension(dim);
        });
        dispatcher.register(5,(packetId,payload,remote,client)->{
            String EntityType =  payload.getString();
            Entity entity = entityFactories.get(EntityType).construct(UUID.randomUUID(), new GameMath.Vector2i(payload.getInt(), payload.getInt()),payload.getString());
            registerNewEntity(entity);
        });

        /*dispatcher.register(4,(packetId,payload,remote,client)->{
            String motd = "This is a Pygame Server";

            ByteBuffer ping_response = ByteBuffer.allocate(17 + motd.length());
            ping_response.put((byte) 0x05);
            ping_response.putInt(payload.getInt());
            ping_response.putInt(players.size());
            ping_response.putInt(20);
            ping_response.putInt(motd.length());
            ping_response.put(motd.getBytes(StandardCharsets.UTF_8));
            client.send(new DatagramPacket(ping_response.array(), ping_response.array().length,remote));
        });*/
        System.out.println("UDP Server lauscht auf Port " + PORT);

        Timer entityTicker = new Timer();
        entityTicker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Iterator<Entity> entityIterator = Main.entities.values().iterator();
                while (entityIterator.hasNext()) {
                    Entity entity = entityIterator.next();
                    entity.tick();
                }
            }
        },0,25);
        entityTicker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Iterator<Entity> entityIterator = Main.entities.values().iterator();
                while (entityIterator.hasNext()) {
                    Entity entity = entityIterator.next();
                    byte[] bytes = entity.getPacketData((byte) 0x05);
                    for (Player p : Main.players.values()) {
                        p.send(new DatagramPacket(bytes,bytes.length,p.getAddress()));
                    }
                }
            }
        },0,50);

        try (DatagramSocket server = new DatagramSocket(PORT)) {
            byte[] buf = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                server.receive(packet);

                int length = packet.getLength();
                if (length == 0) continue;

                InetSocketAddress remote = new InetSocketAddress(packet.getAddress(), packet.getPort());

                try {
                    dispatcher.dispatch(packet.getData(), packet.getOffset(), length, remote, server);
                } catch (IOException e) {
                    System.err.println("Fehler im Handler: " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Konnte Socket nicht öffnen: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
