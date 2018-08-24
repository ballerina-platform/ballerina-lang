package org.ballerinalang.net.http.nativeimpl.pipelining;

import io.netty.channel.ChannelHandlerContext;
import org.wso2.transport.http.netty.contractimpl.HttpPipelineListener;

import static org.ballerinalang.net.http.nativeimpl.pipelining.PipeliningHandler.executePipeliningLogic;

public class PipelineResponseListener implements HttpPipelineListener {


    @Override
    public void onLastHttpContentSent(ChannelHandlerContext sourceContext) {
        executePipeliningLogic(sourceContext, null);
    }
}
