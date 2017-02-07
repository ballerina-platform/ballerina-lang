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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

/**
 * Class Which handover incoming requests to be written to BE asynchronously.
 */
public class ClientRequestWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientRequestWorker.class);

    private ConnectionManager.PoolManagementPolicy poolManagementPolicy;

    private HttpRoute httpRoute;
    private SourceHandler sourceHandler;
    private SenderConfiguration senderConfig;
    private CarbonMessage carbonMessage;
    private HttpRequest httpRequest;
    private CarbonCallback carbonCallback;
    private GenericObjectPool genericObjectPool;
    private ConnectionManager connectionManager;
    private EventLoopGroup eventLoopGroup;
    private Class aClass;

    public ClientRequestWorker(HttpRoute httpRoute, SourceHandler sourceHandler, SenderConfiguration senderConfig,
            HttpRequest httpRequest, CarbonMessage carbonMessage, CarbonCallback carbonCallback,
            ConnectionManager.PoolManagementPolicy poolManagementPolicy, GenericObjectPool genericObjectPool,
            ConnectionManager connectionManager, EventLoopGroup eventLoopGroup, Class aClass) {
        this.poolManagementPolicy = poolManagementPolicy;
        this.httpRequest = httpRequest;
        this.sourceHandler = sourceHandler;
        this.senderConfig = senderConfig;
        this.carbonCallback = carbonCallback;
        this.carbonMessage = carbonMessage;
        this.httpRoute = httpRoute;
        this.genericObjectPool = genericObjectPool;
        this.connectionManager = connectionManager;
        this.eventLoopGroup = eventLoopGroup;
        this.aClass = aClass;
    }

    @Override
    public void run() {
        Channel channel = null;
        TargetChannel targetChannel = null;

        if (poolManagementPolicy == ConnectionManager.PoolManagementPolicy.
                PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING) {
            targetChannel = new TargetChannel();
            ChannelFuture future = ChannelUtils
                    .getNewChannelFuture(targetChannel, eventLoopGroup, aClass, httpRoute, senderConfig);

            try {
                channel = ChannelUtils.openChannel(future, httpRoute);
            } catch (Exception failedCause) {
                String msg = "Error when creating channel for route " + httpRoute;
                log.error(msg);
                MessagingException messagingException = new MessagingException(msg, failedCause, 101503);
                carbonMessage.setMessagingException(messagingException);
                carbonCallback.done(carbonMessage);
                return;
            } finally {
                if (channel != null) {
                    targetChannel.setChannel(channel);
                    targetChannel.setTargetHandler(targetChannel.getHTTPClientInitializer().getTargetHandler());
                }
            }
        } else {
            targetChannel = processThroughConnectionPool();

        }
        if (targetChannel != null) {
            targetChannel.setHttpRoute(httpRoute);
            if (targetChannel.getTargetHandler() != null) {
                targetChannel.getTargetHandler().setCallback(carbonCallback);
                targetChannel.getTargetHandler().setIncomingMsg(carbonMessage);
                targetChannel.getTargetHandler().setTargetChannel(targetChannel);
                targetChannel.getTargetHandler().setConnectionManager(connectionManager);
            } else {
                log.error("Cannot find registered TargetHandler probably connection creation is failed");
                String msg = "Connection creation failed for ";
                MessagingException messagingException = new MessagingException(msg, 101503);
                carbonMessage.setMessagingException(messagingException);
                carbonCallback.done(carbonMessage);
                return;
            }

            boolean written = false;
            if (targetChannel.getChannel() != null) {
                written = ChannelUtils.writeContent(targetChannel.getChannel(), httpRequest, carbonMessage);
            }
            if (written) {
                targetChannel.setRequestWritten(true);
            }
            if (sourceHandler != null) {
                targetChannel.setCorrelatedSource(sourceHandler);
                sourceHandler.addTargetChannel(httpRoute, targetChannel);
            }
        }
    }

    private TargetChannel processThroughConnectionPool() {
        try {
            Object obj = genericObjectPool.borrowObject();
            if (obj != null) {
                TargetChannel targetChannel = (TargetChannel) obj;
                targetChannel.setTargetHandler(targetChannel.getHTTPClientInitializer().getTargetHandler());
                return targetChannel;
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            MessagingException messagingException = new MessagingException(msg, 101503);
            carbonMessage.setMessagingException(messagingException);
            carbonCallback.done(carbonMessage);
        }
        return null;
    }

}
