package pogtan.game;

import pogtan.server.SendPacket;

public final class Channel {
    private final int id;
    private final String name;
    private final ChannelType channelType;
    private final UserStorage userStorage;

    public Channel(int id, String name, ChannelType channelType) {
        this.id = id;
        this.name = name;
        this.channelType = channelType;
        this.userStorage = new UserStorage();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }

    public void encode(SendPacket outPacket) {
        outPacket.encodeString(name);
        outPacket.encodeInt(userStorage.size());
        outPacket.encodeInt(id);
        outPacket.encodeShort(0);
        outPacket.encodeByte(channelType.getValue());
    }
}
