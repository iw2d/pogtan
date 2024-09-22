package pogtan.util;

import io.netty.buffer.ByteBuf;

public final class Crc32 {
    private static final int[] crcTable = genCrcTable();

    public static int[] genCrcTable() {
        final int[] crcTable = new int[256];
        for (int i = 0; i < crcTable.length; i++) {
            int crc = i << 24;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x80000000) != 0) {
                    crc = (crc << 1) ^ 0x4C11DB7;
                } else {
                    crc = (crc << 1);
                }
            }
            crcTable[i] = crc;
        }
        return crcTable;
    }

    public static int updateCrc(int init, byte[] data) {
        int crc = init;
        for (int i = 0; i < data.length; i++) {
            crc = crcTable[(data[i] & 0xFF) ^ (crc >>> 24)] ^ (crc << 8);
        }
        return crc;
    }

    public static int updateCrc(int init, ByteBuf buffer) {
        int crc = init;
        for (int i = 0; i < buffer.writerIndex(); i++) {
            crc = crcTable[(buffer.getByte(i) & 0xFF) ^ (crc >>> 24)] ^ (crc << 8);
        }
        return crc;
    }
}