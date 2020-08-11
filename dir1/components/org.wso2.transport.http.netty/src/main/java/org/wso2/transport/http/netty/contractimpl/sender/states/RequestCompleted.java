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
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contract.Constants
        .IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * State between end of payload write and start of response headers read.
 */
public class RequestCompleted implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(RequestCompleted.class);

    private final SenderReqRespStateManager senderReqRespStateManager;

    RequestCompleted(SenderReqRespStateManager senderReqRespStateManager) {
        this.senderReqRespStateManager = senderReqRespStateManager;
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest) {
        LOG.warn("writeOutboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestEntity {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        if (httpInboundResponse.status().code() != HttpResponseStatus.CONTINUE.code()) {
            senderReqRespStateManager.state = new ReceivingHeaders(senderReqRespStateManager);
            senderReqRespStateManager.readInboundResponseHeaders(targetHandler, httpInboundResponse);
        }
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {
        LOG.warn("readInboundResponseEntityBody {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(TargetHandler targetHandler, HttpResponseFuture httpResponseFuture) {
        httpResponseFuture.notifyHttpListener(
                new ServerConnectorException(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE));
        LOG.error(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(TargetHandler targetHandler,
                                                   HttpResponseFuture httpResponseFuture, String channelID) {
        senderReqRespStateManager.nettyTargetChannel.pipeline().remove(Constants.IDLE_STATE_HANDLER);
        senderReqRespStateManager.nettyTargetChannel.close();
        httpResponseFuture.notifyHttpListener(
                new EndpointTimeOutException(channelID, IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE,
                                             HttpResponseStatus.GATEWAY_TIMEOUT.code()));
        LOG.error("Error in HTTP client: {}", IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE);
    }
}
