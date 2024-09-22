package pogtan.server;

import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.stage.Stage;

import java.net.InetSocketAddress;

public final class Client {
    public static final AttributeKey<Client> ATTRIBUTE_KEY = AttributeKey.valueOf("C");
    private static final Logger log = LogManager.getLogger(Client.class);
    private final SocketChannel channel;
    private final int headerKey;
    private int storedLength = -1;
    private Stage stage;

    public Client(SocketChannel channel, int headerKey) {
        this.channel = channel;
        this.headerKey = headerKey;
    }

    public InetSocketAddress getAddress() {
        return channel.remoteAddress();
    }

    public int getHeaderKey() {
        return headerKey;
    }

    public int getStoredLength() {
        return storedLength;
    }

    public void setStoredLength(int storedLength) {
        this.storedLength = storedLength;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        log.info("Set stage : {}", stage.getClass().getSimpleName());
        this.stage = stage;
    }

    public void write(SendPacket packet) {
        channel.writeAndFlush(packet);
    }

    public void close() {
        channel.close();
    }
}
