package pogtan.stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.Server;
import pogtan.game.Channel;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;

import java.util.ArrayList;
import java.util.List;

public final class MenuStage extends Stage {
    private static final Logger log = LogManager.getLogger(MenuStage.class);
    private final List<User> users = new ArrayList<>();

    public MenuStage(Client client) {
        super(client);
    }

    @Override
    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case LOGIN_REQUEST -> {
                // CMainSystem::Login
                users.clear();
                final int size = packet.decodeByte();
                for (int i = 0; i < size; i++) {
                    final String name = packet.decodeString();
                    final String pass = packet.decodeString();
                    users.add(new User(1234, name));
                }
                client.write(MenuStage.loginResult(0, users));
            }
            case CHANNELS_REQUEST -> {
                // CMenuStage::OnOkClicked
                if (users.isEmpty()) {
                    log.error("Received {} before login", header);
                    return;
                }
                client.setStage(new FrontStage(client, users));
                final List<Channel> channels = Server.getChannels();
                client.write(FrontStage.channelsInfo(channels));
                client.write(FrontStage.channelsState(channels));
            }
            case CONFIRM_ID_REQUEST -> {
                // CNewUserDlg::OnConfirmIdClicked
                final String name = packet.decodeString();
                client.write(MenuStage.confirmIdResult(0));
            }
            case NEW_USER_REQUEST -> {
                // CNewUserDlg::OnOkClicked
                packet.decodeString();
                packet.decodeString();
                packet.decodeString();
                packet.decodeString();
                packet.decodeByte(); // 0
                packet.decodeByte();
                packet.decodeString();
                packet.decodeString();
                packet.decodeString();
                packet.decodeByte();
                client.write(MenuStage.newUserResult(0));
            }
            case CRASH_INFO -> {
                // CException::DeferSendCrashInfo
                final String crashInfo = packet.decodeString();
                log.info(crashInfo);
            }
            default -> {
                super.handlePacket(header, packet);
            }
        }
    }

    public static SendPacket loginResult(int resultType, List<User> users) {
        // CMenuStage::OnLoginResult
        final SendPacket packet = SendPacket.of(SendHeader.LOGIN_RESULT);
        packet.encodeByte(resultType); // 0, 4 : success, 1 : unknown user or incorrect password, 2 : already logged in, 3 : error
        packet.encodeByte(users.size());
        for (User user : users) {
            packet.encodeInt(user.getId());
            // CUser::SetUser
            packet.encodeString(user.getName());
            packet.encodeByte(0);
            packet.encodeShort(0);
            packet.encodeInt(0);
            // ~CUser::SetUser
            packet.encodeInt(0xBEEF);
        }
        return packet;
    }

    public static SendPacket confirmIdResult(int resultType) {
        // CMenuStage::OnNewUserConfirmIdResult
        final SendPacket packet = SendPacket.of(SendHeader.CONFIRM_ID_RESULT);
        packet.encodeByte(resultType); // 0 : success, 1 : already used, 2 : error
        return packet;
    }

    public static SendPacket newUserResult(int resultType) {
        // CMenuStage::OnNewIdResult
        final SendPacket packet = SendPacket.of(SendHeader.NEW_USER_RESULT);
        packet.encodeByte(resultType); // 0 : success, 1 : already used, 2 : error
        return packet;
    }
}
