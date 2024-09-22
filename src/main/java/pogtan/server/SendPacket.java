package pogtan.server;

import pogtan.header.SendHeader;

public interface SendPacket {
    SendHeader getHeader();

    byte[] getData();

    void encodeByte(byte value);

    default void encodeByte(boolean value) {
        encodeByte(value ? 1 : 0);
    }

    default void encodeByte(int value) {
        encodeByte((byte) value);
    }

    void encodeShort(short value);

    default void encodeShort(int value) {
        encodeShort((short) value);
    }

    void encodeInt(int value);

    void encodeBuffer(byte[] buffer);

    void encodeString(String string);

    static SendPacket of(SendHeader header) {
        return new SendPacketImpl(header);
    }
}
