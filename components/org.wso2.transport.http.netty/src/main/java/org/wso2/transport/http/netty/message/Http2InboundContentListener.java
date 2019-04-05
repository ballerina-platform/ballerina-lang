package org.wso2.transport.http.netty.message;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the HTTP/2 inbound content listener.
 */
public class Http2InboundContentListener implements Listener {
    private static final Logger LOG = LoggerFactory.getLogger(Http2InboundContentListener.class);

    private int streamId;
    private Http2Connection http2Connection;
    private boolean applicationConsumedBytes = true;

    public Http2InboundContentListener(int streamId, Http2Connection http2Connection) {
        this.streamId = streamId;
        this.http2Connection = http2Connection;
    }

    /**
     * Updates the flow controller as soon as the content is received from the source. This updates the window size kept
     * by the sender and prevent the streams from stalling.
     *
     * @param httpContent of the message
     */
    @Override
    public void onAdd(HttpContent httpContent) {
        if (!applicationConsumedBytes) {
            updateLocalFlowController(httpContent);
        }
    }

    /**
     * Once the content is removed, update the flow controller with the number of consumed bytes.
     *
     * @param httpContent of the message
     */
    @Override
    public void onRemove(HttpContent httpContent) {
        if (applicationConsumedBytes) {
            updateLocalFlowController(httpContent);
        }
    }

    @Override
    public void resumeReadInterest() {
        applicationConsumedBytes = false;
    }

    private void updateLocalFlowController(HttpContent httpContent) {
        Http2Stream stream = http2Connection.stream(streamId);
        try {
            http2Connection.local().flowController().consumeBytes(stream, httpContent.content().readableBytes());
        } catch (Http2Exception e) {
            LOG.error("Error updating flow controller. {} ", e.getCause());
        }
    }
}
