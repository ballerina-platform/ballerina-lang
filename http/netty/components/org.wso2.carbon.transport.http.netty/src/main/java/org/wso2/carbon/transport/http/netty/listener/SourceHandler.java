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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.common.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.NettyCarbonMessage;
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
    protected ChannelHandlerContext ctx;
    protected NettyCarbonMessage cMsg;
    protected ConnectionManager connectionManager;
    private Map<String, TargetChannel> channelFutureMap = new HashMap<>();
    private DisruptorConfig disruptorConfig;
    protected Map<String, GenericObjectPool> targetChannelPool;
    protected ListenerConfiguration listenerConfiguration;

    public ListenerConfiguration getListenerConfiguration() {
        return listenerConfiguration;
    }

    public SourceHandler(ConnectionManager connectionManager, ListenerConfiguration listenerConfiguration)
            throws Exception {
        this.listenerConfiguration = listenerConfiguration;
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        // Start the server connection Timer

        if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {

            NettyTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }

        disruptorConfig = DisruptorFactory.getDisruptorConfig(DisruptorFactory.DisruptorType.INBOUND);
        disruptor = disruptorConfig.getDisruptor();
        this.ctx = ctx;
        this.targetChannelPool = connectionManager.getTargetChannelPool();

    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpMessage) {

            publishToDisruptor(msg);
            ByteBuf content = ((FullHttpMessage) msg).content();
            cMsg.addHttpContent(new DefaultLastHttpContent(content));
            cMsg.setEndOfMsgAdded(true);
            if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {

                NettyTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestSending(cMsg);
            }

        } else if (msg instanceof HttpRequest) {

            publishToDisruptor(msg);

        } else {
            if (cMsg != null) {
                if (msg instanceof HttpContent) {
                    HttpContent httpContent = (HttpContent) msg;
                    cMsg.addHttpContent(httpContent);
                    if (msg instanceof LastHttpContent) {
                        cMsg.setEndOfMsgAdded(true);
                        if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {

                            NettyTransportContextHolder.getInstance().getHandlerExecutor().
                                    executeAtSourceRequestSending(cMsg);
                        }

                    }
                }
            }
        }

    }

    private void publishToDisruptor(Object msg) {
        cMsg = (NettyCarbonMessage) setupCarbonMessage(msg);
        if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            NettyTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }
        cMsg.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.IS_DISRUPTOR_ENABLE, true);
        if (disruptorConfig.isShared()) {
            cMsg.setProperty(Constants.DISRUPTOR, disruptor);
        }

        boolean continueRequest = true;

        if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {

            continueRequest = NettyTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeRequestContinuationValidator(cMsg, carbonMessage -> {
                        CarbonCallback responseCallback = (CarbonCallback) cMsg
                                .getProperty(org.wso2.carbon.messaging.Constants.CALL_BACK);
                        responseCallback.done(carbonMessage);
                    });

        }
        if (continueRequest) {
            disruptor.publishEvent(new CarbonEventPublisher(cMsg));
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Stop the connector timer
        ctx.close();
        if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            NettyTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
        }
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
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    protected CarbonMessage setupCarbonMessage(Object msg) {
        cMsg = new NettyCarbonMessage();
        if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            NettyTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }
        cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        ResponseCallback responseCallback = new ResponseCallback(this.ctx);
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.CALL_BACK, responseCallback);
        HttpRequest httpRequest = (HttpRequest) msg;

        cMsg.setProperty(Constants.TO, httpRequest.getUri());
        cMsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
        cMsg.setProperty(Constants.SRC_HNDLR, this);
        cMsg.setProperty(Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
        cMsg.setProperty(Constants.HTTP_METHOD, httpRequest.getMethod().name());
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, httpRequest.getProtocolVersion().protocolName());
        if (listenerConfiguration.getSslConfig() != null) {
            cMsg.setProperty(Constants.IS_SECURED_CONNECTION, true);
        } else {
            cMsg.setProperty(Constants.IS_SECURED_CONNECTION, false);
        }
        cMsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        cMsg.setProperty(Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_ADDRESS, ctx.channel().remoteAddress());
        cMsg.setProperty(Constants.REMOTE_HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.REQUEST_URL, httpRequest.getUri());
        ChannelHandler handler = ctx.handler();
        if (handler instanceof WorkerPoolDispatchingSourceHandler) {
            cMsg.setProperty(Constants.CHANNEL_ID,
                             ((WorkerPoolDispatchingSourceHandler) handler).getListenerConfiguration().getId());
        } else if (handler instanceof SourceHandler) {
            cMsg.setProperty(Constants.CHANNEL_ID, ((SourceHandler) handler).getListenerConfiguration().getId());
        } else {
            //Shouldn't come to here
            throw new RuntimeException("Error while getting the channel ID");
        }
        cMsg.setHeaders(Util.getHeaders(httpRequest));
        return cMsg;
    }
}
