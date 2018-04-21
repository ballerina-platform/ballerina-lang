package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.internal.websocket.DefaultWebSocketSession;

import java.nio.ByteBuffer;
import javax.websocket.Session;

/**
 * Default implementation of {@link WebSocketConnection}.
 */
public class DefaultWebSocketConnection implements WebSocketConnection {

    private final ChannelHandlerContext ctx;
    private final DefaultWebSocketSession session;

    public DefaultWebSocketConnection(ChannelHandlerContext ctx, DefaultWebSocketSession session) {
        this.ctx = ctx;
        this.session = session;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void readNextFrame() {
        ctx.channel().read();
    }

    @Override
    public void startReadingFrames() {
        ctx.channel().config().setAutoRead(true);
    }

    @Override
    public void stopReadingFrames() {
        ctx.channel().config().setAutoRead(false);
    }

    @Override
    public ChannelFuture pushText(String text) {
        return pushText(text, true);
    }

    @Override
    public ChannelFuture pushText(String text, boolean finalFrame) {
        return ctx.writeAndFlush(new TextWebSocketFrame(finalFrame, 0, text));
    }

    @Override
    public ChannelFuture pushBinary(ByteBuffer data) {
        return pushBinary(data, true);
    }

    @Override
    public ChannelFuture pushBinary(ByteBuffer data, boolean finalFrame) {
        return ctx.writeAndFlush(new BinaryWebSocketFrame(finalFrame, 0, getNettyBuf(data)));
    }

    @Override
    public ChannelFuture ping(ByteBuffer data) {
        return ctx.writeAndFlush(new PingWebSocketFrame(getNettyBuf(data)));
    }

    @Override
    public ChannelFuture pong(ByteBuffer data) {
        return ctx.writeAndFlush(new PongWebSocketFrame(getNettyBuf(data)));
    }

    @Override
    public ChannelFuture close(int statusCode, String reason) {
        return ctx.writeAndFlush(new CloseWebSocketFrame(statusCode, reason));
    }

    @Override
    public ChannelFuture close() {
        return ctx.close();
    }

    @Deprecated
    public DefaultWebSocketSession getDefaultWebSocketSession() {
        return session;
    }

    public ByteBuf getNettyBuf(ByteBuffer buffer) {
        return Unpooled.wrappedBuffer(buffer);
    }
}
