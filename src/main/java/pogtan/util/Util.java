package pogtan.util;

import java.util.HexFormat;

public final class Util {
    private static final HexFormat hexFormat = HexFormat.ofDelimiter(" ").withUpperCase();

    public static String readableByteArray(byte[] array) {
        return hexFormat.formatHex(array);
    }

    public static String formatHex(byte value) {
        return String.format("0x%02X", value);
    }

    public static String formatHex(int value) {
        return String.format("0x%08X", value);
    }
}
