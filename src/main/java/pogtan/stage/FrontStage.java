package pogtan.stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.Server;
import pogtan.ServerConfig;
import pogtan.game.Channel;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.server.Client;
import pogtan.server.ReceivePacket;
import pogtan.server.SendPacket;

import java.util.List;
import java.util.Optional;

public final class FrontStage extends Stage {
    private static final Logger log = LogManager.getLogger(FrontStage.class);
    private final List<User> users;

    public FrontStage(Client client, List<User> users) {
        super(client);
        this.users = users;
    }

    @Override
    public void handlePacket(ReceiveHeader header, ReceivePacket packet) {
        switch (header) {
            case CONNECT_TO_SERVER_REQUEST -> {
                // CFrontStage::OnChannelSelected
                final int channelId = packet.decodeInt();
                final Optional<Channel> channelResult = Server.getChannel(channelId);
                if (channelResult.isEmpty()) {
                    client.write(connectToServerResult(null));
                    return;
                }
                final Channel channel = channelResult.get();
                Server.submitMigration(users, channel);
                client.write(connectToServerResult(channel));
            }
            default -> {
                super.handlePacket(header, packet);
            }
        }
    }

    public static SendPacket connectToServerResult(Channel channel) {
        // CFrontStage::OnResConnectToSvr
        final SendPacket packet = SendPacket.of(SendHeader.CONNECT_TO_SERVER_RESULT);
        packet.encodeByte(channel != null); // 0 : cannot connect, 2 : cannot enter premium channel
        if (channel != null) {
            packet.encodeInt(channel.getId()); // channel id
            packet.encodeBuffer(ServerConfig.SERVER_HOST); // channel host
            packet.encodeShort(ServerConfig.SERVER_PORT); // channel port
        }
        return packet;
    }

    public static SendPacket channelsInfo(List<Channel> channels) {
        // CFrontStage::OnChannelsInfo
        final SendPacket packet = SendPacket.of(SendHeader.CHANNELS_INFO);
        packet.encodeInt(channels.size());
        for (Channel channel : channels) {
            channel.encode(packet);
        }

        // CMainSystem::SetAdvertisementInfo
        packet.encodeString("");
        packet.encodeShort(0);
        packet.encodeShort(0);
        packet.encodeShort(0);

        packet.encodeInt(0); // int * (string, short)
        packet.encodeInt(0); // int * (byte, string) -> CFxMessage::InputMsg
        packet.encodeShort(0); // *(TSingleton<CGameMapInfoList>::ms_pInstance + 4)
        return packet;
    }

    public static SendPacket channelsState(List<Channel> channels) {
        // CFrontStage::OnChannelsState
        final SendPacket packet = SendPacket.of(SendHeader.CHANNELS_STATE);
        packet.encodeInt(channels.size());
        for (Channel channel : channels) {
            packet.encodeInt(0);
        }
        return packet;
    }

    public static SendPacket guildNotFound() {
        // CFrontStage::OnGuildNotFound
        return SendPacket.of(SendHeader.GUILD_NOT_FOUND);
    }
}
