package pogtan.server;

import pogtan.header.SendHeader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class SendPacketImpl implements SendPacket {
    private final SendHeader header;
    private ByteBuffer buffer;

    public SendPacketImpl(SendHeader header) {
        this.header = header;
        this.buffer = newBuffer(16);
        this.buffer.put(header.getValue());
    }

    public void ensureSize(int size) {
        if (buffer.capacity() - buffer.position() >= size) {
            return;
        }
        final ByteBuffer newBuffer = newBuffer(Math.max(buffer.capacity() * 2, buffer.capacity() + size));
        newBuffer.put(buffer.slice(0, buffer.position()));
        buffer = newBuffer;
    }

    @Override
    public SendHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getData() {
        final byte[] data = new byte[buffer.position()];
        buffer.get(0, data);
        return data;
    }

    @Override
    public void encodeByte(byte value) {
        ensureSize(1);
        buffer.put(value);
    }

    @Override
    public void encodeShort(short value) {
        ensureSize(2);
        buffer.putShort(value);
    }

    @Override
    public void encodeInt(int value) {
        ensureSize(4);
        buffer.putInt(value);
    }

    @Override
    public void encodeBuffer(byte[] value) {
        ensureSize(value.length);
        buffer.put(value);
    }

    @Override
    public void encodeString(String value) {
        ensureSize(value.length() + 2);
        buffer.putShort((short) value.length());
        buffer.put(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static ByteBuffer newBuffer(int capacity) {
        final ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer;
    }
}
