package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.channel.ChannelHandlerContext;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.internal.websocket.DefaultWebSocketSession;

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

    public DefaultWebSocketSession getDefaultWebSocketSession() {
        return session;
    }
}
