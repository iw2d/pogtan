package pogtan.stage;

import pogtan.game.Session;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;

import java.util.List;

public abstract class SessionStage extends ChatStage {
    protected final Session session;

    protected SessionStage(Client client, List<User> users, Session session) {
        super(client, users);
        this.session = session;
    }

    @Override
    public void broadcastPacket(SendPacket packet) {
        session.broadcastPacket(packet);
    }

    @Override
    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case LEAVE_SESSION -> {
                // CMainSystem::SendReturnChannelPacket
                client.setStage(new ChannelStage(client, users, session.getChannel()));
            }
            default -> {
                super.handlePacket(header, packet);
            }
        }
    }

    public static SendPacket setPlayer(int slot, User user, byte[] state, boolean enter) {
        // CSessionStage::OnSetPlayer
        final SendPacket packet = SendPacket.of(SendHeader.SET_PLAYER);
        packet.encodeByte(slot);
        packet.encodeInt(user.getId());
        packet.encodeBuffer(state);
        packet.encodeByte(enter);
        // CUser::SetUser
        packet.encodeString(user.getName());
        packet.encodeByte(0);
        packet.encodeShort(0);
        packet.encodeInt(0);
        // ~CUser::SetUser
        return packet;
    }
}
