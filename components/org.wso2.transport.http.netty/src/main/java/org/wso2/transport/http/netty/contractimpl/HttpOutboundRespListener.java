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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.listener.RequestDataHolder;
import org.wso2.transport.http.netty.listener.SourceErrorHandler;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.listener.states.ListenerStateContext;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Get executed when the response is available.
 */
public class HttpOutboundRespListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(HttpOutboundRespListener.class);
    private final SourceErrorHandler sourceErrorHandler;
    private final boolean headRequest;
    private final SourceHandler sourceHandler;
    private final ListenerStateContext stateContext;

    private ChannelHandlerContext sourceContext;
    private RequestDataHolder requestDataHolder;
    private HandlerExecutor handlerExecutor;
    private HttpCarbonMessage inboundRequestMsg;
    private ChunkConfig chunkConfig;
    private KeepAliveConfig keepAliveConfig;
    private boolean headerWritten = false;
    private boolean continueRequest;
    private long contentLength = 0;
    private String serverName;
    private List<HttpContent> contentList = new ArrayList<>();
    private AtomicInteger writeCounter = new AtomicInteger(0);

    public HttpOutboundRespListener(HttpCarbonMessage requestMsg, SourceHandler sourceHandler,
                                    boolean continueRequest) {
        this.requestDataHolder = new RequestDataHolder(requestMsg);
        this.inboundRequestMsg = requestMsg;
        this.sourceHandler = sourceHandler;
        this.sourceContext = sourceHandler.getInboundChannelContext();
        this.chunkConfig = sourceHandler.getChunkConfig();
        this.keepAliveConfig = sourceHandler.getKeepAliveConfig();
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.serverName = sourceHandler.getServerName();
        this.sourceErrorHandler = sourceHandler.getSourceErrorHandler();
        this.headRequest = requestDataHolder.getHttpMethod().equalsIgnoreCase(Constants.HTTP_HEAD_METHOD);
        this.continueRequest = continueRequest;
        this.stateContext = requestMsg.getStateContext();
    }

    @Override
    public void onMessage(HttpCarbonMessage outboundResponseMsg) {
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
    public void onPushResponse(int promiseId, HttpCarbonMessage httpMessage) {
        inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(new UnsupportedOperationException(
                "Sending Server Push messages is not supported for HTTP/1.x connections"));
    }

    private void writeOutboundResponse(HttpCarbonMessage outboundResponseMsg, boolean keepAlive,
            HttpContent httpContent) {
        stateContext.getState().writeOutboundResponseEntityBody(this, outboundResponseMsg, httpContent);
//        ChunkConfig responseChunkConfig = outboundResponseMsg.getProperty(CHUNKING_CONFIG) != null ?
//                (ChunkConfig) outboundResponseMsg.getProperty(CHUNKING_CONFIG) : null;
//        if (responseChunkConfig != null) {
//            this.setChunkConfig(responseChunkConfig);
//        }
//        if (continueRequest) {
//            sourceErrorHandler.setState(RESPONSE_100_CONTINUE_SENT);
//            continueRequest = false;
//        } else {
//            if (sourceErrorHandler.getState().equals(SourceInteractiveState.RECEIVING_ENTITY_BODY)) {
//                // Response is being sent before finish reading the inbound request,
//                // hence close the connection once the response is sent.
//                keepAlive = false;
//            }
//            sourceErrorHandler.setState(SENDING_ENTITY_BODY);
//        }
//
//        ChannelFuture outboundChannelFuture;
//        HttpResponseFuture outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
//        if (isLastHttpContent(httpContent)) {
//            if (!headerWritten) {
//                if (chunkConfig == ChunkConfig.ALWAYS && (
//                        isVersionCompatibleForChunking(requestDataHolder.getHttpVersion()) ||
//                                shouldEnforceChunkingforHttpOneZero(chunkConfig, requestDataHolder.getHttpVersion()))) {
//                    writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);
//                    outboundChannelFuture = checkHeadRequestAndWriteOutboundResponseBody(httpContent);
//
//                } else {
//                    contentLength += httpContent.content().readableBytes();
//                    setupContentLengthRequest(outboundResponseMsg, contentLength);
//                    outboundChannelFuture = writeOutboundResponseHeaderAndBody(outboundResponseMsg,
//                            (LastHttpContent) httpContent, keepAlive);
//                }
//            } else {
//                outboundChannelFuture = checkHeadRequestAndWriteOutboundResponseBody(httpContent);
//            }
//
//            if (!keepAlive) {
//                outboundChannelFuture.addListener(ChannelFutureListener.CLOSE);
//            }
//            if (handlerExecutor != null) {
//                handlerExecutor.executeAtSourceResponseSending(outboundResponseMsg);
//            }
//        } else {
//            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) && (
//                    isVersionCompatibleForChunking(requestDataHolder.getHttpVersion()) ||
//                            shouldEnforceChunkingforHttpOneZero(chunkConfig, requestDataHolder.getHttpVersion()))) {
//                if (!headerWritten) {
//                    writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);
//                }
//                incrementWriteCount(writeCounter);
//
//                if (headRequest) {
//                    httpContent.release();
//                    return;
//                }
//
//                ChannelFuture outboundResponseChannelFuture = sourceContext.writeAndFlush(httpContent);
//                sourceErrorHandler.addResponseWriteFailureListener(outboundRespStatusFuture,
//                                                                   outboundResponseChannelFuture, writeCounter);
//            } else {
//                this.contentList.add(httpContent);
//                contentLength += httpContent.content().readableBytes();
//            }
//        }
    }

    private void resetOutboundListenerState() {
        contentList.clear();
        contentLength = 0;
        headerWritten = false;
    }

    // Decides whether to close the connection after sending the response
    public boolean isKeepAlive() {
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

    public boolean isContinueRequest() {
        return continueRequest;
    }

    public void setContinueRequest(boolean continueRequest) {
        this.continueRequest = continueRequest;
    }

    public HttpCarbonMessage getInboundRequestMsg() {
        return inboundRequestMsg;
    }

    public RequestDataHolder getRequestDataHolder() {
        return requestDataHolder;
    }

    public ChannelHandlerContext getSourceContext() {
        return sourceContext;
    }

    public String getServerName() {
        return serverName;
    }

    public void setKeepAliveConfig(KeepAliveConfig config) {
        this.keepAliveConfig = config;
    }

    public SourceHandler getSourceHandler() {
        return sourceHandler;
    }
}
