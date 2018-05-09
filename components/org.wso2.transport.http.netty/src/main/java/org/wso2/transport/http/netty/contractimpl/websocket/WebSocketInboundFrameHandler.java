package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.CountDownLatch;

/**
 * Abstract WebSocket frame handler for WebSocket server and client.
 */
public abstract class WebSocketInboundFrameHandler extends ChannelInboundHandlerAdapter {

    /**
     * Set countdown latch for WebSocket connection close.
     *
     * @param closeCountDownLatch {@link CountDownLatch} to wait for WebSocket connection closure.
     */
    public abstract void setCloseCountDownLatch(CountDownLatch closeCountDownLatch);

    /**
     * Retrieve the WebSocket connection associated with the frame handler.
     *
     * @return the WebSocket connection associated with the frame handler.
     */
    public abstract DefaultWebSocketConnection getWebSocketConnection();

    /**
     * Check whether a close frame is received without the relevant connection to this Frame handler sending a close
     * frame.
     *
     * @return true if a close frame is received without the relevant connection to this Frame handler sending a close
     * frame.
     */
    public abstract boolean isCloseFrameReceived();

    /**
     * Retrieve the {@link ChannelHandlerContext} of the {@link WebSocketInboundFrameHandler}.
     *
     * @return the {@link ChannelHandlerContext} of the {@link WebSocketInboundFrameHandler}.
     */
    public abstract ChannelHandlerContext getCtx();

}
