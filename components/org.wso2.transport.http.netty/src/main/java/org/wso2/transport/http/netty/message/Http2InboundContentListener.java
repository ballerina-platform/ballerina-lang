package org.wso2.transport.http.netty.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2LocalFlowController;
import io.netty.handler.codec.http2.Http2Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the HTTP/2 inbound content listener.
 */
public class Http2InboundContentListener implements Listener {
    private static final Logger LOG = LoggerFactory.getLogger(Http2InboundContentListener.class);

    private int streamId;
    private Http2Connection http2Connection;
    private boolean appConsumeRequired = true;
    private AtomicBoolean consumeInboundContent = new AtomicBoolean(true);
    private Http2LocalFlowController http2LocalFlowController;
    private ChannelHandlerContext channelHandlerContext;
    private String inboundType;

    public Http2InboundContentListener(int streamId, ChannelHandlerContext ctx, Http2Connection http2Connection,
                                       String inboundType) {
        this.streamId = streamId;
        this.http2Connection = http2Connection;
        this.http2LocalFlowController = http2Connection.local().flowController();
        this.channelHandlerContext = ctx;
        this.inboundType = inboundType;
    }

    /**
     * Updates the flow controller as soon as the content is received from the source. This updates the window size kept
     * by the sender and prevent the streams from stalling. This is needed in the case where the app doesn't start to
     * consume data until all the content has been received.
     *
     * @param httpContent received data content
     */
    @Override
    public void onAdd(HttpContent httpContent) {
        if (!appConsumeRequired && consumeInboundContent.get()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stream {}. HTTP/2 {} onAdd updateLocalFlowController with bytes {} ", streamId, inboundType,
                          httpContent.content().readableBytes());
            }
            updateLocalFlowController(httpContent.content().readableBytes());
        }
    }

    /**
     * Once the content is removed, update the flow controller with the number of consumed bytes.
     *
     * @param httpContent received data content
     */
    @Override
    public void onRemove(HttpContent httpContent) {
        if (appConsumeRequired && consumeInboundContent.get()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stream {}. HTTP/2 {} onRemove updateLocalFlowController with bytes {} ", streamId,
                          inboundType, httpContent.content().readableBytes());
            }
            updateLocalFlowController(httpContent.content().readableBytes());
        }
    }

    /**
     * Disable app consume flag and consume bytes so that window updates will be issued to the peer.
     */
    @Override
    public void resumeReadInterest() {
        channelHandlerContext.channel().eventLoop().execute(() -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stream {}. In thread {}. {} resumeReadInterest. Unconsumed bytes: {}",
                          streamId, Thread.currentThread().getName(), inboundType, getUnConsumedBytes());
            }
            consumeOutstandingBytes();
            appConsumeRequired = false;
        });
    }

    void stopByteConsumption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Stream {}. In thread {}. {} stop byte consumption", streamId, Thread.currentThread().getName(),
                      inboundType);
        }
        if (consumeInboundContent.get()) {
            consumeInboundContent.set(false);
        }
    }

    /**
     * This method should only be executed in an I/O thread. Do not use {@link #updateLocalFlowController(int)} method
     * inside loop execute since both the {@link #consumeBytes(int)} and {@link #getUnConsumedBytes()} should be
     * executed as a single netty task. Calling {@link #updateLocalFlowController(int)} will produce two netty tasks
     * which will result in an incorrect execution order.
     */
    void resumeByteConsumption() {
        channelHandlerContext.channel().eventLoop().execute(() -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stream {}. In thread {}. {} resume byte consumption. Unconsumed bytes: {}",
                          streamId, Thread.currentThread().getName(), inboundType, getUnConsumedBytes());
            }
            consumeOutstandingBytes();
            consumeInboundContent.set(true);
        });
    }

    /**
     * Immediately consume outstanding bytes so that window updates will be issued to the peer.
     */
    private void consumeOutstandingBytes() {
        int unconsumedBytes = getUnConsumedBytes();
        if (unconsumedBytes > 0) {
            consumeBytes(unconsumedBytes);
        }
    }

    /**
     * Update the local flow controller with the number of consumed bytes. This method content should only be executed
     * in an I/O thread.
     *
     * @param consumedBytes number of consumed bytes
     */
    private void updateLocalFlowController(int consumedBytes) {
        channelHandlerContext.channel().eventLoop().execute(() -> consumeBytes(consumedBytes));
    }

    private void consumeBytes(int consumedBytes) {
        try {
            Http2Stream http2Stream = getHttp2Stream();
            //Because of the netty task execution order is not predictable, onAdd() method's task can get executed
            //after resumeByteConsumption(), even though onAdd() gets called first. Therefore by the time onAdd()'s
            //task gets executed, actual unconsumed bytes might be less than the ones depicted by consumedBytes.
            //ISSUE [https://github.com/netty/netty/issues/9128]
            if (http2Stream != null && consumedBytes <= getUnConsumedBytes()) {
                boolean windowUpdateSent = http2LocalFlowController.consumeBytes(http2Stream, consumedBytes);
                if (windowUpdateSent) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Stream {}. {} windowUpdateSent and flushed {} bytes", streamId, inboundType,
                                  consumedBytes);
                    }
                    channelHandlerContext.flush();
                }
            }
        } catch (Http2Exception e) {
            LOG.error("{} Error updating local flow controller. Stream {}. ConsumedBytes {}. Error code {} ",
                      inboundType, streamId, consumedBytes, e.error().name());
        }
    }

    /**
     * Get the number of unconsumed bytes from the local flow controller. This should only be called from an I/O
     * thread.
     *
     * @return the number of unconsumed bytes
     */
    private int getUnConsumedBytes() {
        Http2Stream http2Stream = getHttp2Stream();
        if (http2Stream != null) {
            return http2LocalFlowController.unconsumedBytes(http2Stream);
        }
        return 0;
    }

    private Http2Stream getHttp2Stream() {
        return http2Connection.stream(streamId);
    }
}
