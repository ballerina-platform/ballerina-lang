package org.ballerinalang.net.http.nativeimpl.pipelining;

import io.netty.channel.ChannelHandlerContext;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contractimpl.HttpPipelineListener;

import static org.ballerinalang.net.http.nativeimpl.pipelining.PipeliningHandler.executePipeliningLogic;
import static org.ballerinalang.net.http.nativeimpl.pipelining.PipeliningHandler.thresholdReached;

public class PipelineResponseListener implements HttpPipelineListener {


    @Override
    public void onResponseSend(ChannelHandlerContext sourceContext) {

        if (thresholdReached(sourceContext, sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get())) {
            return;
        }
        executePipeliningLogic(sourceContext, null);
    }
}
