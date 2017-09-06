/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.sender.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.HTTPTraceLoggingHandler;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientInitializer;
import org.wso2.carbon.transport.http.netty.sender.TargetHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A class that encapsulate channel and state.
 */
public class TargetChannel {

    private static final Logger log = LoggerFactory.getLogger(TargetChannel.class);

    private Channel channel;
    private TargetHandler targetHandler;
    private HTTPClientInitializer httpClientInitializer;
    private HttpRoute httpRoute;
    private SourceHandler correlatedSource;
    private ChannelFuture channelFuture;
    private ConnectionManager connectionManager;
    private boolean isRequestWritten = false;

    public TargetChannel(HTTPClientInitializer httpClientInitializer, ChannelFuture channelFuture) {
        this.httpClientInitializer = httpClientInitializer;
        this.channelFuture = channelFuture;
    }

    public Channel getChannel() {
        return channel;
    }

    public TargetChannel setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public TargetHandler getTargetHandler() {
        return targetHandler;
    }

    public void setTargetHandler(TargetHandler targetHandler) {
        this.targetHandler = targetHandler;
    }

    public HTTPClientInitializer getHTTPClientInitializer() {
        return httpClientInitializer;
    }

    public HttpRoute getHttpRoute() {
        return httpRoute;
    }

    public void setHttpRoute(HttpRoute httpRoute) {
        this.httpRoute = httpRoute;
    }

    public SourceHandler getCorrelatedSource() {
        return correlatedSource;
    }

    public void setCorrelatedSource(SourceHandler correlatedSource) {
        this.correlatedSource = correlatedSource;
    }

    public boolean isRequestWritten() {
        return isRequestWritten;
    }

    public void setRequestWritten(boolean isRequestWritten) {
        this.isRequestWritten = isRequestWritten;
    }

    public void configTargetHandler(HTTPCarbonMessage httpCarbonMessage, HttpResponseFuture httpResponseFuture) {
        this.setTargetHandler(this.getHTTPClientInitializer().getTargetHandler());
        TargetHandler targetHandler = this.getTargetHandler();
        targetHandler.setHttpResponseFuture(httpResponseFuture);
        targetHandler.setIncomingMsg(httpCarbonMessage);
        this.getTargetHandler().setConnectionManager(connectionManager);
        targetHandler.setTargetChannel(this);
    }

    public void setEndPointTimeout(int socketIdleTimeout) {
        this.getChannel().pipeline().addBefore(Constants.TARGET_HANDLER,
                Constants.IDLE_STATE_HANDLER, new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, 0,
                        TimeUnit.MILLISECONDS));
    }

    public void setCorrelationIdForLogging() {
        ChannelPipeline pipeline = this.getChannel().pipeline();
        SourceHandler srcHandler = this.getCorrelatedSource();
        if (srcHandler != null && pipeline.get(Constants.HTTP_TRACE_LOG_HANDLER) != null) {
            HTTPTraceLoggingHandler loggingHandler = (HTTPTraceLoggingHandler) pipeline.get(
                    Constants.HTTP_TRACE_LOG_HANDLER);
            loggingHandler.setCorrelatedSourceId(
                    srcHandler.getInboundChannelContext().channel().id().asShortText());
        }
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void writeContent(HTTPCarbonMessage httpCarbonRequest) {
        try {
            if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                        executeAtTargetRequestReceiving(httpCarbonRequest);
            }

            Util.prepareBuiltMessageForTransfer(httpCarbonRequest);
            Util.setupTransferEncodingForRequest(httpCarbonRequest);
            HttpRequest httpRequest = Util.createHttpRequest(httpCarbonRequest);

            this.setRequestWritten(true);
            this.getChannel().write(httpRequest);

            while (true) {
                if (httpCarbonRequest.isEndOfMsgAdded() && httpCarbonRequest.isEmpty()) {
                    this.getChannel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    break;
                }
                HttpContent httpContent = httpCarbonRequest.getHttpContent();
                if (httpContent instanceof LastHttpContent) {
                    this.getChannel().writeAndFlush(httpContent);
                    if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                        HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                executeAtTargetRequestSending(httpCarbonRequest);
                    }
                    break;
                }
                if (httpContent != null) {
                    this.getChannel().write(httpContent);
                }
            }
        } catch (Exception e) {
            String msg = "Failed to send the request : " + e.getMessage().toLowerCase(Locale.ENGLISH);
            log.error(msg, e);
            MessagingException messagingException = new MessagingException(msg, e, 101500);
            httpCarbonRequest.setMessagingException(messagingException);
            this.targetHandler.getHttpResponseFuture().notifyHttpListener(httpCarbonRequest);
        }
    }
}
