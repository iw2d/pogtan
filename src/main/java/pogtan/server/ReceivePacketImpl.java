package pogtan.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class ReceivePacketImpl implements ReceivePacket {
    private final ByteBuffer buffer;

    public ReceivePacketImpl(byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public byte decodeByte() {
        return buffer.get();
    }

    @Override
    public short decodeShort() {
        return buffer.getShort();
    }

    @Override
    public int decodeInt() {
        return buffer.getInt();
    }

    @Override
    public byte[] decodeBuffer(int length) {
        final byte[] value = new byte[length];
        buffer.get(value);
        return value;
    }

    @Override
    public String decodeString() {
        final short length = decodeShort();
        return new String(decodeBuffer(length), StandardCharsets.US_ASCII);
    }

    @Override
    public byte[] getData() {
        return buffer.array();
    }
}
