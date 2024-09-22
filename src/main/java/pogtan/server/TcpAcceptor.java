package pogtan.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pogtan.ServerConfig;
import pogtan.header.HeaderConverter;
import pogtan.header.ReceiveHeader;
import pogtan.header.SendHeader;
import pogtan.stage.MenuStage;
import pogtan.util.Util;

import java.util.List;

public final class TcpAcceptor extends SocketAcceptor {
    private static final Logger log = LogManager.getLogger(TcpAcceptor.class);

    public static void initialize(int port) throws InterruptedException {
        final ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new PacketDecoder(), new PacketHandler(), new PacketEncoder());
                final Client c = new Client(ch, random.nextInt(0, HeaderConverter.HEADER_KEY_MAX));
                ch.attr(Client.ATTRIBUTE_KEY).set(c);
                c.setStage(new MenuStage(c));
                c.write(connect(c.getHeaderKey()));
            }
        });
        b.childOption(ChannelOption.TCP_NODELAY, true);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);

        final ChannelFuture channelFuture = b.bind(port);
        channelFuture.sync();
        log.info("TcpAcceptor listening on port {}", port);
    }

    private static SendPacket connect(int headerKey) {
        // CMainSystem::OnConnect
        final SendPacket packet = SendPacket.of(SendHeader.CONNECT);
        packet.encodeShort(ServerConfig.GAME_VERSION);
        packet.encodeShort(ServerConfig.GAME_VERSION);
        packet.encodeInt(headerKey);
        packet.encodeString("");
        return packet;
    }

    public static final class PacketDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            final Client c = ctx.channel().attr(Client.ATTRIBUTE_KEY).get();
            if (c.getStoredLength() < 0) {
                if (in.readableBytes() < 3) {
                    return;
                }
                in.readByte();
                final int length = in.readShort();
                c.setStoredLength(length);
            } else if (in.readableBytes() >= c.getStoredLength()) {
                final byte[] data = new byte[c.getStoredLength()];
                in.readBytes(data);
                c.setStoredLength(-1);
                out.add(ReceivePacket.of(data));
            }
        }
    }

    public static final class PacketHandler extends SimpleChannelInboundHandler<ReceivePacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ReceivePacket packet) {
            final Client c = ctx.channel().attr(Client.ATTRIBUTE_KEY).get();
            final byte op = packet.decodeByte();
            final ReceiveHeader header = HeaderConverter.decodeHeader(op, c.getHeaderKey());
            if (header == null) {
                log.error("Unknown header op : {} for header key : {}", op, c.getHeaderKey());
                return;
            }
            final byte[] data = packet.getData();
            log.debug("[In]  | {} ({}/{}) | {}", header, header.getValue(), Util.formatHex(header.getValue()), Util.readableByteArray(data));
            c.getStage().handlePacket(header, packet);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught while handling TCP packet", cause);
        }
    }

    public static final class PacketEncoder extends MessageToByteEncoder<SendPacket> {
        @Override
        protected void encode(ChannelHandlerContext ctx, SendPacket packet, ByteBuf out) {
            final Client c = ctx.channel().attr(Client.ATTRIBUTE_KEY).get();
            final SendHeader header = packet.getHeader();
            final byte[] data = packet.getData();
            log.debug("[Out] | {} ({}/{}) | {}", header, header.getValue(), Util.formatHex(header.getValue()), Util.readableByteArray(data));

            out.writeByte(0);
            out.writeShort(data.length);
            out.writeBytes(data);
        }
    }
}
