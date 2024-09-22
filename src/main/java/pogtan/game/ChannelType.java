package pogtan.game;

public enum ChannelType {
    FREE_CHANNEL(0),
    BEGINNER_CHANNEL(1),
    PREMIUM_CHANNEL(41),
    SPECIAL_CHANNEL(55);

    private final byte value;

    ChannelType(int value) {
        this.value = (byte) value;
    }

    public final byte getValue() {
        return value;
    }
}
