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

import io.ballerina.runtime.util.exceptions.BallerinaConnectorException;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpPipeliningFuture;

import java.util.Queue;

import static org.ballerinalang.net.http.HttpUtil.sendOutboundResponse;
import static org.ballerinalang.net.http.nativeimpl.connection.ResponseWriter.sendResponseRobust;

/**
 * Manages pipelined responses.
 *
 * @since 0.982.0
 */
public class PipeliningHandler {

    private static final Logger log = LoggerFactory.getLogger(PipeliningHandler.class);

    /**
     * This method should be used whenever a response should be sent out via other places (eg:- error responses,
     * special scenarios like responses for options method calls etc..)  other than the respond() call.
     *
     * @param requestMsg  Represents the request message
     * @param responseMsg Represents the corresponding response
     * @return HttpResponseFuture that represent the future results
     */
    public static HttpResponseFuture sendPipelinedResponse(HttpCarbonMessage requestMsg,
                                                           HttpCarbonMessage responseMsg) {
        HttpResponseFuture responseFuture;
        try {
            responseMsg.setPipeliningEnabled(requestMsg.isPipeliningEnabled());
            if (pipeliningRequired(requestMsg)) {
                PipelinedResponse pipelinedResponse = new PipelinedResponse(requestMsg, responseMsg);
                setPipeliningListener(responseMsg);
                responseFuture = executePipeliningLogic(requestMsg.getSourceContext(), pipelinedResponse);
            } else {
                responseFuture = requestMsg.respond(responseMsg);
            }
        } catch (org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred while sending outbound response", e);
        }
        return responseFuture;
    }

    /**
     * Executes pipelining logic.
     *
     * @param sourceContext     Represents channel handler context
     * @param pipelinedResponse Represents pipelined response
     * @return HttpResponseFuture that represent the future results
     */
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
                long nextSequenceNumber = sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get();
                final PipelinedResponse queuedPipelinedResponse = responseQueue.peek();
                long currentSequenceNumber = queuedPipelinedResponse.getSequenceId();
                if (currentSequenceNumber != nextSequenceNumber) {
                    break;
                }
                responseQueue.remove();
                //IMPORTANT: Do not increment the nextSequenceNumber after 'sendOutboundResponseRobust()' or
                //'sendOutboundResponse()' under any circumstance.  nextSequenceNumber should be updated only
                //when the last http content of this message has been written to the socket because in case if
                //one response has delayed http contents, there's a good chance that the contents of another
                //response will be sent out before its turn.
                if (queuedPipelinedResponse.getDataContext() != null &&
                        queuedPipelinedResponse.getOutboundResponseObj() != null) {
                    sendResponseRobust(queuedPipelinedResponse.getDataContext(),
                            queuedPipelinedResponse.getInboundRequestMsg(),
                            queuedPipelinedResponse.getOutboundResponseObj(),
                            queuedPipelinedResponse.getOutboundResponseMsg());
                } else {
                    responseFuture = sendOutboundResponse(queuedPipelinedResponse.getInboundRequestMsg(),
                            queuedPipelinedResponse.getOutboundResponseMsg());
                }
            }
            return responseFuture;
        }
    }

    /**
     * Check whether the pipelining is required.
     *
     * @param request Represents the request message
     * @return a boolean indicating whether the pipelining is required
     */
    public static boolean pipeliningRequired(HttpCarbonMessage request) {
        String httpVersion = request.getHttpVersion();
        return request.isPipeliningEnabled() && request.isKeepAlive() &&
                Constants.HTTP_1_1_VERSION.equalsIgnoreCase(httpVersion);
    }

    /**
     * When the maximum queued response count reached, close the connection because queuing up indefinitely might cause
     * out of memory issues.
     *
     * @param sourceContext Represents channel handler context
     * @param responseQueue Represents pipelined response queue
     * @return a boolean indicating whether the maximum queued response count is reached
     */
    private static boolean thresholdReached(ChannelHandlerContext sourceContext,
                                            Queue<PipelinedResponse> responseQueue) {
        long maxQueuedResponses = sourceContext.channel()
                .attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).get();
        if (Constants.UNBOUNDED_RESPONSE_QUEUE == maxQueuedResponses) {
            return false;
        }

        if (responseQueue.size() > maxQueuedResponses) {
            sourceContext.channel().close();
            log.warn("Threshold {} for pipelined response queue reached hence closing the connection.",
                    maxQueuedResponses);
            return true;
        }
        return false;
    }

    /**
     * Set pipelining listener to outbound response.
     *
     * @param httpResponse Represent HTTP outbound response
     */
    public static void setPipeliningListener(HttpCarbonMessage httpResponse) {
        PipelineResponseListener pipeliningListener = new PipelineResponseListener();
        HttpPipeliningFuture pipeliningFuture = new HttpPipeliningFuture();
        pipeliningFuture.setPipeliningListener(pipeliningListener);
        httpResponse.setPipeliningFuture(pipeliningFuture);
    }
}
