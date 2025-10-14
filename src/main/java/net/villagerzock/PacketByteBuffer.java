package net.villagerzock;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class PacketByteBuffer {
    private ByteBuffer byteBuffer;
    int allocated = 0;
    public PacketByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
    public PacketByteBuffer(byte opCode) {
        byteBuffer = ByteBuffer.allocate(512);
        allocated += 1;
        byteBuffer.put(opCode);
    }
    public PacketByteBuffer put(byte b) {
        byteBuffer.put(b);
        allocated += 1;
        return this;
    }
    public PacketByteBuffer put(byte[] b) {
        byteBuffer.put(b);
        allocated += b.length;
        return this;
    }
    public PacketByteBuffer putInt(int b) {
        byteBuffer.putInt(b);
        allocated += 4;
        return this;
    }
    public PacketByteBuffer putFloat(float b) {
        byteBuffer.putFloat(b);
        allocated += 4;
        return this;
    }
    public PacketByteBuffer putDouble(double b) {
        byteBuffer.putDouble(b);
        allocated += 8;
        return this;
    }
    public PacketByteBuffer putLong(long b) {
        byteBuffer.putLong(b);
        allocated += 8;
        return this;
    }
    public PacketByteBuffer putShort(short b) {
        byteBuffer.putShort(b);
        allocated += 1;
        return this;
    }
    public PacketByteBuffer putString(String b) {
        byteBuffer.putInt(b.length());
        allocated += 4;
        byteBuffer.put(b.getBytes());
        allocated += b.length();
        return this;
    }
    public PacketByteBuffer putBoolean(boolean b) {
        byteBuffer.put((byte) (b ? 1 : 0));
        return this;
    }
    public byte get() {
        return byteBuffer.get();
    }

    public int getInt() {
        return byteBuffer.getInt();
    }

    public float getFloat() {
        return byteBuffer.getFloat();
    }

    public double getDouble() {
        return byteBuffer.getDouble();
    }

    public long getLong() {
        return byteBuffer.getLong();
    }

    public short getShort() {
        return byteBuffer.getShort();
    }

    public String getString() {
        int length = byteBuffer.getInt(); // Erst die Länge lesen
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return new String(bytes);
    }
    public boolean getBoolean() {
        return byteBuffer.get() == 1;
    }
    public byte[] array() {
        byte[] bytes = new byte[allocated];
        for (int i = 0; i < allocated; i++) {
            bytes[i] = byteBuffer.array()[i];
        }
        return bytes;
    }

    public void sendTo(Player p) {
        p.send(new DatagramPacket(array(),array().length,p.getAddress()));
    }
}
