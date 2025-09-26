import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        final int PORT = 6969;
        final int BUFFER_SIZE = 2048;
        Map<UUID,Player> players = new HashMap<>();
        UdpPacketDispatcher dispatcher = new UdpPacketDispatcher();
        dispatcher.register(0,(id,payload,remote,client)->{
            byte[] name_bytes = new byte[payload.getInt()];
            for (int i = 0; i < name_bytes.length; i++) {
                name_bytes[i] = payload.get();
            }
            UUID uuid = UUID.randomUUID();
            System.out.println("Gave Player UUID: " + uuid);
            Player player = new ServerPlayer(new String(name_bytes, StandardCharsets.UTF_8),uuid,client,remote);
            players.put(uuid,player);
            String uuid_string = uuid.toString();
            byte[] uuid_bytes = new byte[uuid_string.length() + 5];
            uuid_bytes[0] = 0x00;
            uuid_bytes[1] = (byte) ((uuid_string.length() >> 24) & 0xFF);
            uuid_bytes[2] = (byte) ((uuid_string.length() >> 16) & 0xFF);
            uuid_bytes[3] = (byte) ((uuid_string.length() >> 8) & 0xFF);
            uuid_bytes[4] = (byte) (uuid_string.length() & 0xFF);
            for (int i = 5; i < uuid_bytes.length; i++) {
                uuid_bytes[i] = uuid_string.getBytes(StandardCharsets.UTF_8)[i - 5];
            }

            for (Player p : players.values()) {
                if (p != player) {
                    byte[] other_player_bytes = p.getPacketData((byte)0x01);
                    player.send(new DatagramPacket(other_player_bytes, other_player_bytes.length,remote));
                    byte[] new_players_bytes = player.getPacketData((byte)0x01);
                    p.send(new DatagramPacket(new_players_bytes, new_players_bytes.length,p.getAddress()));
                }
            }

            client.send(new DatagramPacket(uuid_bytes, uuid_bytes.length, remote));
            System.out.println("Received Player Join Packet from Client called: " + new String(name_bytes, StandardCharsets.UTF_8));
        });
        dispatcher.register(1,new UdpPacketHandler() {
            private Map<Player,Short> seq = new HashMap<>();
            @Override
            public void handle(byte packetId, ByteBuffer payload, InetSocketAddress remote, DatagramSocket socket) throws IOException {
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
                        byte[] other_player_bytes = p.getPacketData((byte)0x01);
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
            System.out.println(player.getName() + " Disconnected!");
            players.remove(uuid);
            ByteBuffer buffer = ByteBuffer.allocate(5 + uuid_bytes.length);
            buffer.put((byte) 0x02);
            buffer.putInt(uuid_length);
            buffer.put(uuid_bytes);
            for (Player p : players.values()){
                System.out.println("Sending Disconnect Packet to Player: " + p.getName());
                p.send(new DatagramPacket(buffer.array(), buffer.array().length,p.getAddress()));
            }
        });

        System.out.println("UDP Server lauscht auf Port " + PORT);

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
