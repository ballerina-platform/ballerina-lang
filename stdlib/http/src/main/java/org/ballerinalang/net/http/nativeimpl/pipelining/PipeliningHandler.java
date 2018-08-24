/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.nativeimpl.pipelining;

import io.netty.channel.ChannelHandlerContext;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.DataContext;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Queue;

import static org.ballerinalang.net.http.HttpUtil.sendOutboundResponse;
import static org.ballerinalang.net.http.nativeimpl.connection.ResponseWriter.sendResponseRobust;

/**
 * Handle pipeline responses.
 */
public class PipeliningHandler {

    private static void sendPipelinedResponseRobust(DataContext dataContext, HttpCarbonMessage requestMessage,
                                                    BMap<String, BValue> outboundResponseStruct,
                                                    HttpCarbonMessage responseMessage) {
        sendResponseRobust(dataContext, requestMessage, outboundResponseStruct, responseMessage);
    }

    public static HttpResponseFuture sendPipelinedResponse(HttpCarbonMessage requestMsg,
                                                           HttpCarbonMessage responseMsg) {
        HttpResponseFuture responseFuture = null;
        try {
            responseMsg.setPipeliningNeeded(requestMsg.isPipeliningNeeded());
            if (pipeliningRequired(requestMsg)) {
                PipelinedResponse pipelinedResponse = new PipelinedResponse(requestMsg.getSequenceId(),
                        requestMsg, responseMsg);
                responseMsg.setPipelineListener(new PipelineResponseListener());
                responseFuture = executePipeliningLogic(requestMsg.getSourceContext(), pipelinedResponse);
            } else {
                responseFuture = requestMsg.respond(responseMsg);
            }
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
        return responseFuture;
    }

    public static HttpResponseFuture executePipeliningLogic(ChannelHandlerContext sourceContext,
                                                            PipelinedResponse pipelinedResponse) {
        HttpResponseFuture responseFuture = null;

        synchronized (sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get()) {
            Queue<PipelinedResponse> responseQueue = sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get();
            if (thresholdReached(sourceContext, responseQueue)) {
                return null;
            }
            if (pipelinedResponse != null) {
                responseQueue.add(pipelinedResponse);
            }
            while (!responseQueue.isEmpty()) {
                Integer nextSequenceNumber = sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get();
                final PipelinedResponse queuedPipelinedResponse = responseQueue.peek();
                int currentSequenceNumber = queuedPipelinedResponse.getSequenceId();
                if (currentSequenceNumber != nextSequenceNumber) {
                    break;
                }
                responseQueue.remove();
                //IMPORTANT: Do not increment the nextSequenceNumber after 'sendOutboundResponseRobust()' or
                // 'sendOutboundResponse()' call under any circumstance.  nextSequenceNumber should be updated only
                // when the last http content of this message has been written to the socket because in case if
                // one response has delayed http contents, there's a good chance that the contents of another
                // response will be sent out before its turn.
                if (queuedPipelinedResponse.getDataContext() != null &&
                        queuedPipelinedResponse.getOutboundResponseStruct() != null) {
                    sendPipelinedResponseRobust(queuedPipelinedResponse.getDataContext(),
                            queuedPipelinedResponse.getInboundRequestMsg(),
                            queuedPipelinedResponse.getOutboundResponseStruct(),
                            queuedPipelinedResponse.getOutboundResponseMsg());
                } else {
                    responseFuture = sendOutboundResponse(queuedPipelinedResponse.getInboundRequestMsg(),
                            queuedPipelinedResponse.getOutboundResponseMsg());
                }
            }
            return responseFuture;
        }
    }

    public static boolean pipeliningRequired(HttpCarbonMessage request) {
        String httpVersion = (String) request.getProperty(Constants.HTTP_VERSION);
        return request.isPipeliningNeeded() && request.isKeepAlive() &&
                !Constants.HTTP2_VERSION.equalsIgnoreCase(httpVersion);
    }

    /**
     * When the maximum queued response count reached, close the connection because queuing up indefinitely might cause
     * out of memory issues.
     *
     * @param sourceContext Represent channel handler context
     * @param responseQueue Represent pipelined response queue
     * @return a boolean indicating whether the maximum queued response count is reached
     */
    private static boolean thresholdReached(ChannelHandlerContext sourceContext,
                                            Queue<PipelinedResponse> responseQueue) {
        Integer maxQueuedResponses = sourceContext.channel()
                .attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).get();
        if (responseQueue.size() > maxQueuedResponses) {
            sourceContext.close();
            return true;
        }
        return false;
    }
}
