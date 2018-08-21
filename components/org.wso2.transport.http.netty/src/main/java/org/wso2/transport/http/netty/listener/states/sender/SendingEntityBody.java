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
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.sender.TargetHandler;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.transport.http.netty.common.Constants.CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Util.isLastHttpContent;

/**
 * State between start and end of outbound response entity body write
 */
public class SendingEntityBody implements SenderState {

    private static Logger log = LoggerFactory.getLogger(SendingEntityBody.class);
    private final StateContext stateContext;
    private final boolean headersWritten;
    private final HandlerExecutor handlerExecutor;
    private final ChunkConfig chunkConfig;
    private final TargetChannel targetChannel;
    private final HttpResponseFuture httpInboundResponseFuture;
    private final String httpVersion;
    private long contentLength = 0;
    private List<HttpContent> contentList = new ArrayList<>();

    public SendingEntityBody(StateContext stateContext, ChunkConfig chunkConfig,
                             TargetChannel targetChannel, boolean headersWritten,
                             HttpResponseFuture httpInboundResponseFuture) {
        this.stateContext = stateContext;
        this.chunkConfig = chunkConfig;
        this.targetChannel = targetChannel;
        this.headersWritten = headersWritten;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.httpInboundResponseFuture = httpInboundResponseFuture;
        this.httpVersion = targetChannel.getHttpVersion();
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {

    }

    @Override
    public void writeOutboundRequestEntityBody(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        if (isLastHttpContent(httpContent)) {
            if (!headersWritten) {
                contentLength += httpContent.content().readableBytes();
                Util.setupContentLengthRequest(httpOutboundRequest, contentLength);
                writeHeaders(httpOutboundRequest);
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

    private void writeHeaders(HttpCarbonMessage httpOutboundRequest) {
        setHttpVersionProperty(httpOutboundRequest);
        HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);
        targetChannel.setRequestHeaderWritten(true);
        ChannelFuture outboundHeaderFuture = this.targetChannel.getChannel().write(httpRequest);
        notifyIfHeaderFailure(outboundHeaderFuture);
    }

    private void notifyIfHeaderFailure(ChannelFuture outboundRequestChannelFuture) {
        outboundRequestChannelFuture.addListener(writeOperationPromise -> {
            if (writeOperationPromise.cause() != null) {
                notifyResponseFutureListener(writeOperationPromise);
            }
        });
    }

    private void setHttpVersionProperty(HttpCarbonMessage httpOutboundRequest) {
        if (Float.valueOf(httpVersion) == Constants.HTTP_2_0) {
            // Upgrade request of HTTP/2 should be a HTTP/1.1 request
            httpOutboundRequest.setProperty(Constants.HTTP_VERSION, String.valueOf(Constants.HTTP_1_1));
        } else {
            httpOutboundRequest.setProperty(Constants.HTTP_VERSION, httpVersion);
        }
    }

    private void writeOutboundRequestBody(HttpContent lastHttpContent) {
        ChannelFuture outboundRequestChannelFuture = targetChannel.getChannel().writeAndFlush(lastHttpContent);
        checkForRequestWriteStatus(outboundRequestChannelFuture);
    }

    private void checkForRequestWriteStatus(ChannelFuture outboundRequestChannelFuture) {
        outboundRequestChannelFuture.addListener(writeOperationPromise -> {
            if (writeOperationPromise.cause() != null) {
                notifyResponseFutureListener(writeOperationPromise);
            } else {
                stateContext.setSenderState(new RequestCompleted(stateContext));
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
}
