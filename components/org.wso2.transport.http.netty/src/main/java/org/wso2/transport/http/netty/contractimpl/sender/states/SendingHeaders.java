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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contractimpl.common.states.MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;

import static org.wso2.transport.http.netty.contract.Constants
        .IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants.INBOUND_RESPONSE_ALREADY_RECEIVED;
import static org.wso2.transport.http.netty.contract.Constants
        .REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contractimpl.common.Util.isEntityBodyAllowed;
import static org.wso2.transport.http.netty.contractimpl.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.contractimpl.common.Util.setupChunkedRequest;
import static org.wso2.transport.http.netty.contractimpl.common.Util.setupContentLengthRequest;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.checkChunkingCompatibility;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.writeRequestHeaders;

/**
 * State between start and end of outbound request header write.
 */
public class SendingHeaders implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingHeaders.class);

    private final String httpVersion;
    private final ChunkConfig chunkConfig;
    private final TargetChannel targetChannel;
    private final MessageStateContext messageStateContext;
    private final HttpResponseFuture httpInboundResponseFuture;

    public SendingHeaders(MessageStateContext messageStateContext, TargetChannel targetChannel, String httpVersion,
                          ChunkConfig chunkConfig, HttpResponseFuture httpInboundResponseFuture) {
        this.messageStateContext = messageStateContext;
        this.targetChannel = targetChannel;
        this.httpVersion = httpVersion;
        this.chunkConfig = chunkConfig;
        this.httpInboundResponseFuture = httpInboundResponseFuture;
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        if (isLastHttpContent(httpContent)) {
            if (isEntityBodyAllowed(getHttpMethod(httpOutboundRequest))) {
                if (chunkConfig == ChunkConfig.ALWAYS && checkChunkingCompatibility(httpVersion, chunkConfig)) {
                    setupChunkedRequest(httpOutboundRequest);
                } else {
                    long contentLength = httpContent.content().readableBytes();
                    setupContentLengthRequest(httpOutboundRequest, contentLength);
                }
            }
            writeRequestHeaders(httpOutboundRequest, httpInboundResponseFuture, httpVersion, targetChannel);
            writeResponse(httpOutboundRequest, httpContent, true);
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) &&
                    checkChunkingCompatibility(httpVersion, chunkConfig)) {
                setupChunkedRequest(httpOutboundRequest);
                writeRequestHeaders(httpOutboundRequest, httpInboundResponseFuture, httpVersion, targetChannel);
                writeResponse(httpOutboundRequest, httpContent, true);
                return;
            }
            writeResponse(httpOutboundRequest, httpContent, false);
        }
    }

    @Override
    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        writeOutboundRequestHeaders(httpOutboundRequest, httpContent);
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        // If this method is called, it is an application error. Inbound response is receiving before the completion
        // of request header write.
        targetHandler.getOutboundRequestMsg().setIoException(new IOException(INBOUND_RESPONSE_ALREADY_RECEIVED));
        messageStateContext.setSenderState(new ReceivingHeaders(messageStateContext));
        messageStateContext.getSenderState().readInboundResponseHeaders(targetHandler, httpInboundResponse);
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {
        LOG.warn("readInboundResponseEntityBody {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(HttpResponseFuture httpResponseFuture) {
        // HttpResponseFuture will be notified asynchronously via writeOutboundRequestHeaders method.
        LOG.error(REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(HttpResponseFuture httpResponseFuture, String channelID) {
        // HttpResponseFuture will be notified asynchronously via writeOutboundRequestHeaders method.
        LOG.error("Error in HTTP client: {}", IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS);
    }

    private String getHttpMethod(HttpCarbonMessage httpOutboundRequest) {
        String httpMethod = (String) httpOutboundRequest.getProperty(Constants.HTTP_METHOD);
        if (httpMethod == null) {
            throw new IllegalArgumentException("Couldn't get the HTTP method from the outbound request");
        }
        return httpMethod;
    }

    private void writeResponse(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, boolean headersWritten) {
        messageStateContext.setSenderState(new SendingEntityBody(messageStateContext, targetChannel, headersWritten,
                                                                 httpInboundResponseFuture));
        messageStateContext.getSenderState().writeOutboundRequestEntity(outboundResponseMsg, httpContent);
    }
}
