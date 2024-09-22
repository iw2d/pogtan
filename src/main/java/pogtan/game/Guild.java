package pogtan.game;

import pogtan.server.SendPacket;

public final class Guild {
    public void encode(SendPacket packet) {
        packet.encodeInt(1); // guild.getGuildId()
        packet.encodeString("guild"); // guild.getGuildName()
        packet.encodeShort(0);
    }
}
