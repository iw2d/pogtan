package pogtan.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.security.SecureRandom;

public abstract class SocketAcceptor {
    protected static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    protected static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    protected static final SecureRandom random = new SecureRandom();
}
