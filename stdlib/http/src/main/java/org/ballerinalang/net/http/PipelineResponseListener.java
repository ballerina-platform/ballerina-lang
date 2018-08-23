package org.ballerinalang.net.http;

import io.netty.channel.ChannelHandlerContext;
import org.ballerinalang.net.http.nativeimpl.connection.PipelinedResponse;
import org.ballerinalang.net.http.nativeimpl.connection.PipeliningHandler;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contractimpl.HttpPipelineListener;

import java.util.Queue;

/**
 * Created by rukshani on 8/22/18.
 */
public class PipelineResponseListener implements HttpPipelineListener {


    @Override
    public void onResponseSend(ChannelHandlerContext sourceContext) {

        Queue<PipelinedResponse> responseQueue = sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get();

        Integer maxQueuedResponses = sourceContext.channel().attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).get();
        if (responseQueue.size() > maxQueuedResponses) {
            //Cannot queue up indefinitely which might cause out of memory issues, so closing the connection
            sourceContext.close();
            return;
        }
        while (!responseQueue.isEmpty()) {
            Integer nextSequenceNumber = sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get();
            final PipelinedResponse queuedPipelinedResponse = responseQueue.peek();
            int currentSequenceNumber = queuedPipelinedResponse.getSequenceId();
            if (currentSequenceNumber != nextSequenceNumber) {
                break;
            }
            responseQueue.remove();
            //TODO:
            //ctx.write(queuedPipelinedResponse.getResponse());
            //call send response robust
            PipeliningHandler.sendOutboundResponseRobust(queuedPipelinedResponse.getDataContext(),
                    queuedPipelinedResponse.getInboundRequestMsg(),
                    queuedPipelinedResponse.getOutboundResponseStruct(),
                    queuedPipelinedResponse.getOutboundResponseMsg());
        }
    }


}
