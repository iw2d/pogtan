package pogtan.game;

import pogtan.server.SendPacket;

public final class User {
    private final int id;
    private final String name;
    private int level = 1;
    private int lastSelectedBomber;

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLastSelectedBomber() {
        return lastSelectedBomber;
    }

    public void setLastSelectedBomber(int lastSelectedBomber) {
        this.lastSelectedBomber = lastSelectedBomber;
    }

    public void encode(SendPacket packet) {
        packet.encodeString(getName());
        packet.encodeByte(0);
        packet.encodeShort(getLevel());
        packet.encodeInt(0);
        packet.encodeShort(0);
    }
}
