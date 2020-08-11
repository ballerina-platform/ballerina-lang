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

package org.ballerinalang.net.netty.contractimpl.sender.states;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contract.exceptions.ClientConnectorException;
import org.ballerinalang.net.netty.contract.exceptions.EndpointTimeOutException;
import org.ballerinalang.net.netty.contractimpl.common.Util;
import org.ballerinalang.net.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.ballerinalang.net.netty.contractimpl.common.states.StateUtil;
import org.ballerinalang.net.netty.contractimpl.sender.TargetHandler;
import org.ballerinalang.net.netty.internal.HandlerExecutor;
import org.ballerinalang.net.netty.internal.HttpTransportContextHolder;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

/**
 * State between start and end of outbound request entity body write.
 */
public class SendingEntityBody implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingEntityBody.class);

    private final org.ballerinalang.net.netty.contractimpl.common.states.SenderReqRespStateManager
            senderReqRespStateManager;

    private final HandlerExecutor handlerExecutor;
    private final org.ballerinalang.net.netty.contract.HttpResponseFuture httpInboundResponseFuture;

    SendingEntityBody(SenderReqRespStateManager senderReqRespStateManager,
                      org.ballerinalang.net.netty.contract.HttpResponseFuture httpInboundResponseFuture) {
        this.senderReqRespStateManager = senderReqRespStateManager;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.httpInboundResponseFuture = httpInboundResponseFuture;
    }

    @Override
    public void writeOutboundRequestHeaders(org.ballerinalang.net.netty.message.HttpCarbonMessage httpOutboundRequest) {
        LOG.warn("writeOutboundRequestHeaders {}", StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundRequestEntity(org.ballerinalang.net.netty.message.HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        if (Util.isLastHttpContent(httpContent)) {
            writeOutboundRequestBody(httpContent);

            if (handlerExecutor != null) {
                handlerExecutor.executeAtTargetRequestSending(httpOutboundRequest);
            }
        } else {
            senderReqRespStateManager.nettyTargetChannel.writeAndFlush(httpContent);
        }
    }

    @Override
    public void readInboundResponseHeaders(org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        // If this method is called, it is an application error. Inbound response is receiving before the completion
        // of request body write.
        if (httpInboundResponse.status().code() != HttpResponseStatus.CONTINUE.code()) {
            targetHandler.getOutboundRequestMsg().setIoException(new IOException(
                    org.ballerinalang.net.netty.contract.Constants.INBOUND_RESPONSE_ALREADY_RECEIVED));
            senderReqRespStateManager.state = new ReceivingHeaders(senderReqRespStateManager);
            senderReqRespStateManager.readInboundResponseHeaders(targetHandler, httpInboundResponse);
        }
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {
        LOG.warn("readInboundResponseEntityBody {}", StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler, org.ballerinalang.net.netty.contract.HttpResponseFuture httpResponseFuture) {
        targetHandler.getOutboundRequestMsg()
                .setIoException(new IOException(
                        org.ballerinalang.net.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY));
        httpResponseFuture.notifyHttpListener(
                        new ClientConnectorException(senderReqRespStateManager.nettyTargetChannel.id().asShortText(),
                                                     org.ballerinalang.net.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY));
        LOG.error("Error in HTTP client: {}", org.ballerinalang.net.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(TargetHandler targetHandler,
                                                   HttpResponseFuture httpResponseFuture, String channelID) {

        senderReqRespStateManager.nettyTargetChannel.pipeline().remove(org.ballerinalang.net.netty.contract.Constants.IDLE_STATE_HANDLER);
        senderReqRespStateManager.nettyTargetChannel.close();
        targetHandler.getOutboundRequestMsg()
                .setIoException(new IOException(
                        org.ballerinalang.net.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY));
        httpResponseFuture.notifyHttpListener(
                new EndpointTimeOutException(channelID, org.ballerinalang.net.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY,
                                             HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));

        LOG.error("Error in HTTP client: {}", org.ballerinalang.net.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY);
    }

    private void writeOutboundRequestBody(HttpContent lastHttpContent) {
        ChannelFuture outboundRequestChannelFuture =
                senderReqRespStateManager.nettyTargetChannel.writeAndFlush(lastHttpContent);
        checkForRequestWriteStatus(outboundRequestChannelFuture);
    }

    private void checkForRequestWriteStatus(ChannelFuture outboundRequestChannelFuture) {
        outboundRequestChannelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(
                            org.ballerinalang.net.netty.contract.Constants.CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED);
                }
                httpInboundResponseFuture.notifyHttpListener(throwable);
            } else {
                senderReqRespStateManager.state = new RequestCompleted(senderReqRespStateManager);
            }
        });
    }
}
