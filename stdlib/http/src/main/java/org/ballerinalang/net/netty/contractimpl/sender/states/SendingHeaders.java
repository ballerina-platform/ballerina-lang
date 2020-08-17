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
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.timeout.IdleStateHandler;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contract.config.ChunkConfig;
import org.ballerinalang.net.netty.contractimpl.common.Util;
import org.ballerinalang.net.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.ballerinalang.net.netty.contractimpl.common.states.StateUtil;
import org.ballerinalang.net.netty.contractimpl.sender.TargetHandler;
import org.ballerinalang.net.netty.contractimpl.sender.channel.TargetChannel;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * State between start and end of outbound request header write.
 */
public class SendingHeaders implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingHeaders.class);

    private final String httpVersion;
    private final ChunkConfig chunkConfig;
    private final TargetChannel targetChannel;
    private final SenderReqRespStateManager senderReqRespStateManager;
    private final HttpResponseFuture httpInboundResponseFuture;

    private long contentLength = 0;
    private List<HttpContent> contentList = new ArrayList<>();

    public SendingHeaders(SenderReqRespStateManager senderReqRespStateManager,
                          TargetChannel targetChannel, String httpVersion,
                          ChunkConfig chunkConfig, HttpResponseFuture httpInboundResponseFuture) {
        this.senderReqRespStateManager = senderReqRespStateManager;
        this.targetChannel = targetChannel;
        this.httpVersion = httpVersion;
        this.chunkConfig = chunkConfig;
        this.httpInboundResponseFuture = httpInboundResponseFuture;
        configIdleTimeoutTrigger(senderReqRespStateManager.socketTimeout);
    }

    private void configIdleTimeoutTrigger(int socketIdleTimeout) {
        ChannelPipeline pipeline = senderReqRespStateManager.nettyTargetChannel.pipeline();
        IdleStateHandler idleStateHandler = new IdleStateHandler(0, 0, socketIdleTimeout, TimeUnit.MILLISECONDS);
        if (pipeline.get(Constants.TARGET_HANDLER) == null) {
            pipeline.addLast(Constants.IDLE_STATE_HANDLER, idleStateHandler);
        } else {
            pipeline.addBefore(Constants.TARGET_HANDLER,
                               Constants.IDLE_STATE_HANDLER, idleStateHandler);
        }
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest) {
        // We don't really do anything here because we only start sending headers when there is some content
        // to be written.
    }

    @Override
    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest,
                                           HttpContent httpContent) {
        if (Util.isLastHttpContent(httpContent)) {
            if (Util.checkContentLengthAndTransferEncodingHeaderAllowance(httpOutboundRequest)) {
                if (chunkConfig == ChunkConfig.ALWAYS && StateUtil
                        .checkChunkingCompatibility(httpVersion, chunkConfig)) {
                    Util.setupChunkedRequest(httpOutboundRequest);
                } else {
                    contentLength += httpContent.content().readableBytes();
                    Util.setupContentLengthRequest(httpOutboundRequest, contentLength);
                }
            }
            writeRequestHeaders(httpOutboundRequest, httpInboundResponseFuture, httpVersion, targetChannel);
            writeRequestBody(httpOutboundRequest, httpContent);
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) &&
                    StateUtil.checkChunkingCompatibility(httpVersion, chunkConfig)) {
                Util.setupChunkedRequest(httpOutboundRequest);
                writeRequestHeaders(httpOutboundRequest, httpInboundResponseFuture, httpVersion, targetChannel);
                writeRequestBody(httpOutboundRequest, httpContent);
                return;
            }
            waitForCompleteBody(httpContent);
        }
    }

    private void writeRequestHeaders(HttpCarbonMessage httpOutboundRequest,
                                     HttpResponseFuture httpInboundResponseFuture, String httpVersion,
                                     TargetChannel targetChannel) {
        setHttpVersionProperty(httpOutboundRequest, httpVersion);
        HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);
        targetChannel.setRequestHeaderWritten(true);
        ChannelFuture outboundHeaderFuture = senderReqRespStateManager.nettyTargetChannel.write(httpRequest);
        StateUtil.notifyIfHeaderWriteFailure(httpInboundResponseFuture, outboundHeaderFuture,
                                             Constants.CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED);
    }

    private void setHttpVersionProperty(HttpCarbonMessage httpOutboundRequest, String httpVersion) {
        if (Constants.HTTP_2_0.equals(httpVersion)) {
            // Upgrade request of HTTP/2 should be a HTTP/1.1 request
            httpOutboundRequest.setHttpVersion(String.valueOf(Constants.HTTP_1_1));
        } else {
            httpOutboundRequest.setHttpVersion(httpVersion);
        }
    }

    private void writeRequestBody(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        String expectHeader = httpOutboundRequest.getHeader(HttpHeaderNames.EXPECT.toString());
        if (expectHeader != null && expectHeader.equalsIgnoreCase(
                Constants.HEADER_VAL_100_CONTINUE)) {
            senderReqRespStateManager.state = new Sending100Continue(senderReqRespStateManager,
                                                                     httpInboundResponseFuture);
            senderReqRespStateManager.nettyTargetChannel.flush();
        } else {
            senderReqRespStateManager.state = new SendingEntityBody(senderReqRespStateManager,
                                                                    httpInboundResponseFuture);
        }

        for (HttpContent cachedHttpContent : contentList) {
            senderReqRespStateManager.writeOutboundRequestEntity(httpOutboundRequest, cachedHttpContent);
        }
        senderReqRespStateManager.writeOutboundRequestEntity(httpOutboundRequest, httpContent);
    }

    private void waitForCompleteBody(HttpContent httpContent) {
        contentList.add(httpContent);
        contentLength += httpContent.content().readableBytes();
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        // If this method is called, it is an application error. Inbound response is receiving before the completion
        // of request header write.
        targetHandler.getOutboundRequestMsg().setIoException(new IOException(
                Constants.INBOUND_RESPONSE_ALREADY_RECEIVED));
        senderReqRespStateManager.state = new ReceivingHeaders(senderReqRespStateManager);
        senderReqRespStateManager.readInboundResponseHeaders(targetHandler, httpInboundResponse);
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {
        LOG.warn("readInboundResponseEntityBody {}", StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(TargetHandler targetHandler, HttpResponseFuture httpResponseFuture) {
        // HttpResponseFuture will be notified asynchronously via writeOutboundRequestHeaders method.
        LOG.error(Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(TargetHandler targetHandler, HttpResponseFuture httpResponseFuture,
                                                   String channelID) {
        // HttpResponseFuture will be notified asynchronously via writeOutboundRequestHeaders method.
        senderReqRespStateManager.nettyTargetChannel.pipeline().remove(Constants.IDLE_STATE_HANDLER);
        senderReqRespStateManager.nettyTargetChannel.close();
        LOG.error("Error in HTTP client: {}", Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS);
    }
}
