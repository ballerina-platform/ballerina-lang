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
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.SourceInteractiveState;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.listener.RequestDataHolder;
import org.wso2.transport.http.netty.listener.SourceErrorHandler;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.transport.http.netty.common.Constants.CHUNKING_CONFIG;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.RESPONSE_100_CONTINUE_SENT;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.SENDING_ENTITY_BODY;
import static org.wso2.transport.http.netty.common.Util.createFullHttpResponse;
import static org.wso2.transport.http.netty.common.Util.createHttpResponse;
import static org.wso2.transport.http.netty.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.common.Util.isVersionCompatibleForChunking;
import static org.wso2.transport.http.netty.common.Util.setupChunkedRequest;
import static org.wso2.transport.http.netty.common.Util.setupContentLengthRequest;
import static org.wso2.transport.http.netty.common.Util.shouldEnforceChunkingforHttpOneZero;

/**
 * Get executed when the response is available.
 */
public class HttpOutboundRespListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(HttpOutboundRespListener.class);
    private final SourceErrorHandler sourceErrorHandler;
    private final boolean headRequest;

    private ChannelHandlerContext sourceContext;
    private RequestDataHolder requestDataHolder;
    private HandlerExecutor handlerExecutor;
    private HTTPCarbonMessage inboundRequestMsg;
    private ChunkConfig chunkConfig;
    private KeepAliveConfig keepAliveConfig;
    private boolean headerWritten = false;
    private boolean continueRequest;
    private long contentLength = 0;
    private String serverName;
    private List<HttpContent> contentList = new ArrayList<>();
    private AtomicInteger writeCounter = new AtomicInteger(0);

    public HttpOutboundRespListener(ChannelHandlerContext channelHandlerContext, HTTPCarbonMessage requestMsg,
                                    ChunkConfig chunkConfig, KeepAliveConfig keepAliveConfig, String serverName,
                                    SourceErrorHandler sourceErrorHandler, boolean continueRequest) {
        this.sourceContext = channelHandlerContext;
        this.requestDataHolder = new RequestDataHolder(requestMsg);
        this.inboundRequestMsg = requestMsg;
        this.keepAliveConfig = keepAliveConfig;
        this.handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        this.chunkConfig = chunkConfig;
        this.serverName = serverName;
        this.sourceErrorHandler = sourceErrorHandler;
        this.headRequest = requestDataHolder.getHttpMethod().equalsIgnoreCase(Constants.HTTP_HEAD_METHOD);
        this.continueRequest = continueRequest;
    }

    @Override
    public void onMessage(HTTPCarbonMessage outboundResponseMsg) {
        sourceContext.channel().eventLoop().execute(() -> {

            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceResponseReceiving(outboundResponseMsg);
            }

            resetOutboundListenerState();

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
        ChunkConfig responseChunkConfig = outboundResponseMsg.getProperty(CHUNKING_CONFIG) != null ?
                (ChunkConfig) outboundResponseMsg.getProperty(CHUNKING_CONFIG) : null;
        if (responseChunkConfig != null) {
            this.setChunkConfig(responseChunkConfig);
        }
        if (continueRequest) {
            sourceErrorHandler.setState(RESPONSE_100_CONTINUE_SENT);
            continueRequest = false;
        } else {
            if (sourceErrorHandler.getState().equals(SourceInteractiveState.RECEIVING_ENTITY_BODY)) {
                // Response is being sent before finish reading the inbound request,
                // hence close the connection once the response is sent.
                keepAlive = false;
            }
            sourceErrorHandler.setState(SENDING_ENTITY_BODY);
        }

        ChannelFuture outboundChannelFuture;
        HttpResponseFuture outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        if (isLastHttpContent(httpContent)) {
            if (!headerWritten) {
                if (chunkConfig == ChunkConfig.ALWAYS && (
                        isVersionCompatibleForChunking(requestDataHolder.getHttpVersion()) ||
                                shouldEnforceChunkingforHttpOneZero(chunkConfig, requestDataHolder.getHttpVersion()))) {
                    writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);
                    outboundChannelFuture = checkHeadRequestAndWriteOutboundResponseBody(httpContent);

                } else {
                    contentLength += httpContent.content().readableBytes();
                    setupContentLengthRequest(outboundResponseMsg, contentLength);
                    outboundChannelFuture = writeOutboundResponseHeaderAndBody(outboundResponseMsg,
                            (LastHttpContent) httpContent, keepAlive);
                }
            } else {
                outboundChannelFuture = checkHeadRequestAndWriteOutboundResponseBody(httpContent);
            }

            if (!keepAlive) {
                outboundChannelFuture.addListener(ChannelFutureListener.CLOSE);
            }
            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceResponseSending(outboundResponseMsg);
            }
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) && (
                    isVersionCompatibleForChunking(requestDataHolder.getHttpVersion()) ||
                            shouldEnforceChunkingforHttpOneZero(chunkConfig, requestDataHolder.getHttpVersion()))) {
                if (!headerWritten) {
                    writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);
                }
                incrementWriteCount(writeCounter);

                if (headRequest) {
                    httpContent.release();
                    return;
                }

                ChannelFuture outboundResponseChannelFuture = sourceContext.writeAndFlush(httpContent);
                sourceErrorHandler.addResponseWriteFailureListener(outboundRespStatusFuture,
                                                                   outboundResponseChannelFuture, writeCounter);
            } else {
                this.contentList.add(httpContent);
                contentLength += httpContent.content().readableBytes();
            }
        }
    }

    private ChannelFuture checkHeadRequestAndWriteOutboundResponseBody(HttpContent httpContent) {
        ChannelFuture outboundChannelFuture;
        if (headRequest) {
            httpContent.release();
            outboundChannelFuture = writeOutboundResponseBody(new DefaultLastHttpContent());
        } else {
            outboundChannelFuture = writeOutboundResponseBody(httpContent);
        }
        return outboundChannelFuture;
    }

    private ChannelFuture writeOutboundResponseHeaderAndBody(HTTPCarbonMessage outboundResponseMsg,
                                                             LastHttpContent lastHttpContent, boolean keepAlive) {
        HttpResponseFuture outRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();

        CompositeByteBuf allContent = Unpooled.compositeBuffer();
        for (HttpContent cachedHttpContent : contentList) {
            allContent.addComponent(true, cachedHttpContent.content());
        }
        allContent.addComponent(true, lastHttpContent.content());

        if (headRequest) {
            allContent.release();
            allContent = Unpooled.compositeBuffer();
            allContent.addComponent(true, new DefaultLastHttpContent().content());
        }

        HttpResponse fullOutboundResponse =
                createFullHttpResponse(outboundResponseMsg, requestDataHolder.getHttpVersion(),
                serverName, keepAlive, allContent);

        headerWritten = true;
        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(fullOutboundResponse);
        sourceErrorHandler.checkForResponseWriteStatus(inboundRequestMsg, outRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private ChannelFuture writeOutboundResponseBody(HttpContent lastHttpContent) {
        HttpResponseFuture outRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        incrementWriteCount(writeCounter);
        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(lastHttpContent);
        sourceErrorHandler.checkForResponseWriteStatus(inboundRequestMsg, outRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private void writeHeaders(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive,
                              HttpResponseFuture outboundRespStatusFuture) {
        setupChunkedRequest(outboundResponseMsg);
        incrementWriteCount(writeCounter);
        ChannelFuture outboundHeaderFuture = writeOutboundResponseHeaders(outboundResponseMsg, keepAlive);
        sourceErrorHandler.addResponseWriteFailureListener(outboundRespStatusFuture, outboundHeaderFuture,
                                                           writeCounter);
    }

    private void resetOutboundListenerState() {
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

    private ChannelFuture writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive) {
        HttpResponse response = createHttpResponse(outboundResponseMsg, requestDataHolder.getHttpVersion(),
                serverName, keepAlive);
        headerWritten = true;
        return sourceContext.write(response);
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

    private int incrementWriteCount(AtomicInteger writeCounter) {
        return writeCounter.incrementAndGet();
    }
}
