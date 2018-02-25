/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.sender.http2;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code UpgradeRequestHandler} is responsible for issuing the upgrade request
 */
public class UpgradeRequestHandler extends ChannelDuplexHandler {

    private TargetChannel targetChannel;

    /* Lock for synchronizing access */
    private Lock lock = new ReentrantLock();

    /* Outbound handler to be engaged after the upgrade */
    private ClientOutboundHandler http2ClientOutboundHandler;
    private SenderConfiguration senderConfiguration;

    public UpgradeRequestHandler(SenderConfiguration senderConfiguration, ClientOutboundHandler clientHandler) {
        this.senderConfiguration = senderConfiguration;
        this.http2ClientOutboundHandler = clientHandler;
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof OutboundMsgHolder) {
            OutboundMsgHolder outboundMsgHolder = (OutboundMsgHolder) msg;
            lock.lock();            // Locking is required to prevent two requests issuing the upgrade
            try {
                TargetChannel.UpgradeState state = targetChannel.getUpgradeState();
                if (state == TargetChannel.UpgradeState.UPGRADE_NOT_ISSUED) {
                    targetChannel.updateUpgradeState(TargetChannel.UpgradeState.UPGRADE_ISSUED);
                    new UpgradeRequestWriter(outboundMsgHolder).writeContent(ctx);
                } else if (state == TargetChannel.UpgradeState.UPGRADE_ISSUED) {
                    // if upgrade request is already issued, we need to queue the subsequent messages
                    targetChannel.addPendingMessage(outboundMsgHolder);
                } else {
                    ctx.write(msg, promise);
                }
            } finally {
                lock.unlock();
            }
        } else {
            ctx.write(msg, promise);
        }
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);

        if (evt instanceof HttpClientUpgradeHandler.UpgradeEvent) {
            HttpClientUpgradeHandler.UpgradeEvent upgradeEvent = (HttpClientUpgradeHandler.UpgradeEvent) evt;
            if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_SUCCESSFUL.name().equals(upgradeEvent.name())) {
                lock.lock();
                try {
                    targetChannel.updateUpgradeState(TargetChannel.UpgradeState.UPGRADED);
                    ctx.pipeline().remove(this);
                    ctx.pipeline().addLast(http2ClientOutboundHandler);
                } finally {
                    lock.unlock();
                }
                flushPendingMessages(ctx, ctx.newPromise());
            } else if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_REJECTED.name().equals(upgradeEvent.name())) {
                // If upgrade fails, notify the listener and continue with the queued requests
                lock.lock();
                try {
                    targetChannel.updateUpgradeState(TargetChannel.UpgradeState.UPGRADE_NOT_ISSUED);
                } finally {
                    lock.unlock();
                }
                targetChannel.getInFlightMessage(1).getResponseFuture().notifyHttpListener(
                        new Exception("HTTP/2 Upgrade failed"));
                tryNextMessage(ctx, ctx.newPromise());
            }
        }
    }

    /* Flush the messages queued during the upgrading process */
    private void flushPendingMessages(ChannelHandlerContext ctx, ChannelPromise promise) {

        targetChannel.getPendingMessages().forEach(message -> {
            ctx.write(message, promise);
        });
        targetChannel.getPendingMessages().clear();
    }

    /* Try the updrade with the next queued message if the initial upgrade fail */
    private void tryNextMessage(ChannelHandlerContext ctx, ChannelPromise promise) {
        OutboundMsgHolder nextMessage = targetChannel.getPendingMessages().poll();
        if (nextMessage != null) {
            ctx.write(nextMessage, promise);
        }
    }

    /* Responsible for writing initial upgrade request in HTTP/1.1. Writing logic is very much same as
    {@code TargetChannel} of http client, we may refactor this later to prevent duplicate logic */
    private class UpgradeRequestWriter {

        private final Logger log = LoggerFactory.getLogger(UpgradeRequestWriter.class);
        int contentLength = 0;
        private List<HttpContent> contentList = new ArrayList<>();
        private String httpVersion = senderConfiguration.getHttpVersion();
        private ChunkConfig chunkConfig = senderConfiguration.getChunkingConfig();
        private HTTPCarbonMessage outboundRequest;
        private HttpResponseFuture responseFuture;
        private OutboundMsgHolder outboundMsgHolder;
        private boolean isRequestWritten = false;

        public UpgradeRequestWriter(OutboundMsgHolder outboundMsgHolder) {
            this.outboundMsgHolder = outboundMsgHolder;
            outboundRequest = outboundMsgHolder.getRequest();
            responseFuture = outboundMsgHolder.getResponseFuture();
        }

        public void writeContent(ChannelHandlerContext ctx) {

            // Response for the upgrade request will arrive in stream 1, so use 1 as the stream id
            targetChannel.putInFlightMessage(1, outboundMsgHolder);

            outboundRequest.getHttpContentAsync().
                    setMessageListener((httpContent -> targetChannel.getChannel().eventLoop().execute(() -> {
                        try {
                            writeOutboundRequest(ctx, httpContent);
                        } catch (Exception exception) {
                            String errorMsg = "Failed to send the request : "
                                              + exception.getMessage().toLowerCase(Locale.ENGLISH);
                            log.error(errorMsg, exception);
                            responseFuture.notifyHttpListener(exception);
                        }
                    })));
        }

        private void writeOutboundRequest(ChannelHandlerContext ctx, HttpContent httpContent) throws Exception {
            if (Util.isLastHttpContent(httpContent)) {
                if (!isRequestWritten) {
                    if (Util.isEntityBodyAllowed(getHttpMethod(outboundRequest))) {
                        if (chunkConfig == ChunkConfig.ALWAYS && Util.isVersionCompatibleForChunking(httpVersion)) {
                            Util.setupChunkedRequest(outboundRequest);
                        } else {
                            contentLength += httpContent.content().readableBytes();
                            Util.setupContentLengthRequest(outboundRequest, contentLength);
                        }
                    }
                    writeOutboundRequestHeaders(outboundRequest);
                }

                writeOutboundRequestBody(ctx, httpContent);
            } else {
                if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO)
                    && Util.isVersionCompatibleForChunking(httpVersion)) {
                    if (!isRequestWritten) {
                        Util.setupChunkedRequest(outboundRequest);
                        writeOutboundRequestHeaders(outboundRequest);
                    }
                    ChannelFuture outboundRequestChannelFuture = ctx.channel().writeAndFlush(httpContent);
                    notifyIfFailure(outboundRequestChannelFuture);
                } else {
                    contentList.add(httpContent);
                    contentLength += httpContent.content().readableBytes();
                }
            }
        }

        private void writeOutboundRequestHeaders(HTTPCarbonMessage httpOutboundRequest) {
            httpOutboundRequest.setProperty(Constants.HTTP_VERSION, httpVersion);
            HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);
            isRequestWritten = true;
            targetChannel.getChannel().write(httpRequest);
        }

        private void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent lastHttpContent) {
            if (chunkConfig == ChunkConfig.NEVER || !Util.isVersionCompatibleForChunking(httpVersion)) {
                for (HttpContent cachedHttpContent : contentList) {
                    ChannelFuture outboundRequestChannelFuture = ctx.channel().writeAndFlush(cachedHttpContent);
                    notifyIfFailure(outboundRequestChannelFuture);
                }
            }
            ChannelFuture outboundRequestChannelFuture = targetChannel.getChannel().writeAndFlush(lastHttpContent);
            notifyIfFailure(outboundRequestChannelFuture);
        }

        private void notifyIfFailure(ChannelFuture outboundRequestChannelFuture) {
            outboundRequestChannelFuture.addListener(writeOperationPromise -> {
                if (writeOperationPromise.cause() != null) {
                    Throwable throwable = writeOperationPromise.cause();
                    if (throwable instanceof ClosedChannelException) {
                        throwable = new IOException(Constants.REMOTE_SERVER_ABRUPTLY_CLOSE_REQUEST_CONNECTION);
                    }
                    log.error(Constants.REMOTE_SERVER_ABRUPTLY_CLOSE_REQUEST_CONNECTION, throwable);
                    responseFuture.notifyHttpListener(throwable);
                }
            });
        }

        private String getHttpMethod(HTTPCarbonMessage httpOutboundRequest) throws Exception {
            String httpMethod = (String) httpOutboundRequest.getProperty(Constants.HTTP_METHOD);
            if (httpMethod == null) {
                throw new Exception("Couldn't get the HTTP method from the outbound request");
            }
            return httpMethod;
        }
    }

}
