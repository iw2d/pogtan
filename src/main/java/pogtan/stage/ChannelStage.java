package pogtan.stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.Server;
import pogtan.game.Channel;
import pogtan.game.Session;
import pogtan.game.Slot;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;

import java.util.List;

public final class ChannelStage extends ChatStage {
    private static final Logger log = LogManager.getLogger(ChannelStage.class);
    private final Channel channel;

    public ChannelStage(Client client, List<User> users, Channel channel) {
        super(client, users);
        this.channel = channel;
    }

    @Override
    public void broadcastPacket(SendPacket packet) {
        channel.getUserStorage().broadcastPacket(packet);
    }

    @Override
    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case DISCONNECT_FROM_CHANNEL_REQUEST -> {
                // CChannelStage::OnChannelClicked
                channel.getUserStorage().remove(client);
                client.setStage(new FrontStage(client, users));
                final List<Channel> channels = Server.getChannels();
                client.write(FrontStage.channelsInfo(channels));
                client.write(FrontStage.channelsState(channels));
            }
            case USER_SIMPLE_INFO_REQUEST -> {
                // CUserMap::DeferPostUserInfoSimpleReq
                final int page = packet.decodeShort();
                client.write(userSimpleInfo(page, List.of()));
            }
            case MY_INFO_REQUEST -> {
                // CChannelStage::OnButtonClicked
                client.write(myInfoResult());
            }
            case MODIFY_USER_INFO_REQUEST -> {
                // CMyInfoDlg::OnOkClicked
                packet.decodeString(); // pass
                packet.decodeString();
                packet.decodeString();
                packet.decodeByte();
                packet.decodeByte();
                packet.decodeString();
                packet.decodeString();
                packet.decodeString();
                packet.decodeByte();
                client.write(modifyUserInfoResult(true));
            }
            case CREATE_SESSION -> {
                // CChannelStage::CreateSession
                final String name = packet.decodeString();
                final String pass = packet.decodeString();
                packet.decodeByte();
                packet.decodeByte();
                for (User user : users) {
                    user.setLastSelectedBomber(packet.decodeByte()); // LastSelBomber
                }
                final Session session = new Session(channel, 0, name, pass);
                session.getSlots().getLast().setUser(new User(999, "fake"));
                session.getSlots().getLast().setReady(true);
                if (!session.addClient(client, users)) {
                    log.error("Failed to add user to session");
                    return;
                }
                client.setStage(new PrepareStage(client, users, session));
                client.write(createSessionResult(0, session));
                for (Slot slot : session.getSlots()) {
                    if (slot.getUser() != null) {
                        client.write(SessionStage.setPlayer(slot.getIndex(), slot.getUser(), slot.getState(), false));
                    }
                }
            }
            case JOIN_SESSION -> {
                // CChannelStage::JoinSession
            }
            case AUTO_JOIN_SESSION -> {
                // CChannelStage::AutoJoinSession
            }
            default -> {
                super.handlePacket(header, packet);
            }
        }
    }

    public static SendPacket myInfoResult() {
        // CChannelStage::OnMyInfoResult
        final SendPacket packet = SendPacket.of(SendHeader.MY_INFO_RESULT);
        packet.encodeString("NAME"); // NAME
        packet.encodeString("EMAIL"); // EMAIL
        packet.encodeByte(0);
        packet.encodeByte(0); // GENDER (0 = male, 1 = female)
        packet.encodeString("ADDRESS"); // ADDRESS
        packet.encodeString("444 444"); // PHONE
        packet.encodeString("INTRODUCTION"); // INTRODUCTION
        packet.encodeByte(1); // PRIVACY (0 = private, 1 = public)
        return packet;
    }

    public static SendPacket modifyUserInfoResult(boolean success) {
        // CChannelStage::OnModifyUserInfoResult
        final SendPacket packet = SendPacket.of(SendHeader.MODIFY_USER_INFO_RESULT);
        packet.encodeByte(success);
        return packet;
    }

    public static SendPacket inviteUser(int mapId, String inviterName, String sessionName) {
        // CChannelStage::OnInviteUser
        final SendPacket packet = SendPacket.of(SendHeader.INVITE_USER);
        packet.encodeShort(mapId);
        packet.encodeInt(0); // inviter id? ignored
        packet.encodeString(inviterName);
        packet.encodeString(sessionName);
        return packet;
    }

    public static SendPacket createSessionResult(int resultType, Session session) {
        // CChannelStage::OnCreateSessionResult
        // 0 : success
        // 1 : session with the same name already exists
        // 2 : no more session slots
        // 3 : failed to create session
        // 4 : no permission to create session
        final SendPacket packet = SendPacket.of(SendHeader.CREATE_SESSION_RESULT);
        packet.encodeByte(resultType);
        if (session != null) {
            packet.encodeShort(session.getId());
            packet.encodeInt(0);
            packet.encodeByte(0);
        }
        return packet;
    }

    public static SendPacket joinSessionResult(int resultType) {
        // CChannelStage::OnJoinSessionResult
        // 0 : success
        // 1 : cannot join session
        // 2 : same as 1?
        final SendPacket packet = SendPacket.of(SendHeader.CREATE_SESSION_RESULT);
        packet.encodeByte(resultType);
        return packet;
    }
}
