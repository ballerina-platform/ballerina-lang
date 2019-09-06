/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.exceptions.ClientConnectorException;
import org.wso2.transport.http.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.wso2.transport.http.netty.contract.Constants
        .REMOTE_SERVER_CLOSED_BEFORE_READING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.contractimpl.common.Util.safelyRemoveHandlers;

/**
 * This class implements SenderStates. The responsibility of this class is to handle requests with
 * Expect Continue header. As in the spec, it should only send headers of the request and wait for server response
 * before sending the request body.
 */
public class Sending100Continue implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(Sending100Continue.class);

    private final SenderReqRespStateManager senderReqRespStateManager;

    private final HttpResponseFuture httpInboundResponseFuture;
    private TargetHandler targetHandler;
    private HttpCarbonMessage httpOutboundRequest;
    private List<HttpContent> contentList = new ArrayList<>();

    Sending100Continue(SenderReqRespStateManager senderReqRespStateManager,
                              HttpResponseFuture httpInboundResponseFuture) {
        this.senderReqRespStateManager = senderReqRespStateManager;
        this.httpInboundResponseFuture = httpInboundResponseFuture;
        configIdleTimeoutTrigger(senderReqRespStateManager.socketTimeout / 5);
    }

    private void configIdleTimeoutTrigger(int socketIdleTimeout) {
        ChannelPipeline pipeline = senderReqRespStateManager.nettyTargetChannel.pipeline();
        IdleStateHandler idleStateHandler = new IdleStateHandler(0, 0, socketIdleTimeout, TimeUnit.MILLISECONDS);
        safelyRemoveHandlers(pipeline, Constants.IDLE_STATE_HANDLER);
        if (pipeline.get(Constants.TARGET_HANDLER) == null) {
            pipeline.addLast(Constants.IDLE_STATE_HANDLER, idleStateHandler);
        } else {
            pipeline.addBefore(Constants.TARGET_HANDLER, Constants.IDLE_STATE_HANDLER, idleStateHandler);
        }
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest) {
        this.httpOutboundRequest = httpOutboundRequest;
    }

    @Override
    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        contentList.add(httpContent);
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        this.targetHandler = targetHandler;
        configIdleTimeoutTrigger(senderReqRespStateManager.socketTimeout);

        if (httpInboundResponse.status().code() == HttpResponseStatus.CONTINUE.code()) {
            senderReqRespStateManager.state =
                    new SendingEntityBody(senderReqRespStateManager, httpInboundResponseFuture);

            for (HttpContent cachedHttpContent : contentList) {
                senderReqRespStateManager.writeOutboundRequestEntity(httpOutboundRequest, cachedHttpContent);
            }
        } else {
            if (targetHandler.getHttpResponseFuture() != null) {
                for (HttpContent cachedHttpContent : contentList) {
                    cachedHttpContent.release();
                }
                senderReqRespStateManager.state = new ReceivingHeaders(senderReqRespStateManager);
                senderReqRespStateManager.readInboundResponseHeaders(targetHandler, httpInboundResponse);
            } else {
                LOG.error("Cannot notify the response to client as there is no associated responseFuture");
            }
        }
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) throws Exception {
        senderReqRespStateManager.state = new ReceivingEntityBody(senderReqRespStateManager, targetHandler);
        senderReqRespStateManager.readInboundResponseEntityBody(ctx, httpContent, inboundResponseMsg);
    }

    @Override
    public void handleAbruptChannelClosure(TargetHandler targetHandler, HttpResponseFuture httpResponseFuture) {
        for (HttpContent cachedHttpContent : contentList) {
            cachedHttpContent.release();
        }

        httpResponseFuture
                .notifyHttpListener(
                        new ClientConnectorException(senderReqRespStateManager.nettyTargetChannel.id().asShortText(),
                                                     REMOTE_SERVER_CLOSED_BEFORE_READING_100_CONTINUE_RESPONSE));
        LOG.error("Error in HTTP client: {}", REMOTE_SERVER_CLOSED_BEFORE_READING_100_CONTINUE_RESPONSE);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(TargetHandler targetHandler,
                                                   HttpResponseFuture httpResponseFuture, String channelID) {
        configIdleTimeoutTrigger(senderReqRespStateManager.socketTimeout);
        senderReqRespStateManager.state =
                new SendingEntityBody(senderReqRespStateManager, httpInboundResponseFuture);

        for (HttpContent cachedHttpContent : contentList) {
            senderReqRespStateManager.writeOutboundRequestEntity(httpOutboundRequest, cachedHttpContent);
        }
    }
}

