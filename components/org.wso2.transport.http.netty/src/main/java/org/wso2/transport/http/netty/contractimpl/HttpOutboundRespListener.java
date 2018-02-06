/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.contractimpl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.listener.RequestDataHolder;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Get executed when the response is available.
 */
public class HttpOutboundRespListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(HttpOutboundRespListener.class);

    private ChannelHandlerContext sourceContext;
    private RequestDataHolder requestDataHolder;
    private HandlerExecutor handlerExecutor;
    private HTTPCarbonMessage inboundRequestMsg;
    private ChunkConfig chunkConfig;
    private boolean isHeaderWritten = false;
    private int contentLength = 0;
    private String serverName;
    private List<HttpContent> contentList = new ArrayList<>();

    public HttpOutboundRespListener(ChannelHandlerContext channelHandlerContext, HTTPCarbonMessage requestMsg,
            ChunkConfig chunkConfig, String serverName) {
        this.sourceContext = channelHandlerContext;
        this.requestDataHolder = new RequestDataHolder(requestMsg);
        this.inboundRequestMsg = requestMsg;
        this.handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        this.chunkConfig = chunkConfig;
        this.serverName = serverName;
    }

    @Override
    public void onMessage(HTTPCarbonMessage outboundResponseMsg) {
        sourceContext.channel().eventLoop().execute(() -> {

            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceResponseReceiving(outboundResponseMsg);
            }

            boolean keepAlive = isKeepAlive();

            outboundResponseMsg.getHttpContentAsync().setMessageListener(httpContent ->
                    this.sourceContext.channel().eventLoop().execute(() -> {
                        try {
                            writeOutboundResponse(outboundResponseMsg, keepAlive, httpContent);
                        } catch (Exception exception) {
                            String errorMsg = "Failed to send the outbound response : "
                                    + exception.getMessage().toLowerCase(Locale.ENGLISH);
                            log.error(errorMsg, exception);
                            inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(exception);
                        }
                    }));
        });
    }

    private void writeOutboundResponse(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive,
            HttpContent httpContent) {
        if (Util.isLastHttpContent(httpContent)) {
            if (!isHeaderWritten) {
                if ((chunkConfig == ChunkConfig.ALWAYS)
                        && Util.isVersionCompatibleForChunking(requestDataHolder.getHttpVersion())) {
                    Util.setupChunkedRequest(outboundResponseMsg);
                } else {
                    contentLength += httpContent.content().readableBytes();
                    Util.setupContentLengthRequest(outboundResponseMsg, contentLength);
                }
                writeOutboundResponseHeaders(outboundResponseMsg, keepAlive);
            }

            ChannelFuture outboundChannelFuture = writeOutboundResponseBody(httpContent);

            if (!keepAlive) {
                outboundChannelFuture.addListener(ChannelFutureListener.CLOSE);
            }
            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceResponseSending(outboundResponseMsg);
            }
            resetState(outboundResponseMsg);
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO)
                    && Util.isVersionCompatibleForChunking(requestDataHolder.getHttpVersion())) {
                if (!isHeaderWritten) {
                    Util.setupChunkedRequest(outboundResponseMsg);
                    writeOutboundResponseHeaders(outboundResponseMsg, keepAlive);
                }
                HttpResponseFuture outboundRespStatusFuture =
                        inboundRequestMsg.getHttpOutboundRespStatusFuture();
                ChannelFuture outboundResponseChannelFuture = sourceContext.writeAndFlush(httpContent);
                notifyIfFailure(outboundRespStatusFuture, outboundResponseChannelFuture);
            } else {
                this.contentList.add(httpContent);
                contentLength += httpContent.content().readableBytes();
            }
        }
    }

    private ChannelFuture writeOutboundResponseBody(HttpContent lastHttpContent) {
        HttpResponseFuture outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        if (chunkConfig == ChunkConfig.NEVER ||
                !Util.isVersionCompatibleForChunking(requestDataHolder.getHttpVersion())) {
            for (HttpContent cachedHttpContent : contentList) {
                ChannelFuture outboundResponseChannelFuture = sourceContext.writeAndFlush(cachedHttpContent);
                notifyIfFailure(outboundRespStatusFuture, outboundResponseChannelFuture);
            }
        }
        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(lastHttpContent);
        outboundChannelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_RESPONSE_CONNECTION);
                }
                log.error(Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_RESPONSE_CONNECTION, throwable);
                outboundRespStatusFuture.notifyHttpListener(throwable);
            } else {
                outboundRespStatusFuture.notifyHttpListener(inboundRequestMsg);
            }
        });
        return outboundChannelFuture;
    }

    private void notifyIfFailure(HttpResponseFuture outboundRespStatusFuture,
            ChannelFuture outboundResponseChannelFuture) {
        outboundResponseChannelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_RESPONSE_CONNECTION);
                }
                log.error(Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_RESPONSE_CONNECTION, throwable);
                outboundRespStatusFuture.notifyHttpListener(throwable);
            }
        });
    }

    private void resetState(HTTPCarbonMessage outboundResponseMsg) {
        outboundResponseMsg.removeHttpContentAsyncFuture();
        contentList.clear();
        contentLength = 0;
    }

    // Decides whether to close the connection after sending the response
    private boolean isKeepAlive() {
        String requestConnectionHeader = requestDataHolder.getConnectionHeaderValue();

        if (Float.valueOf(requestDataHolder.getHttpVersion()) <= Constants.HTTP_1_0) {
            return requestConnectionHeader != null && requestConnectionHeader
                    .equalsIgnoreCase(Constants.CONNECTION_KEEP_ALIVE);
        } else {
            return requestConnectionHeader == null || !requestConnectionHeader
                    .equalsIgnoreCase(Constants.CONNECTION_CLOSE);
        }
    }

    private void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive) {
        HttpResponse response = Util.createHttpResponse(outboundResponseMsg, requestDataHolder.getHttpVersion(),
                serverName, keepAlive);
        isHeaderWritten = true;
        sourceContext.write(response);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Couldn't send the outbound response", throwable);
    }
}
