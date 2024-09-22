package pogtan.game;

import pogtan.server.SendPacket;

public final class User {
    private final int id;
    private final String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void encode(SendPacket packet) {
        packet.encodeString(getName());
        packet.encodeByte(0);
        packet.encodeShort(0);
        packet.encodeInt(0);
        packet.encodeShort(0);
    }
}
