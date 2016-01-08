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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.sender;


import com.lmax.disruptor.RingBuffer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.FaultHandler;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

/**
 * Class Which handover incoming requests to be written to BE asynchronously.
 */
public class ClientRequestWorker implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(ClientRequestWorker.class);

    private boolean globalEndpointPooling;

    private HttpRoute httpRoute;
    private SourceHandler sourceHandler;
    private NettyClientInitializer nettyClientInitializer;
    private CarbonMessage carbonMessage;
    private HttpRequest httpRequest;
    private CarbonCallback carbonCallback;
    private GenericObjectPool genericObjectPool;
    private ConnectionManager connectionManager;
    private RingBuffer ringBuffer;


    public ClientRequestWorker(HttpRoute httpRoute, SourceHandler sourceHandler,
                               NettyClientInitializer nettyClientInitializer, HttpRequest httpRequest,
                               CarbonMessage carbonMessage, CarbonCallback carbonCallback,
                               boolean globalEndpointPooling, GenericObjectPool genericObjectPool,
                               ConnectionManager connectionManager, RingBuffer ringBuffer) {
        this.globalEndpointPooling = globalEndpointPooling;
        this.httpRequest = httpRequest;
        this.sourceHandler = sourceHandler;
        this.nettyClientInitializer = nettyClientInitializer;
        this.carbonCallback = carbonCallback;
        this.carbonMessage = carbonMessage;
        this.httpRoute = httpRoute;
        this.genericObjectPool = genericObjectPool;
        this.connectionManager = connectionManager;
        this.ringBuffer = ringBuffer;
    }


    @Override
    public void run() {
        Channel channel = null;
        TargetChannel targetChannel = null;
        ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
        EventLoopGroup group = ctx.channel().eventLoop();
        Class cl = ctx.channel().getClass();


        if (globalEndpointPooling) {

            try {
                Object obj = genericObjectPool.borrowObject();
                if (obj != null) {
                    targetChannel = (TargetChannel) obj;
                    targetChannel.setTargetHandler(targetChannel.getNettyClientInitializer().getTargetHandler());
                }
            } catch (Exception e) {
                String msg = "Cannot borrow free channel from pool";
                log.error(msg, e);
               FaultHandler  faultHandler = carbonMessage.getFaultHandlerStack().pop();
                if (faultHandler != null) {
                    faultHandler.handleFault("502", e, carbonCallback);
                    carbonMessage.getFaultHandlerStack().push(faultHandler);
                }
            }
        } else {
            targetChannel = new TargetChannel();
            ChannelFuture future = ChannelUtils.getNewChannelFuture(targetChannel, group, cl, httpRoute,
                                                                    nettyClientInitializer);

            try {
                channel = ChannelUtils.openChannel(future, httpRoute);
            } catch (Exception failedCause) {
                String msg = "Error when creating channel for route " + httpRoute;
                log.error(msg);
                FaultHandler faultHandler = carbonMessage.getFaultHandlerStack().pop();
                targetChannel = null;
                if (faultHandler != null) {
                    faultHandler.handleFault("502", failedCause, carbonCallback);
                    carbonMessage.getFaultHandlerStack().push(faultHandler);
                }
            } finally {
                if (channel != null) {
                    targetChannel.setChannel(channel);
                    targetChannel.setTargetHandler(targetChannel.getNettyClientInitializer().getTargetHandler());
                    sourceHandler.addTargetChannel(httpRoute, targetChannel);
                }
            }
        }
        if (targetChannel != null) {
            targetChannel.setHttpRoute(httpRoute);
            targetChannel.setCorrelatedSource(sourceHandler);
            targetChannel.getTargetHandler().setCallback(carbonCallback);
            targetChannel.getTargetHandler().setRingBuffer(ringBuffer);
            targetChannel.getTargetHandler().setTargetChannel(targetChannel);
            targetChannel.getTargetHandler().setConnectionManager(connectionManager);
            ChannelUtils.writeContent(channel, httpRequest, carbonMessage);
        }
    }
}
