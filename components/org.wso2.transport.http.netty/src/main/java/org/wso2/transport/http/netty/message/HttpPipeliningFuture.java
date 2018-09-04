package org.wso2.transport.http.netty.message;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.HttpPipelineListener;

/**
 * Piplining logic to be executed in future.
 *
 * @since 6.0.228
 */
public class HttpPipeliningFuture {
    private static final Logger log = LoggerFactory.getLogger(HttpPipeliningFuture.class);
    private HttpPipelineListener pipelineListener;

    public void setPipelineListener(HttpPipelineListener pipelineListener) {
        this.pipelineListener = pipelineListener;
    }

    public void notifyPipeliningListener(ChannelHandlerContext sourceContext) {
        if (this.pipelineListener != null) {
            this.pipelineListener.onLastHttpContentSent(sourceContext);
        } else {
            log.error("Http pipelining listener is not set");
        }
    }
}
