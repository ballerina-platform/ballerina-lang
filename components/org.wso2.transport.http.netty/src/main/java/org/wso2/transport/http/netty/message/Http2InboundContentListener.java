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
    private AtomicBoolean resumeRead = new AtomicBoolean(true);
    private Http2LocalFlowController http2LocalFlowController;
    private ChannelHandlerContext channelHandlerContext;

    public Http2InboundContentListener(int streamId, ChannelHandlerContext ctx, Http2Connection http2Connection) {
        this.streamId = streamId;
        this.http2Connection = http2Connection;
        this.http2LocalFlowController = http2Connection.local().flowController();
        this.channelHandlerContext = ctx;
    }

    /**
     * Updates the flow controller as soon as the content is received from the source. This updates the window size kept
     * by the sender and prevent the streams from stalling.
     *
     * @param httpContent of the message
     */
    @Override
    public void onAdd(HttpContent httpContent) {
        if (!appConsumeRequired && resumeRead.get()) {
//            LOG.warn("HTTP/2 inbound onAdd updateLocalFlowController {} ", httpContent.content().readableBytes());
            updateLocalFlowController(httpContent.content().readableBytes());
        }
    }

    /**
     * Once the content is removed, update the flow controller with the number of consumed bytes.
     *
     * @param httpContent of the message
     */
    @Override
    public void onRemove(HttpContent httpContent) {
        if (appConsumeRequired && resumeRead.get()) {
//            LOG.warn("HTTP/2 inbound onRemove updateLocalFlowController {} ", httpContent.content().readableBytes());
            updateLocalFlowController(httpContent.content().readableBytes());
        }
    }

    /**
     * Disable app consume flag so that window updates will be issued to the peer.
     */
    @Override
    public void resumeReadInterest() {
        appConsumeRequired = false;
    }

    //Always gets called from ballerina thread
    void stopByteConsumption() {
//        LOG.warn("Stop byte consumption");
        if (resumeRead.get()) {
            resumeRead.set(false);
        }
    }

    //Always gets called from I/O thread. TODO:If this is true then there's no need for eventloop execute here.
    void resumeByteConsumption() {
//        LOG.warn("In thread {}. Resume byte consumption. Unconsumed bytes: {}", Thread.currentThread().getName(),
//                 getUnConsumedBytes());
        resumeRead.set(true);
        channelHandlerContext.channel().eventLoop().execute(() -> updateLocalFlowController(getUnConsumedBytes()));
    }

    //This method content should only be executed in an I/O thread
    private void updateLocalFlowController(int consumedBytes) {
        channelHandlerContext.channel().eventLoop().execute(() -> {
            try {
                boolean windowUpdateSent = http2LocalFlowController.consumeBytes(getHttp2Stream(), consumedBytes);
                if (windowUpdateSent) {
//                    LOG.warn("windowUpdateSent {} and flushed {} bytes", windowUpdateSent, consumedBytes);
                    channelHandlerContext.flush();
                }
            } catch (Http2Exception e) {
                LOG.error("Error updating local flow controller. {} ", e.error().code());
            }
        });
    }

    //Should always be called from an I/O thread
    private int getUnConsumedBytes() {
        return http2LocalFlowController.unconsumedBytes(getHttp2Stream());
    }

    private Http2Stream getHttp2Stream() {
        return http2Connection.stream(streamId);
    }
}
