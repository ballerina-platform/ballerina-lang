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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.exceptions.ClientConnectorException;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.transport.http.netty.contract.Constants.CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.contract.Constants
        .IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contract.Constants.INBOUND_RESPONSE_ALREADY_RECEIVED;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contractimpl.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.writeRequestHeaders;

/**
 * State between start and end of outbound request entity body write.
 */
public class SendingEntityBody implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingEntityBody.class);

    private final SenderReqRespStateManager senderReqRespStateManager;

    private final boolean headersWritten;
    private final HandlerExecutor handlerExecutor;
    private final TargetChannel targetChannel;
    private final HttpResponseFuture httpInboundResponseFuture;
    private final String httpVersion;
    private long contentLength = 0;
    private List<HttpContent> contentList = new ArrayList<>();

    SendingEntityBody(SenderReqRespStateManager senderReqRespStateManager, TargetChannel targetChannel,
                             boolean headersWritten, HttpResponseFuture httpInboundResponseFuture, String httpVersion) {
        this.senderReqRespStateManager = senderReqRespStateManager;
        this.targetChannel = targetChannel;
        this.headersWritten = headersWritten;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.httpInboundResponseFuture = httpInboundResponseFuture;
        this.httpVersion = httpVersion;
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        if (isLastHttpContent(httpContent)) {
            if (!headersWritten) {
                contentLength += httpContent.content().readableBytes();
                Util.setupContentLengthRequest(httpOutboundRequest, contentLength);
                writeRequestHeaders(httpOutboundRequest, httpInboundResponseFuture, httpVersion, targetChannel);
                for (HttpContent cachedHttpContent : contentList) {
                    this.targetChannel.getChannel().writeAndFlush(cachedHttpContent);
                }
            }
            writeOutboundRequestBody(httpContent);

            if (handlerExecutor != null) {
                handlerExecutor.executeAtTargetRequestSending(httpOutboundRequest);
            }

        } else {
            if (headersWritten) {
                this.targetChannel.getChannel().writeAndFlush(httpContent);
            } else {
                this.contentList.add(httpContent);
                contentLength += httpContent.content().readableBytes();
            }
        }
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        // If this method is called, it is an application error. Inbound response is receiving before the completion
        // of request body write.
        targetHandler.getOutboundRequestMsg().setIoException(new IOException(INBOUND_RESPONSE_ALREADY_RECEIVED));
        senderReqRespStateManager.senderState = new ReceivingHeaders(senderReqRespStateManager);
        senderReqRespStateManager.readInboundResponseHeaders(targetHandler, httpInboundResponse);
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {
        LOG.warn("readInboundResponseEntityBody {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(HttpResponseFuture httpResponseFuture) {
        httpResponseFuture
                .notifyHttpListener(new ClientConnectorException(targetChannel.getChannel().id().asShortText(),
                REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY));
        LOG.error("Error in HTTP client: {}", REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(HttpResponseFuture httpResponseFuture, String channelID) {
        httpResponseFuture.notifyHttpListener(
                new EndpointTimeOutException(channelID, IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY,
                                             HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
        LOG.error("Error in HTTP client: {}", IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY);
    }

    private void writeOutboundRequestBody(HttpContent lastHttpContent) {
        ChannelFuture outboundRequestChannelFuture = targetChannel.getChannel().writeAndFlush(lastHttpContent);
        checkForRequestWriteStatus(outboundRequestChannelFuture);
    }

    private void checkForRequestWriteStatus(ChannelFuture outboundRequestChannelFuture) {
        outboundRequestChannelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED);
                }
                httpInboundResponseFuture.notifyHttpListener(throwable);
            } else {
                senderReqRespStateManager.senderState = new RequestCompleted(senderReqRespStateManager);
            }
        });
    }
}
