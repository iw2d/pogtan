package pogtan.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.util.Crc32;

public final class UdpAcceptor extends SocketAcceptor {
    private static final Logger log = LogManager.getLogger(UdpAcceptor.class);

    public static void initialize(int port) throws InterruptedException {
        final Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioDatagramChannel.class);
        b.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                ch.pipeline().addLast(new PacketHandler());
            }
        });
        b.option(ChannelOption.SO_BROADCAST, true);

        final ChannelFuture channelFuture = b.bind(port);
        channelFuture.sync();
        log.info("UdpAcceptor listening on port {}", port);
    }

    public static final class PacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            final ByteBuf in = packet.content();
            final int readableBytes = in.readableBytes();
            if (readableBytes < 1) {
                return;
            }
            in.readByte(); // size
            in.readInt(); // CRC

            switch (in.readByte()) {
                case 0 -> {
                    final ByteBuf out = Unpooled.buffer(5 + 1 + 4); // header + data + crc
                    out.writeByte(5 + 1);
                    out.writeInt(0);
                    out.writeByte(2); // op
                    out.writeInt(Crc32.updateCrc(0, out));
                    ctx.writeAndFlush(new DatagramPacket(out, packet.sender()));
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught while handling UDP packet", cause);
        }
    }
}
