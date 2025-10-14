package net.villagerzock;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ChatHandler {
    public static void sendMessage(String message,Player player){
        ByteBuffer buffer = ByteBuffer.allocate(256);
        buffer.put((byte) 0x04);
        buffer.putInt(message.length());
        buffer.put(message.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(buffer.array(), StandardCharsets.UTF_8));
        player.send(new DatagramPacket(buffer.array(),buffer.array().length, player.getAddress()));
    }
    public static void sendMessage(String message){
        System.out.println("[CHAT] " + message);
        for (Player p : Main.players.values()){
            sendMessage(message,p);
        }
    }
}
