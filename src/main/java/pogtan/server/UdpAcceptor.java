package pogtan.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.Server;
import pogtan.stage.GameStage;
import pogtan.util.Crc32;
import pogtan.util.Tuple;
import pogtan.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

            final byte header = in.readByte();
            log.debug("[UDP] {}", header);
            switch (header) {
                case 0 -> {
                    // Encode Packet
                    final ByteBuf out = Unpooled.buffer(5 + 1 + 4); // header + data + crc
                    out.writeByte(5 + 1);
                    out.writeInt(0);
                    out.writeByte(2); // op
                    out.writeInt(Crc32.updateCrc(0, out));
                    ctx.writeAndFlush(new DatagramPacket(out, packet.sender()));
                }
                case 1 -> {
                    // Decode Packet
                    final int userId = in.readInt();
                    in.readShort();
                    in.readInt();
                    final List<Tuple<Integer, Integer>> addresses = new ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        final int host = in.readInt(); // host
                        final int port = in.readShort(); // port
                        addresses.add(Tuple.of(host, port));
                    }

                    // Resolve client
                    final Optional<Client> clientResult = Server.getClient(userId);
                    if (clientResult.isEmpty()) {
                        log.error("Could not resolve client with ID : {}", userId);
                        return;
                    }
                    final Client client = clientResult.get();
                    if (!(client.getStage() instanceof GameStage gameStage)) {
                        log.error("Client received UDP(1) while in incorrect stage : {}", client.getStage().getClass().getSimpleName());
                        return;
                    }

                    // Encode packet
                    final ByteBuf out = Unpooled.buffer(5 + 9 + 4);
                    out.writeByte(5 + 9);
                    out.writeInt(0);
                    out.writeByte(3); // op
                    out.writeByte(0); // index?
                    out.writeByte(0); // host slot?
                    out.writeByte(0); // idk
                    out.writeByte(0);
                    out.writeShort(0);
                    out.writeShort(0);
                    out.writeInt(Crc32.updateCrc(0, out));
                    ctx.writeAndFlush(new DatagramPacket(out, packet.sender()));

                    // Update stage
                    client.write(GameStage.startGame(0, gameStage.getSession(), addresses));
                }
                default -> {
                    log.error("Unhandled UDP header {}/{}", header, Util.formatHex(header));
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught while handling UDP packet", cause);
        }
    }
}
