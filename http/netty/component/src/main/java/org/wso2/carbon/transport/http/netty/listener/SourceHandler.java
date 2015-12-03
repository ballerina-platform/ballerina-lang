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
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.HTTPContentChunk;
import org.wso2.carbon.messaging.Pipe;
import org.wso2.carbon.messaging.PipeImpl;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.common.disruptor.publisher.CarbonEventPublisher;
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
    private CarbonMessage cMsg;
    private ConnectionManager connectionManager;
    private Map<String, TargetChannel> channelFutureMap = new HashMap<>();

    private int queueSize;
    private DisruptorConfig disruptorConfig;
    private Map<String, GenericObjectPool> targetChannelPool;

    public SourceHandler(int queueSize , ConnectionManager connectionManager) throws Exception {
        this.queueSize = queueSize;
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        disruptorConfig = DisruptorFactory.getDisruptorConfig(DisruptorFactory.DisruptorType.INBOUND);
        disruptor = disruptorConfig.getDisruptor();
        this.ctx = ctx;
        this.targetChannelPool = connectionManager.getTargetChannelPool();

    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            cMsg = new CarbonMessage(Constants.PROTOCOL_NAME);
            cMsg.setPort(((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
            cMsg.setHost(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
            ResponseCallback responseCallback = new ResponseCallback(this.ctx);
            cMsg.setCarbonCallback(responseCallback);
            HttpRequest httpRequest = (HttpRequest) msg;
            cMsg.setURI(httpRequest.getUri());
            Pipe pipe = new PipeImpl(queueSize);
            cMsg.setPipe(pipe);

            cMsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
            cMsg.setProperty(Constants.SRC_HNDLR, this);
            cMsg.setProperty(Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
            cMsg.setProperty(Constants.HTTP_METHOD, httpRequest.getMethod().name());
            cMsg.setProperty(Constants.TRANSPORT_HEADERS, Util.getHeaders(httpRequest));

            if (disruptorConfig.isShared()) {
                cMsg.setProperty(Constants.DISRUPTOR, disruptor);
            }
            disruptor.publishEvent(new CarbonEventPublisher(cMsg));
        } else {
            HTTPContentChunk chunk;
            if (cMsg != null) {
                if (msg instanceof HttpContent) {
                    HttpContent httpContent = (HttpContent) msg;
                    chunk = new HTTPContentChunk(httpContent);
                    cMsg.getPipe().addContentChunk(chunk);
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
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
        log.error("Exception caught in Netty Source handler" , cause);
    }
}



