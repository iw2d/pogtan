package pogtan.server;

public interface ReceivePacket {
    byte decodeByte();

    short decodeShort();

    int decodeInt();

    byte[] decodeBuffer(int length);

    String decodeString();

    byte[] getData();

    static ReceivePacket of(byte[] data) {
        return new ReceivePacketImpl(data);
    }
}
