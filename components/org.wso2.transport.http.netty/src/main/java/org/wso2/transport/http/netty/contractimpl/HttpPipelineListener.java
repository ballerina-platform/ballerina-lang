package org.wso2.transport.http.netty.contractimpl;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by rukshani on 8/22/18.
 */
public interface HttpPipelineListener {

    void onResponseSend(ChannelHandlerContext channelHandlerContext);

}
