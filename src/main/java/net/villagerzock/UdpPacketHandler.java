package net.villagerzock;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface UdpPacketHandler {
    /**
     * @param packetId   0..255 aus dem ersten Byte
     * @param payload    ByteBuffer NUR der Nutzdaten ab Byte 1, limit = tatsächliche Länge
     * @param remote     Absender (Adresse+Port)
     * @param socket     Server-Socket, falls eine Antwort gesendet werden soll
     */
    void handle(byte packetId, PacketByteBuffer payload, InetSocketAddress remote, DatagramSocket socket) throws IOException;
}
