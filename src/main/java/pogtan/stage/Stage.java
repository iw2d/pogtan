package pogtan.stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.Server;
import pogtan.game.Channel;
import pogtan.game.Guild;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;
import pogtan.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Stage {
    private static final Logger log = LogManager.getLogger(Stage.class);
    protected final Client client;

    protected Stage(Client client) {
        this.client = client;
    }

    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case CONNECT_TO_CHANNEL_REQUEST -> {
                // CFrontStage::OnResConnectToSvr
                final int size = packet.decodeByte();
                final List<User> userCheck = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    final String name = packet.decodeString();
                    final int id = packet.decodeInt();
                    packet.decodeInt();
                    userCheck.add(new User(id, name));
                }
                final int migrationKey = userCheck.get(0).getId();
                final Optional<Tuple<List<User>, Channel>> migrationResult = Server.completeMigration(migrationKey);
                if (migrationResult.isEmpty()) {
                    log.error("Could not complete migration with key : {}", migrationKey);
                    return;
                }
                final List<User> users = migrationResult.get().getLeft();
                final Channel channel = migrationResult.get().getRight();
                client.setStage(new ChannelStage(client, users, channel));
                channel.getUserStorage().add(client, users);
                client.write(enterChannel());
            }
            case ESTIMATE_RTTP_REQUEST -> {
                // CMainSystem::SendEstimateRTTPacket
            }
            default -> {
                log.error("Unhandled packet : {}", header);
            }
        }
    }

    public static SendPacket enterChannel() {
        // CStage::OnEnterChannel
        return SendPacket.of(SendHeader.ENTER_CHANNEL);
    }

    public static SendPacket serverReservedMessage() {
        // CStage::OnSvrReservedMsg
        return SendPacket.of(SendHeader.SERVER_RESERVED_MESSAGE);
    }

    public static SendPacket estimateRttpResult(int delta, int timestamp) {
        // CStage::OnSvrTimeNotify
        final SendPacket packet = SendPacket.of(SendHeader.ESTIMATE_RTTP_RESULT);
        packet.encodeInt(delta);
        packet.encodeInt(timestamp);
        return packet;
    }

    public static SendPacket userSimpleInfo(int page, List<User> users) {
        final SendPacket packet = SendPacket.of(SendHeader.USER_SIMPLE_INFO);
        // CUserMap::OnUserSimpleInfo
        packet.encodeShort(page);
        packet.encodeByte(users.size());
        for (User user : users) {
            user.encode(packet);
        }
        return packet;
    }

    public static SendPacket guildInfo(Guild guild) {
        final SendPacket packet = SendPacket.of(SendHeader.GUILD_INFO);
        guild.encode(packet); // CGuild::Decode
        return packet;
    }

    public static SendPacket adminNotifyMessage(String message) {
        // CStage::OnAdminNotifyMsg
        final SendPacket packet = SendPacket.of(SendHeader.ADMIN_NOTIFY_MESSAGE);
        packet.encodeString(message);
        return packet;
    }

    public static SendPacket guildMemberSimpleInfo(List<User> users) {
        final SendPacket packet = SendPacket.of(SendHeader.GUILD_MEMBER_SIMPLE_INFO);
        // CUserMap::OnGuildMemberSimpleInfo
        packet.encodeShort(0);
        packet.encodeByte(users.size());
        for (User user : users) {
            user.encode(packet);
        }
        return packet;
    }

    public static SendPacket guildMark(byte[] buffer) {
        // CStage::OnGuildMark
        final SendPacket packet = SendPacket.of(SendHeader.GUILD_MARK);
        packet.encodeInt(0);
        packet.encodeShort(0);
        packet.encodeInt(buffer.length);
        packet.encodeBuffer(buffer);
        return packet;
    }
}
