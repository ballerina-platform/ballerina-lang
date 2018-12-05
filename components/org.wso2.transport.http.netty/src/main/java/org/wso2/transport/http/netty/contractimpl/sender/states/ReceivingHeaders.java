/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.sender.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http2.Http2CodecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.common.states.MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contract.Constants
        .IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants
        .REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage;

/**
 * State between start and end of inbound response headers read.
 */
public class ReceivingHeaders implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingHeaders.class);

    private final MessageStateContext messageStateContext;
    private TargetHandler targetHandler;

    ReceivingHeaders(MessageStateContext messageStateContext) {
        this.messageStateContext = messageStateContext;
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestEntity {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        this.targetHandler = targetHandler;
        OutboundMsgHolder msgHolder = targetHandler.getHttp2TargetHandler()
                .getHttp2ClientChannel().getInFlightMessage(Http2CodecUtil.HTTP_UPGRADE_STREAM_ID);
        if (msgHolder != null) {
            // Response received over HTTP/1.x connection, so mark no push promises available in the channel
            msgHolder.markNoPromisesReceived();
        }
        if (targetHandler.getHttpResponseFuture() != null) {
            HttpCarbonMessage inboundResponseMsg = targetHandler.getInboundResponseMsg();
            inboundResponseMsg.setTargetContext(targetHandler.getContext());
            targetHandler.getHttpResponseFuture().notifyHttpListener(inboundResponseMsg);
        } else {
            LOG.error("Cannot notify the response to client as there is no associated responseFuture");
        }

        if (httpInboundResponse.decoderResult().isFailure()) {
            LOG.warn(httpInboundResponse.decoderResult().cause().getMessage());
        }
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) throws Exception {
        messageStateContext.setSenderState(new ReceivingEntityBody(messageStateContext, targetHandler));
        messageStateContext.getSenderState().readInboundResponseEntityBody(ctx, httpContent, inboundResponseMsg);
    }

    @Override
    public void handleAbruptChannelClosure(HttpResponseFuture httpResponseFuture) {
        handleIncompleteInboundMessage(targetHandler.getInboundResponseMsg(),
                                        REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_HEADERS);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(HttpResponseFuture httpResponseFuture, String channelID) {
        handleIncompleteInboundMessage(targetHandler.getInboundResponseMsg(),
                                        IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE_HEADERS);
    }
}
