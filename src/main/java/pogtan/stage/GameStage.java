package pogtan.stage;

import pogtan.game.Session;
import pogtan.game.Slot;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;
import pogtan.util.Tuple;

import java.util.List;

public final class GameStage extends SessionStage {
    public GameStage(Client client, List<User> users, Session session) {
        super(client, users, session);
    }

    @Override
    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case BOMB_IGNITE -> {
                // CGameMsgCache::PutIgniteBombMsg
                final int unk1 = packet.decodeByte();
                final int unk2 = packet.decodeByte();
                final int unk3 = packet.decodeByte();
                final int unk4 = packet.decodeByte();
                session.broadcastPacket(bombIgnite(unk1, unk2, unk3, unk4));
            }
            case GAME_STAGE_CHECK_IN -> {
                // CGOBomber::Update
                final int index = packet.decodeByte();
            }
            default -> {
                super.handlePacket(header, packet);
            }
        }
    }

    public static SendPacket startGame(int index, Session session, List<Tuple<Integer, Integer>> addresses) {
        // CGameStage::OnStartGame
        final SendPacket packet = SendPacket.of(SendHeader.START_GAME);
        packet.encodeInt((int) System.currentTimeMillis());
        packet.encodeInt(index);

        for (Slot slot : session.getSlots()) {
            final User user = slot.getUser();
            if (user == null) {
                packet.encodeByte(false);
                continue;
            }
            packet.encodeByte(true);
            for (int j = 0; j < 3; j++) {
                // Addresses from UDP(3)
                if (j < addresses.size()) {
                    packet.encodeInt(addresses.get(j).getLeft());
                    packet.encodeShort(addresses.get(j).getRight());
                } else {
                    packet.encodeInt(0);
                    packet.encodeShort(0);
                }
            }
        }
        return packet;
    }

    public static SendPacket gameStageCheckIn(int index) {
        // CGameStage::OnGameStageCheckIn
        final SendPacket packet = SendPacket.of(SendHeader.GAME_STAGE_CHECK_IN);
        packet.encodeByte(index);
        return packet;
    }

    public static SendPacket bombIgnite(int unk1, int unk2, int unk3, int unk4) {
        // CGameStage::OnBombIgnite
        final SendPacket packet = SendPacket.of(SendHeader.BOMB_IGNITE);
        packet.encodeByte(unk1);
        packet.encodeByte(unk2);
        packet.encodeByte(unk3);
        packet.encodeByte(unk4);
        packet.encodeInt(1234); // bomb ID?
        return packet;
    }
}
