package pogtan.stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.Server;
import pogtan.game.Channel;
import pogtan.game.User;
import pogtan.header.ReceiveHeader;
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
            default -> {
                super.handlePacket(header, packet);
            }
        }
    }
}
