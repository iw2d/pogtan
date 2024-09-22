package pogtan.stage;

import pogtan.game.Session;
import pogtan.game.Slot;
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
            case SET_SESSION_MAP_REQUEST -> {
                // CPrepareStage::SendMapSelRequest
                final int map = packet.decodeShort();
                session.setMap(map);
                session.broadcastPacket(setSessionMap(map));
            }
            case SET_SLOT_STATE_REQUEST -> {
                // CPrepareStage::SendSetSlotStateRequest
                final int index = packet.decodeByte();
                final byte[] state = packet.decodeBuffer(3);
                session.getSlots().get(index).setState(state);
                session.broadcastPacket(setSlotState(index, state));
            }
            case START_GAME_REQUEST -> {
                // CPrepareStage::DeferStartGame
                client.write(startGameResult(0));
                client.setStage(new GameStage(client, users, session));
                session.broadcastPacket(launchGameStage(users.get(0).getId(), session));
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

    public static SendPacket setSessionMap(int map) {
        // CPrepareStage::OnSetSessionMap
        final SendPacket packet = SendPacket.of(SendHeader.SET_SESSION_MAP);
        packet.encodeShort(map);
        return packet;
    }

    public static SendPacket startGameResult(int resultType) {
        // CPrepareStage::OnStartGameResult
        // 0 : success
        // 1 : cannot select map
        // 2 : not everyone is ready
        // 3 : not enough players
        // 4 : too many players
        // 5 : not enough teams
        // 6 : unbalanced teams
        // 7 : premium channel only
        final SendPacket packet = SendPacket.of(SendHeader.START_GAME_RESULT);
        packet.encodeByte(resultType);
        return packet;
    }

    public static SendPacket launchGameStage(int unk, Session session) {
        // CPrepareStage::OnLaunchGameStage
        final SendPacket packet = SendPacket.of(SendHeader.LAUNCH_GAME_STAGE);
        packet.encodeInt(1); // unsure, host id?
        packet.encodeShort(session.getMap());

        final List<Slot> slots = session.getUserSlots();
        packet.encodeByte(slots.size());
        for (int i = 0; i < slots.size(); i++) {
            final Slot slot = slots.get(i);
            packet.encodeByte(slot.getIndex());
            packet.encodeByte(slot.isChiefPlayer());
            packet.encodeByte(i);
            packet.encodeByte(slot.getBomber());
            packet.encodeByte(slot.getTeam());
        }
        return packet;
    }
}
