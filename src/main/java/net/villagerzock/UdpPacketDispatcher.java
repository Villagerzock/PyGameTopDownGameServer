package net.villagerzock;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpPacketDispatcher {
    private final Map<Integer, UdpPacketHandler> handlers = new ConcurrentHashMap<>();
    private UdpPacketHandler defaultHandler = (id, payload, remote, socket) -> {
        // Ignorieren oder loggen
        System.out.println("Unbekannter Packet-Typ: " + id + " von " + remote);
    };

    public void register(int packetId, UdpPacketHandler handler) {
        if (packetId < 0 || packetId > 255) throw new IllegalArgumentException("packetId 0..255");
        if (handler == null) throw new IllegalArgumentException("handler == null");
        handlers.put(packetId, handler);
    }

    public void setDefaultHandler(UdpPacketHandler handler) {
        this.defaultHandler = handler != null ? handler : this.defaultHandler;
    }

    public void dispatch(byte[] data, int offset, int length, InetSocketAddress remote, DatagramSocket socket) throws IOException {
        if (length <= 0) return;
        byte packetId = data[offset];
        int payloadOffset = offset + 1;
        int payloadLength = Math.max(0, length - 1);
        PacketByteBuffer payload = new PacketByteBuffer(ByteBuffer.wrap(data, payloadOffset, payloadLength).slice());
        UdpPacketHandler h = handlers.get(packetId & 0xFF);
        if (h == null) h = defaultHandler;
        h.handle(packetId, payload, remote, socket);
    }
}
