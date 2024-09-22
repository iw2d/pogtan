package pogtan.stage;

import pogtan.game.Session;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;

import java.util.List;

public final class PrepareStage extends SessionStage {
    public PrepareStage(Client client, List<User> users, Session session) {
        super(client, users, session);
    }

    @Override
    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case READY_STATE_REQUEST -> {
                // CPrepareStage::SendReadyStateRequest
                final int size = packet.decodeByte();
                for (int i = 0; i < size; i++) {
                    final int index = packet.decodeByte();
                    final byte[] state = packet.decodeBuffer(3);
                    session.getSlots().get(index).setState(state);
                    session.broadcastPacket(setSlotState(index, state));
                }
            }
            case SET_SLOT_STATE_REQUEST -> {
                // CPrepareStage::SendSetSlotStateRequest
                final int index = packet.decodeByte();
                final byte[] state = packet.decodeBuffer(3);
                session.getSlots().get(index).setState(state);
                session.broadcastPacket(setSlotState(index, state));
            }
            default -> {
                super.handlePacket(header, packet);
            }
        }
    }

    public static SendPacket setSlotState(int index, byte[] state) {
        // CPrepareStage::OnSetSlotState
        final SendPacket packet = SendPacket.of(SendHeader.SET_SLOT_STATE);
        packet.encodeByte(index);
        packet.encodeBuffer(state);
        return packet;
    }
}
