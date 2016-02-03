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

package org.wso2.carbon.transport.http.netty.listener;

import com.lmax.disruptor.RingBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.TransportConstants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.common.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.transport.http.netty.latency.metrics.ConnectionMetricsHolder;
import org.wso2.carbon.transport.http.netty.latency.metrics.RequestMetricsHolder;
import org.wso2.carbon.transport.http.netty.latency.metrics.ResponseMetricsHolder;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * A Class responsible for handle  incoming message through netty inbound pipeline.
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(SourceHandler.class);

    private RingBuffer disruptor;
    private ChannelHandlerContext ctx;
    private NettyCarbonMessage cMsg;
    private ConnectionManager connectionManager;
    private Map<String, TargetChannel> channelFutureMap = new HashMap<>();

    private int queueSize;
    private DisruptorConfig disruptorConfig;
    private Map<String, GenericObjectPool> targetChannelPool;

    private ConnectionMetricsHolder serverConnectionMetricsHolder;
        private ConnectionMetricsHolder clientConnectionMetricsHolder;
        private RequestMetricsHolder serverRequestMetricsHolder;
        private RequestMetricsHolder clientRequestMetricsHolder;
        private ResponseMetricsHolder serverResponseMetricsHolder;
        private ResponseMetricsHolder clientResponseMetricsHolder;


    public SourceHandler(ConnectionManager connectionManager) throws Exception {
        this.connectionManager = connectionManager;

        // Initialize the connection metric holder
        serverConnectionMetricsHolder = new ConnectionMetricsHolder(TransportConstants.TYPE_SOURCE_CONNECTION);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        // Start the server connection Timer
        serverConnectionMetricsHolder.startTimer();
        disruptorConfig = DisruptorFactory.getDisruptorConfig(DisruptorFactory.DisruptorType.INBOUND);
        disruptor = disruptorConfig.getDisruptor();
        this.ctx = ctx;
        this.targetChannelPool = connectionManager.getTargetChannelPool();

    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            // Initialize the metric holders
            this.serverRequestMetricsHolder = new RequestMetricsHolder(
                    TransportConstants.TYPE_SERVER_REQUEST);
            this.clientRequestMetricsHolder = new RequestMetricsHolder(
                    TransportConstants.TYPE_CLIENT_REQUEST);
            this.serverResponseMetricsHolder = new ResponseMetricsHolder(
                    TransportConstants.TYPE_SERVER_RESPONSE);
            this.clientResponseMetricsHolder = new ResponseMetricsHolder(
                    TransportConstants.TYPE_CLIENT_RESPONSE);
            this.clientConnectionMetricsHolder = new ConnectionMetricsHolder(
                    TransportConstants.TYPE_CLIENT_CONNECTION);

            serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_LIFE_TIMER);

            cMsg = new NettyCarbonMessage();
            cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
            cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
            ResponseCallback responseCallback = new ResponseCallback(this.ctx);
            cMsg.setProperty(Constants.CALL_BACK, responseCallback);
            HttpRequest httpRequest = (HttpRequest) msg;


            cMsg.setProperty(Constants.TO, httpRequest.getUri());
            cMsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
            cMsg.setProperty(Constants.SRC_HNDLR, this);
            cMsg.setProperty(Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
            cMsg.setProperty(Constants.HTTP_METHOD, httpRequest.getMethod().name());
            serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_HEADER_READ_TIMER);
            cMsg.setHeaders(Util.getHeaders(httpRequest));
            serverRequestMetricsHolder.stopTimer(TransportConstants.REQUEST_HEADER_READ_TIMER);
            cMsg.setProperty(TransportConstants.CLIENT_RESPONSE_METRICS_HOLDER, this.clientResponseMetricsHolder);
            cMsg.setProperty(TransportConstants.SERVER_RESPONSE_METRICS_HOLDER, this.serverResponseMetricsHolder);
            cMsg.setProperty(TransportConstants.SERVER_REQUEST_METRICS_HOLDER, this.serverRequestMetricsHolder);
            cMsg.setProperty(TransportConstants.CLIENT_REQUEST_METRICS_HOLDER, this.clientRequestMetricsHolder);
            cMsg.setProperty(TransportConstants.SERVER_CONNECTION_METRICS_HOLDER, this.serverConnectionMetricsHolder);
            cMsg.setProperty(TransportConstants.CLIENT_CONNECTION_METRICS_HOLDER, this.clientConnectionMetricsHolder);



            if (disruptorConfig.isShared()) {
                cMsg.setProperty(Constants.DISRUPTOR, disruptor);
            }
            disruptor.publishEvent(new CarbonEventPublisher(cMsg));
        } else {
            if (cMsg != null) {
                if (msg instanceof HttpContent) {
                    if (serverRequestMetricsHolder.getrBodyReadContext() == null) {
                        serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_BODY_READ_TIMER);
                    }
                    HttpContent httpContent = (HttpContent) msg;
                    cMsg.addHttpContent(httpContent);
                    if (msg instanceof LastHttpContent) {
                        serverRequestMetricsHolder.stopTimer(TransportConstants.REQUEST_BODY_READ_TIMER);
                        serverRequestMetricsHolder.stopTimer(TransportConstants.REQUEST_LIFE_TIMER);
                        cMsg.setEomAdded(true);
                    }
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // Stop the connector timer
        serverConnectionMetricsHolder.stopTimer();
        log.info("************ " + this.serverConnectionMetricsHolder.getConnectionTimer().getCount());
        disruptorConfig.notifyChannelInactive();
        connectionManager.notifyChannelInactive();
    }

    public void addTargetChannel(HttpRoute route, TargetChannel targetChannel) {
        channelFutureMap.put(route.toString(), targetChannel);
    }

    public void removeChannelFuture(HttpRoute route) {
        log.debug("Removing channel future from map");
        channelFutureMap.remove(route.toString());
    }

    public TargetChannel getChannel(HttpRoute route) {
        return channelFutureMap.get(route.toString());
    }

    public Map<String, GenericObjectPool> getTargetChannelPool() {
        return targetChannelPool;
    }

    public ChannelHandlerContext getInboundChannelContext() {
        return ctx;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("Exception caught in Netty Source handler", cause);
            }
    public ConnectionMetricsHolder getServerConnectionMetricsHolder() {
                return serverConnectionMetricsHolder;
            }

                public ConnectionMetricsHolder getClientConnectionMetricsHolder() {
                return clientConnectionMetricsHolder;
            }

                public RequestMetricsHolder getServerRequestMetricsHolder() {
                return serverRequestMetricsHolder;
            }

                public RequestMetricsHolder getClientRequestMetricsHolder() {
                return clientRequestMetricsHolder;
            }

                public ResponseMetricsHolder getServerResponseMetricsHolder() {
                return serverResponseMetricsHolder;
            }

                public ResponseMetricsHolder getClientResponseMetricsHolder() {
                return clientResponseMetricsHolder;
           }
}



