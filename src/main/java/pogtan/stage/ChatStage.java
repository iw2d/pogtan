package pogtan.stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;

import java.util.List;

public abstract class ChatStage extends Stage {
    private static final Logger log = LogManager.getLogger(ChatStage.class);
    protected final List<User> users;

    protected ChatStage(Client client, List<User> users) {
        super(client);
        this.users = users;
    }

    public abstract void broadcastPacket(SendPacket packet);

    @Override
    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case CHAT_MESSAGE -> {
                // CChatStage::HandleChatMsg
                final String message = packet.decodeString();
                final String formattedMessage = String.format("%s : %s",
                        String.join(" & ", users.stream().map(User::getName).toList()),
                        message
                );
                broadcastPacket(chatMessage(formattedMessage));
            }
            case COMMAND_MESSAGE -> {
                // CChatStage::HandleSpecialCmdMsg
            }
            case WHISPER_MESSAGE -> {
                // CChatStage::HandleChatMsg
            }

            default -> {
                super.handlePacket(header, packet);
            }
        }
    }

    public static SendPacket userDetailInfo(User user) {
        // CChatStage::OnUserDetailInfo
        final SendPacket packet = SendPacket.of(SendHeader.USER_DETAIL_INFO);
        packet.encodeByte(user != null);
        if (user != null) {
            packet.encodeString("");
            packet.encodeString("");
            packet.encodeString("");
            packet.encodeByte(false);
            packet.encodeInt(0);
            packet.encodeInt(0);
            packet.encodeInt(0);

            packet.encodeInt(0);
            packet.encodeInt(0);

            packet.encodeShort(0);
            packet.encodeInt(0);
            packet.encodeShort(0);

            packet.encodeByte(false); // boolean -> (string, string, string)
        }
        return packet;
    }

    public static SendPacket userLocation(User user) {
        // CChatStage::OnUserLocation
        final SendPacket packet = SendPacket.of(SendHeader.USER_DETAIL_INFO);
        packet.encodeByte(user != null);
        if (user != null) {
            packet.encodeString("");
            packet.encodeInt(0xF0000000);
            packet.encodeShort(0xFFFF);
        }
        return packet;
    }

    public static SendPacket chatMessage(String message) {
        // CChatStage::OnChatMsg
        final SendPacket packet = SendPacket.of(SendHeader.CHAT_MESSAGE);
        packet.encodeString(message);
        return packet;
    }

    public static SendPacket whisperMessage(String message) {
        // CChatStage::OnWhisper
        final SendPacket packet = SendPacket.of(SendHeader.WHISPER_MESSAGE);
        packet.encodeString(message);
        return packet;
    }

    public static SendPacket whisperMessageRet(String message) {
        // CChatStage::OnWhisperRet
        final SendPacket packet = SendPacket.of(SendHeader.WHISPER_MESSAGE_RET);
        packet.encodeByte(message == null);
        if (message != null) {
            packet.encodeString(message);
        }
        return packet;
    }
}
