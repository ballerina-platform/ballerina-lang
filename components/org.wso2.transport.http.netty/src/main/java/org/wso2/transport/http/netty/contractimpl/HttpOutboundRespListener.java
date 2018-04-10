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

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.listener.RequestDataHolder;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.wso2.transport.http.netty.common.Constants.CHUNKING_CONFIG;

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
    private KeepAliveConfig keepAliveConfig;
    private boolean headerWritten = false;
    private int contentLength = 0;
    private String serverName;
    private List<HttpContent> contentList = new ArrayList<>();

    public HttpOutboundRespListener(ChannelHandlerContext channelHandlerContext, HTTPCarbonMessage requestMsg,
                                    ChunkConfig chunkConfig,
                                    KeepAliveConfig keepAliveConfig,
                                    String serverName) {
        this.sourceContext = channelHandlerContext;
        this.requestDataHolder = new RequestDataHolder(requestMsg);
        this.inboundRequestMsg = requestMsg;
        this.keepAliveConfig = keepAliveConfig;
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

    @Override
    public void onPushPromise(Http2PushPromise pushPromise) {
        inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(new UnsupportedOperationException(
                "Sending a PUSH_PROMISE is not supported for HTTP/1.x connections"));
    }

    @Override
    public void onPushResponse(int promiseId, HTTPCarbonMessage httpMessage) {
        inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(new UnsupportedOperationException(
                "Sending Server Push messages is not supported for HTTP/1.x connections"));
    }

    private void writeOutboundResponse(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive,
            HttpContent httpContent) {
        ChannelFuture outboundChannelFuture;
        ChunkConfig responseChunkConfig = outboundResponseMsg.getProperty(CHUNKING_CONFIG) != null ?
                (ChunkConfig) outboundResponseMsg.getProperty(CHUNKING_CONFIG) : null;
        if (responseChunkConfig != null) {
            this.setChunkConfig(responseChunkConfig);
        }

        if (Util.isLastHttpContent(httpContent)) {
            if (!headerWritten) {
                if (chunkConfig == ChunkConfig.ALWAYS
                        && Util.isVersionCompatibleForChunking(requestDataHolder.getHttpVersion())) {
                    Util.setupChunkedRequest(outboundResponseMsg);
                    writeOutboundResponseHeaders(outboundResponseMsg, keepAlive);
                    outboundChannelFuture = writeOutboundResponseBody(httpContent);
                } else {
                    contentLength += httpContent.content().readableBytes();
                    Util.setupContentLengthRequest(outboundResponseMsg, contentLength);
                    outboundChannelFuture = writeOutboundResponseHeaderAndBody(outboundResponseMsg,
                            (LastHttpContent) httpContent, keepAlive);
                }
            } else {
                outboundChannelFuture = writeOutboundResponseBody(httpContent);
            }

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
                if (!headerWritten) {
                    Util.setupChunkedRequest(outboundResponseMsg);
                    writeOutboundResponseHeaders(outboundResponseMsg, keepAlive);
                }
                HttpResponseFuture outboundRespStatusFuture =
                        inboundRequestMsg.getHttpOutboundRespStatusFuture();
                ChannelFuture outboundResponseChannelFuture = sourceContext.writeAndFlush(httpContent);
                Util.addResponseWriteFailureListener(outboundRespStatusFuture, outboundResponseChannelFuture);
            } else {
                this.contentList.add(httpContent);
                contentLength += httpContent.content().readableBytes();
            }
        }
    }

    private ChannelFuture writeOutboundResponseHeaderAndBody(HTTPCarbonMessage outboundResponseMsg,
            LastHttpContent lastHttpContent, boolean keepAlive) {
        HttpResponseFuture outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();

        CompositeByteBuf allContent = Unpooled.compositeBuffer();
        for (HttpContent cachedHttpContent : contentList) {
            allContent.addComponent(true, cachedHttpContent.content());
        }
        allContent.addComponent(true, lastHttpContent.content());

        HttpResponse fullOutboundResponse =
                Util.createFullHttpResponse(outboundResponseMsg, requestDataHolder.getHttpVersion(),
                serverName, keepAlive, allContent);

        headerWritten = true;
        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(fullOutboundResponse);
        Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private ChannelFuture writeOutboundResponseBody(HttpContent lastHttpContent) {
        HttpResponseFuture outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(lastHttpContent);
        Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private void resetState(HTTPCarbonMessage outboundResponseMsg) {
        outboundResponseMsg.removeHttpContentAsyncFuture();
        contentList.clear();
        contentLength = 0;
        headerWritten = false;
    }

    // Decides whether to close the connection after sending the response
    private boolean isKeepAlive() {
        if (keepAliveConfig == null || keepAliveConfig == KeepAliveConfig.AUTO) {
            String requestConnectionHeader = requestDataHolder.getConnectionHeaderValue();
            if (Float.valueOf(requestDataHolder.getHttpVersion()) <= Constants.HTTP_1_0) {
                return requestConnectionHeader != null && requestConnectionHeader
                        .equalsIgnoreCase(Constants.CONNECTION_KEEP_ALIVE);
            } else {
                return requestConnectionHeader == null || !requestConnectionHeader
                        .equalsIgnoreCase(Constants.CONNECTION_CLOSE);
            }
        } else {
            return keepAliveConfig == KeepAliveConfig.ALWAYS;
        }
    }

    private void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive) {
        HttpResponse response = Util.createHttpResponse(outboundResponseMsg, requestDataHolder.getHttpVersion(),
                serverName, keepAlive);
        headerWritten = true;
        sourceContext.write(response);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Couldn't send the outbound response", throwable);
    }

    public ChunkConfig getChunkConfig() {
        return chunkConfig;
    }

    public void setChunkConfig(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }
}
