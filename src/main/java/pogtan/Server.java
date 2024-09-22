package pogtan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.game.Channel;
import pogtan.game.ChannelType;
import pogtan.game.User;
import pogtan.server.TcpAcceptor;
import pogtan.server.UdpAcceptor;
import pogtan.util.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Server {
    private static final Logger log = LogManager.getLogger(Server.class);
    private static final List<Channel> channels = List.of(
            new Channel(1, "TEST", ChannelType.FREE_CHANNEL)
    );
    private static final ConcurrentHashMap<Integer, Tuple<List<User>, Channel>> migrations = new ConcurrentHashMap<>();

    public static List<Channel> getChannels() {
        return channels;
    }

    public static Optional<Channel> getChannel(int index) {
        if (index < 0 || index >= channels.size()) {
            return Optional.empty();
        }
        return Optional.of(channels.get(index));
    }

    public static void submitMigration(List<User> users, Channel channel) {
        final int migrationKey = users.get(0).getId();
        migrations.put(migrationKey, Tuple.of(users, channel));
    }

    public static Optional<Tuple<List<User>, Channel>> completeMigration(int migrationKey) {
        return Optional.ofNullable(migrations.remove(migrationKey));
    }

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting server");

        TcpAcceptor.initialize(ServerConfig.SERVER_PORT);

        UdpAcceptor.initialize(ServerConfig.SERVER_PORT + 1);
    }
}
