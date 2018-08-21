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

package org.wso2.transport.http.netty.listener.states.sender;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.sender.TargetHandler;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import static org.wso2.transport.http.netty.common.Constants.CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Util.isEntityBodyAllowed;
import static org.wso2.transport.http.netty.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.common.Util.setupChunkedRequest;

/**
 * State between start and end of outbound response entity body write
 */
public class SendingHeaders implements SenderState {

    private static Logger log = LoggerFactory.getLogger(SendingHeaders.class);
    private final String httpVersion;
    private final ChunkConfig chunkConfig;
    private final TargetChannel targetChannel;
    private final StateContext stateContext;
    private final HttpResponseFuture httpInboundResponseFuture;

    public SendingHeaders(StateContext stateContext, TargetChannel targetChannel, String httpVersion,
                          ChunkConfig chunkConfig, HttpResponseFuture httpInboundResponseFuture) {
        this.stateContext = stateContext;
        this.targetChannel = targetChannel;
        this.httpVersion = httpVersion;
        this.chunkConfig = chunkConfig;
        this.httpInboundResponseFuture = httpInboundResponseFuture;
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent)
            throws Exception {
        if (isLastHttpContent(httpContent)) {
            if (isEntityBodyAllowed(getHttpMethod(httpOutboundRequest))) {
                if (chunkConfig == ChunkConfig.ALWAYS && checkChunkingCompatibility()) {
                    setupChunkedRequest(httpOutboundRequest);
                } else {
                    long contentLength = httpContent.content().readableBytes();
                    Util.setupContentLengthRequest(httpOutboundRequest, contentLength);
                }
            }
            writeHeaders(httpOutboundRequest);
            writeResponse(httpOutboundRequest, httpContent, true);
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) &&
                    checkChunkingCompatibility()) {
                setupChunkedRequest(httpOutboundRequest);
                writeHeaders(httpOutboundRequest);
                writeResponse(httpOutboundRequest, httpContent, true);
                return;
            }
            writeResponse(httpOutboundRequest, httpContent, false);
        }
    }

    @Override
    public void writeOutboundRequestEntityBody(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent)
            throws Exception {
        writeOutboundRequestHeaders(httpOutboundRequest, httpContent);
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler,
                                           HttpResponse httpInboundResponse) {

    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx,
                                              HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {

    }

    @Override
    public void handleAbruptChannelClosure(HttpResponseFuture httpResponseFuture) {
        // HttpResponseFuture will be notified asynchronously via Target channel.
        log.error(REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST);

    }

    @Override
    public void handleIdleTimeoutConnectionClosure(HttpResponseFuture httpResponseFuture, String channelID) {
        // HttpResponseFuture will be notified asynchronously via Target channel.
        log.error("Error in HTTP client: {}", IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST);
    }

    private boolean checkChunkingCompatibility() {
        return Util.isVersionCompatibleForChunking(httpVersion) || Util
                .shouldEnforceChunkingforHttpOneZero(chunkConfig, httpVersion);
    }

    private String getHttpMethod(HttpCarbonMessage httpOutboundRequest) throws Exception {
        String httpMethod = (String) httpOutboundRequest.getProperty(Constants.HTTP_METHOD);
        if (httpMethod == null) {
            throw new Exception("Couldn't get the HTTP method from the outbound request");
        }
        return httpMethod;
    }

    private void writeHeaders(HttpCarbonMessage httpOutboundRequest) {
        setHttpVersionProperty(httpOutboundRequest);
        HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);
        targetChannel.setRequestHeaderWritten(true);
        ChannelFuture outboundHeaderFuture = this.targetChannel.getChannel().write(httpRequest);
        notifyIfHeaderFailure(outboundHeaderFuture);
    }

    private void setHttpVersionProperty(HttpCarbonMessage httpOutboundRequest) {
        if (Float.valueOf(httpVersion) == Constants.HTTP_2_0) {
            // Upgrade request of HTTP/2 should be a HTTP/1.1 request
            httpOutboundRequest.setProperty(Constants.HTTP_VERSION, String.valueOf(Constants.HTTP_1_1));
        } else {
            httpOutboundRequest.setProperty(Constants.HTTP_VERSION, httpVersion);
        }
    }

    private void writeResponse(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, boolean headersWritten)
            throws Exception {
        stateContext.setSenderState(new SendingEntityBody(stateContext, chunkConfig, targetChannel,
                                                          headersWritten, httpInboundResponseFuture));
        stateContext.getSenderState().writeOutboundRequestEntityBody(outboundResponseMsg, httpContent);
    }

    private void notifyIfHeaderFailure(ChannelFuture outboundRequestChannelFuture) {
        outboundRequestChannelFuture.addListener(writeOperationPromise -> {
            if (writeOperationPromise.cause() != null) {
                notifyResponseFutureListener(writeOperationPromise);
            }
        });
    }

    private void notifyResponseFutureListener(Future<? super Void> writeOperationPromise) {
        Throwable throwable = writeOperationPromise.cause();
        if (throwable instanceof ClosedChannelException) {
            throwable = new IOException(CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED);
        }
        httpInboundResponseFuture.notifyHttpListener(throwable);
    }
}
