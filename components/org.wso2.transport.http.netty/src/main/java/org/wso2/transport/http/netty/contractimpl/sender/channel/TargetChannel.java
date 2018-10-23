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

package org.wso2.transport.http.netty.contractimpl.sender.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.ForwardedExtensionConfig;
import org.wso2.transport.http.netty.contractimpl.common.BackPressureHandler;
import org.wso2.transport.http.netty.contractimpl.common.HttpRoute;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.listener.HttpTraceLoggingHandler;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.ConnectionAvailabilityFuture;
import org.wso2.transport.http.netty.contractimpl.sender.ForwardedHeaderUpdater;
import org.wso2.transport.http.netty.contractimpl.sender.HttpClientChannelInitializer;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.states.SendingHeaders;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A class that encapsulate channel and state.
 */
public class TargetChannel {

    private static final Logger LOG = LoggerFactory.getLogger(TargetChannel.class);

    private Channel channel;
    private TargetHandler targetHandler;
    private HttpClientChannelInitializer httpClientChannelInitializer;
    private HttpRoute httpRoute;
    private SourceHandler correlatedSource;
    private ChannelFuture channelFuture;
    private ConnectionManager connectionManager;
    private boolean requestHeaderWritten = false;
    private String httpVersion;
    private ChunkConfig chunkConfig;
    private HandlerExecutor handlerExecutor;
    private Http2ClientChannel http2ClientChannel;

    private final ConnectionAvailabilityFuture connectionAvailabilityFuture;
    private HttpResponseFuture httpInboundResponseFuture;

    public TargetChannel(HttpClientChannelInitializer httpClientChannelInitializer, ChannelFuture channelFuture,
                         HttpRoute httpRoute, ConnectionAvailabilityFuture connectionAvailabilityFuture) {
        this.httpClientChannelInitializer = httpClientChannelInitializer;
        this.channelFuture = channelFuture;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.httpRoute = httpRoute;
        if (httpClientChannelInitializer != null) {
            http2ClientChannel =
                    new Http2ClientChannel(httpClientChannelInitializer.getHttp2ConnectionManager(),
                                           httpClientChannelInitializer.getConnection(),
                                           httpRoute, channelFuture.channel());
        }
        this.connectionAvailabilityFuture = connectionAvailabilityFuture;
    }

    public ConnectionAvailabilityFuture getConnenctionReadyFuture() {
        return connectionAvailabilityFuture;
    }

    public Channel getChannel() {
        return channel;
    }

    public TargetChannel setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    private TargetHandler getTargetHandler() {
        return targetHandler;
    }

    private void setTargetHandler(TargetHandler targetHandler) {
        this.targetHandler = targetHandler;
    }

    private HttpClientChannelInitializer getHttpClientChannelInitializer() {
        return httpClientChannelInitializer;
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

    public boolean isRequestHeaderWritten() {
        return requestHeaderWritten;
    }

    public void setRequestHeaderWritten(boolean isRequestWritten) {
        this.requestHeaderWritten = isRequestWritten;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setChunkConfig(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    public void configTargetHandler(HttpCarbonMessage httpCarbonMessage, HttpResponseFuture httpInboundResponseFuture) {
        this.setTargetHandler(this.getHttpClientChannelInitializer().getTargetHandler());
        TargetHandler handler = this.getTargetHandler();
        handler.setHttpResponseFuture(httpInboundResponseFuture);
        handler.setOutboundRequestMsg(httpCarbonMessage);
        handler.setConnectionManager(connectionManager);
        handler.setTargetChannel(this);

        this.httpInboundResponseFuture = httpInboundResponseFuture;
    }

    public void setEndPointTimeout(int socketIdleTimeout) {
        this.getChannel().pipeline().addBefore(Constants.TARGET_HANDLER, Constants.IDLE_STATE_HANDLER,
                                               new IdleStateHandler(0, 0, socketIdleTimeout, TimeUnit.MILLISECONDS));
        http2ClientChannel.setSocketIdleTimeout(socketIdleTimeout);
    }

    public void setCorrelationIdForLogging() {
        ChannelPipeline pipeline = this.getChannel().pipeline();
        SourceHandler srcHandler = this.getCorrelatedSource();
        if (srcHandler != null && pipeline.get(Constants.HTTP_TRACE_LOG_HANDLER) != null) {
            HttpTraceLoggingHandler loggingHandler = (HttpTraceLoggingHandler)
                    pipeline.get(Constants.HTTP_TRACE_LOG_HANDLER);
            loggingHandler.setCorrelatedSourceId(srcHandler.getInboundChannelContext().channel().id().asShortText());
        }
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public Http2ClientChannel getHttp2ClientChannel() {
        return http2ClientChannel;
    }

    public void writeContent(HttpCarbonMessage httpOutboundRequest) {
        BackPressureHandler backpressureHandler = Util.getBackPressureHandler(targetHandler.getContext());

        Util.setBackPressureObservableListener(httpOutboundRequest.isPassthrough(), backpressureHandler,
                                               httpOutboundRequest.getSourceContext(), targetHandler.getContext());

        if (handlerExecutor != null) {
            handlerExecutor.executeAtTargetRequestReceiving(httpOutboundRequest);
        }

        resetTargetChannelState();

        MessageStateContext messageStateContext = httpOutboundRequest.getMessageStateContext();
        if (messageStateContext == null) {
            messageStateContext = new MessageStateContext();
            httpOutboundRequest.setMessageStateContext(messageStateContext);
        }
        httpOutboundRequest.getMessageStateContext()
                .setSenderState(new SendingHeaders(messageStateContext, this, httpVersion, chunkConfig,
                                                   httpInboundResponseFuture));
        httpOutboundRequest.getHttpContentAsync().setMessageListener((httpContent -> {
            if (backpressureHandler != null) {
                backpressureHandler.getBackPressureObservable().notifyAcquire();
            }
            this.channel.eventLoop().execute(() -> {
                try {
                    writeOutboundRequest(httpOutboundRequest, httpContent);
                } catch (Exception exception) {
                    String errorMsg = "Failed to send the request : "
                            + exception.getMessage().toLowerCase(Locale.ENGLISH);
                    LOG.error(errorMsg, exception);
                    this.targetHandler.getHttpResponseFuture().notifyHttpListener(exception);
                }
            });
        }));
    }

    private void writeOutboundRequest(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) throws Exception {
        httpOutboundRequest.getMessageStateContext().getSenderState()
                .writeOutboundRequestEntity(httpOutboundRequest, httpContent);
    }

    private void resetTargetChannelState() {
        this.setRequestHeaderWritten(false);
    }

    public void setForwardedExtension(ForwardedExtensionConfig forwardedConfig, HttpCarbonMessage httpOutboundRequest) {
        if (forwardedConfig == ForwardedExtensionConfig.DISABLE) {
            return;
        }
        String localAddress = ((InetSocketAddress) this.getChannel().localAddress()).getAddress().getHostAddress();
        ForwardedHeaderUpdater headerUpdater = new ForwardedHeaderUpdater(httpOutboundRequest, localAddress);
        if (headerUpdater.isForwardedHeaderRequired()) {
            headerUpdater.setForwardedHeader();
            return;
        }
        if (headerUpdater.isXForwardedHeaderRequired()) {
            if (forwardedConfig == ForwardedExtensionConfig.ENABLE) {
                headerUpdater.setDefactoForwardedHeaders();
                return;
            }
            headerUpdater.transformAndSetForwardedHeader();
            return;
        }
        LOG.warn("Both Forwarded and X-Forwarded-- headers are present. Hence updating only the forwarded header");
        headerUpdater.setForwardedHeader();
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}
