package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketFrameType;
import org.wso2.transport.http.netty.contractimpl.listener.WebSocketMessageQueueHandler;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.wso2.transport.http.netty.contract.Constants.MESSAGE_QUEUE_HANDLER;

/**
 * Default implementation of {@link WebSocketConnection}.
 */
public class DefaultWebSocketConnection implements WebSocketConnection {

    private final ChannelHandlerContext ctx;
    private final WebSocketInboundFrameHandler frameHandler;
    private final boolean secure;
    private final InetSocketAddress localAddress;
    private WebSocketMessageQueueHandler webSocketMessageQueueHandler;
    private WebSocketFrameType continuationFrameType;
    private boolean closeFrameSent;
    private int closeInitiatedStatusCode;
    private String id;
    private String negotiatedSubProtocol;

    public DefaultWebSocketConnection(ChannelHandlerContext ctx, WebSocketInboundFrameHandler frameHandler,
                                      WebSocketMessageQueueHandler webSocketMessageQueueHandler, boolean secure,
                                      String negotiatedSubProtocol) {
        this.ctx = ctx;
        this.id = WebSocketUtil.getChannelId(ctx);
        this.frameHandler = frameHandler;
        this.webSocketMessageQueueHandler = webSocketMessageQueueHandler;
        this.secure = secure;
        this.localAddress = (InetSocketAddress) ctx.channel().localAddress();
        this.negotiatedSubProtocol = negotiatedSubProtocol;
    }

    @Override
    public String getChannelId() {
        return this.id;
    }

    @Override
    public boolean isOpen() {
        return this.ctx.channel().isOpen();
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public String getHost() {
        return localAddress.getHostName();
    }

    @Override
    public int getPort() {
        return localAddress.getPort();
    }

    @Override
    public String getNegotiatedSubProtocol() {
        return negotiatedSubProtocol;
    }

    @Override
    public void readNextFrame() {
        webSocketMessageQueueHandler.readNextFrame();
    }

    @Override
    public void startReadingFrames() {
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.get(MESSAGE_QUEUE_HANDLER) != null) {
            ctx.pipeline().remove(MESSAGE_QUEUE_HANDLER);
        }
        ctx.channel().config().setAutoRead(true);
    }

    @Override
    public void stopReadingFrames() {
        ctx.channel().config().setAutoRead(false);
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.get(MESSAGE_QUEUE_HANDLER) == null) {
            ctx.pipeline().addBefore(Constants.WEBSOCKET_FRAME_HANDLER, MESSAGE_QUEUE_HANDLER,
                                     webSocketMessageQueueHandler);
        }
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
            throw new IllegalStateException("Close frame already sent. Cannot push text data!");
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
            throw new IllegalStateException("Close frame already sent. Cannot push binary data.");
        }
        if (continuationFrameType != null) {
            if (finalFrame) {
                continuationFrameType = null;
            }
            return ctx.writeAndFlush(new ContinuationWebSocketFrame(finalFrame, 0, getNettyByteBuf(data)));
        }
        if (!finalFrame) {
            continuationFrameType = WebSocketFrameType.BINARY;
        }
        return ctx.writeAndFlush(new BinaryWebSocketFrame(finalFrame, 0, getNettyByteBuf(data)));
    }

    @Override
    public ChannelFuture ping(ByteBuffer data) {
        return ctx.writeAndFlush(new PingWebSocketFrame(getNettyByteBuf(data)));
    }

    @Override
    public ChannelFuture pong(ByteBuffer data) {
        return ctx.writeAndFlush(new PongWebSocketFrame(getNettyByteBuf(data)));
    }

    @Override
    public ChannelFuture initiateConnectionClosure(int statusCode, String reason) {
        return initiateConnectionClosure(new CloseWebSocketFrame(statusCode, reason));
    }

    @Override
    public ChannelFuture initiateConnectionClosure() {
        return initiateConnectionClosure(new CloseWebSocketFrame());
    }

    private ChannelFuture initiateConnectionClosure(CloseWebSocketFrame closeWebSocketFrame) {
        handleCloseFrameSent();
        closeInitiatedStatusCode = closeWebSocketFrame.statusCode();
        closeInitiatedStatusCode = closeInitiatedStatusCode == -1 ? 1005 : closeInitiatedStatusCode;
        ChannelPromise closePromise = ctx.newPromise();
        ctx.writeAndFlush(closeWebSocketFrame).addListener(future -> {
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
        return finishConnectionClosure(new CloseWebSocketFrame(statusCode, reason));
    }

    @Override
    public ChannelFuture finishConnectionClosure() {
        return finishConnectionClosure(new CloseWebSocketFrame());
    }

    private ChannelFuture finishConnectionClosure(CloseWebSocketFrame closeWebSocketFrame) {
        if (!frameHandler.isCloseFrameReceived()) {
            throw new IllegalStateException("Cannot finish a connection closure without receiving a close frame");
        }
        ChannelPromise channelPromise = ctx.newPromise();
        ctx.writeAndFlush(closeWebSocketFrame).addListener(future -> {
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
        frameHandler.setCloseInitialized(true);
        return ctx.close();
    }

    @Override
    public ChannelFuture terminateConnection(int statusCode, String reason) {
        handleCloseFrameSent();
        ChannelPromise closePromise = ctx.newPromise();
        ctx.writeAndFlush(new CloseWebSocketFrame(statusCode, reason)).addListener(writeFuture -> {
            frameHandler.setCloseInitialized(true);
            Throwable writeCause = writeFuture.cause();
            if (!writeFuture.isSuccess() && writeCause != null) {
                closePromise.setFailure(writeCause);
                ctx.close();
                return;
            }
            ctx.close().addListener(closeFuture -> {
                Throwable closeCause = closeFuture.cause();
                if (!closeFuture.isSuccess() && closeCause != null) {
                    closePromise.setFailure(closeCause);
                } else {
                    closePromise.setSuccess();
                }
            });

        });
        return closePromise;
    }

    private void handleCloseFrameSent() {
        if (closeFrameSent) {
            throw new IllegalStateException("Close frame already sent. Cannot send close frame again.");
        }
        closeFrameSent = true;
    }
    int getCloseInitiatedStatusCode() {
        return this.closeInitiatedStatusCode;
    }

    private ByteBuf getNettyByteBuf(ByteBuffer buffer) {
        return Unpooled.wrappedBuffer(buffer);
    }
}
