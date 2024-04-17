package plu.capstone.playerpiano.outputs.arduino.packets;

import java.nio.ByteBuffer;

public abstract class Packet {

    private ByteBuffer buffer;

    protected final void allocate(int size) {
        buffer = ByteBuffer.allocate(size);
    }

    protected abstract void writeBytes();

    protected final void write(char id) { write((byte)id); }

    protected final void write(int b) { write((byte)b); }
    protected final void write(byte b) {
        if(buffer == null) {
            throw new IllegalStateException("Packet has not been allocated yet! Call allocate() before writing bytes.");
        }
        buffer.put(b);
    }

    public final byte[] getBytes() {
        processPacket();
        return buffer.array();
    }

    protected void processPacket() {
        if(buffer == null) {
            writeBytes();
        }
    }

    public final int size() { return getBytes().length; }

}
