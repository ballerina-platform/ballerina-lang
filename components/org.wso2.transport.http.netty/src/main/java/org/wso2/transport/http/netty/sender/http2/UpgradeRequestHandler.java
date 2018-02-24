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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
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

    private ClientOutboundHandler clientHandler;

    public UpgradeRequestHandler(ClientOutboundHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof OutboundMsgHolder) {
            OutboundMsgHolder outboundMsgHolder = (OutboundMsgHolder) msg;

            lock.lock();
            try {
                TargetChannel.UpgradeState state = targetChannel.getUpgradeState();
                if (state == TargetChannel.UpgradeState.UPGRADE_NOT_ISSUED) {
                    targetChannel.updateUpgradeState(TargetChannel.UpgradeState.UPGRADE_ISSUED);
                    new UpgradeRequestWriter(outboundMsgHolder).writeContent(ctx);
                } else if (state == TargetChannel.UpgradeState.UPGRADED) {
                    ctx.write(msg, promise);
                } else if (state == TargetChannel.UpgradeState.UPGRADE_ISSUED) {
                    targetChannel.addPendingMessage(outboundMsgHolder);
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
                    ctx.pipeline().addLast(clientHandler);
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

    private void flushPendingMessages(ChannelHandlerContext ctx, ChannelPromise promise) {

        targetChannel.getPendingMessages().forEach(message -> {
            ctx.write(message, promise);
        });
        targetChannel.getPendingMessages().clear();
    }

    private void tryNextMessage(ChannelHandlerContext ctx, ChannelPromise promise) {
        OutboundMsgHolder nextMessage = targetChannel.getPendingMessages().poll();
        if (nextMessage != null) {
            ctx.write(nextMessage, promise);
        }
    }

    private class UpgradeRequestWriter {

        private final Log log = LogFactory.getLog(UpgradeRequestWriter.class);
        List<HttpContent> contentList = new ArrayList<>();
        int contentLength = 0;
        boolean isRequestWritten = false;
        String httpVersion = "1.1";
        ChunkConfig chunkConfig = ChunkConfig.AUTO;
        HTTPCarbonMessage httpOutboundRequest;
        HttpResponseFuture responseFuture;
        OutboundMsgHolder outboundMsgHolder;

        public UpgradeRequestWriter(OutboundMsgHolder outboundMsgHolder) {
            this.outboundMsgHolder = outboundMsgHolder;
            httpOutboundRequest = outboundMsgHolder.getRequest();
            responseFuture = outboundMsgHolder.getResponseFuture();
        }

        public void writeContent(ChannelHandlerContext ctx) {

            targetChannel.putInFlightMessage(1, outboundMsgHolder);

            httpOutboundRequest.getHttpContentAsync().
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
                if (!this.isRequestWritten) {
                    if (Util.isEntityBodyAllowed(getHttpMethod(httpOutboundRequest))) {
                        if (chunkConfig == ChunkConfig.ALWAYS && Util.isVersionCompatibleForChunking(httpVersion)) {
                            Util.setupChunkedRequest(httpOutboundRequest);
                        } else {
                            contentLength += httpContent.content().readableBytes();
                            Util.setupContentLengthRequest(httpOutboundRequest, contentLength);
                        }
                    }
                    writeOutboundRequestHeaders(httpOutboundRequest);
                }

                writeOutboundRequestBody(ctx, httpContent);
            } else {
                if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO)
                    && Util.isVersionCompatibleForChunking(httpVersion)) {
                    if (!this.isRequestWritten) {
                        Util.setupChunkedRequest(httpOutboundRequest);
                        writeOutboundRequestHeaders(httpOutboundRequest);
                    }
                    ChannelFuture outboundRequestChannelFuture = ctx.channel().writeAndFlush(httpContent);
                    notifyIfFailure(outboundRequestChannelFuture);
                } else {
                    this.contentList.add(httpContent);
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
