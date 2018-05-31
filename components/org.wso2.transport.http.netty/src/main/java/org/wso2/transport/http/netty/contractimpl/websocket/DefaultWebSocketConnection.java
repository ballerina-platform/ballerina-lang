package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketFrameType;
import org.wso2.transport.http.netty.internal.websocket.DefaultWebSocketSession;
import org.wso2.transport.http.netty.listener.WebSocketFramesBlockingHandler;

import java.nio.ByteBuffer;
import javax.websocket.Session;

/**
 * Default implementation of {@link WebSocketConnection}.
 */
public class DefaultWebSocketConnection implements WebSocketConnection {

    private final WebSocketInboundFrameHandler frameHandler;
    private final ChannelHandlerContext ctx;
    private final DefaultWebSocketSession session;
    private WebSocketFramesBlockingHandler blockingHandler;
    private WebSocketFrameType continuationFrameType;
    private boolean closeFrameSent;
    private int closeInitiatedStatusCode;

    public DefaultWebSocketConnection(ChannelHandlerContext ctx, WebSocketInboundFrameHandler frameHandler,
                                      WebSocketFramesBlockingHandler blockingHandler,
                                      DefaultWebSocketSession session) {
        this.ctx = ctx;
        this.frameHandler = frameHandler;
        this.blockingHandler = blockingHandler;
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
        blockingHandler.readNextFrame();
    }

    @Override
    public void startReadingFrames() {
        ctx.pipeline().remove(Constants.WEBSOCKET_FRAME_BLOCKING_HANDLER);
        ctx.channel().config().setAutoRead(true);
    }

    @Override
    public void stopReadingFrames() {
        ctx.channel().config().setAutoRead(false);
        ctx.pipeline().addBefore(Constants.WEBSOCKET_FRAME_HANDLER, Constants.WEBSOCKET_FRAME_BLOCKING_HANDLER,
                                 blockingHandler = new WebSocketFramesBlockingHandler());
    }

    @Override
    public ChannelFuture pushText(String text) {
        return pushText(text, true);
    }

    @Override
    public ChannelFuture pushText(String text, boolean finalFrame) {
        if (continuationFrameType == WebSocketFrameType.BINARY) {
            throw new IllegalStateException("Cannot interrupt WebSocket binary frame continuation");
        }
        if (closeFrameSent) {
            throw new IllegalStateException("Already sent close frame. Cannot push text data!");
        }
        if (continuationFrameType != null) {
            if (finalFrame) {
                continuationFrameType = null;
            }
            return ctx.writeAndFlush(new ContinuationWebSocketFrame(finalFrame, 0, text));
        }
        if (!finalFrame) {
            continuationFrameType = WebSocketFrameType.TEXT;
        }
        return ctx.writeAndFlush(new TextWebSocketFrame(finalFrame, 0, text));
    }

    @Override
    public ChannelFuture pushBinary(ByteBuffer data) {
        return pushBinary(data, true);
    }

    @Override
    public ChannelFuture pushBinary(ByteBuffer data, boolean finalFrame) {
        if (continuationFrameType == WebSocketFrameType.TEXT) {
            throw new IllegalStateException("Cannot interrupt WebSocket text frame continuation");
        }
        if (closeFrameSent) {
            throw new IllegalStateException("Already sent close frame. Cannot push binary data!");
        }
        if (continuationFrameType != null) {
            if (finalFrame) {
                continuationFrameType = null;
            }
            return ctx.writeAndFlush(new ContinuationWebSocketFrame(finalFrame, 0, getNettyBuf(data)));
        }
        if (!finalFrame) {
            continuationFrameType = WebSocketFrameType.BINARY;
        }
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
    public ChannelFuture initiateConnectionClosure(int statusCode, String reason) {
        if (closeFrameSent) {
            throw new IllegalStateException("Already sent close frame. Cannot send close frame again!");
        }
        closeFrameSent = true;
        closeInitiatedStatusCode = statusCode;
        ChannelPromise closePromise = ctx.newPromise();
        ctx.writeAndFlush(new CloseWebSocketFrame(statusCode, reason)).addListener(future -> {
            frameHandler.setClosePromise(closePromise);
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                ctx.close().addListener(closeFuture -> closePromise.setFailure(cause));
            }
        });
        return closePromise;
    }

    @Override
    public ChannelFuture finishConnectionClosure(int statusCode, String reason) {
        if (!frameHandler.isCloseFrameReceived()) {
            throw new IllegalStateException("Cannot finish a connection closure without receiving a close frame");
        }
        ChannelPromise channelPromise = ctx.newPromise();
        ctx.writeAndFlush(new CloseWebSocketFrame(statusCode, reason)).addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                ctx.close().addListener(closeFuture -> channelPromise.setFailure(cause));
                return;
            }
            ctx.close().addListener(closeFuture -> channelPromise.setSuccess());
        });
        return channelPromise;
    }

    @Override
    public ChannelFuture terminateConnection() {
        return ctx.close();
    }

    public int getCloseInitiatedStatusCode() {
        return this.closeInitiatedStatusCode;
    }

    @Deprecated
    public DefaultWebSocketSession getDefaultWebSocketSession() {
        return session;
    }

    public ByteBuf getNettyBuf(ByteBuffer buffer) {
        return Unpooled.wrappedBuffer(buffer);
    }
}
