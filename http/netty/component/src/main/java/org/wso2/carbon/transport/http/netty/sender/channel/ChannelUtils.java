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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.sender.NettyClientInitializer;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for Channel handling.
 */
public class ChannelUtils {

    private static final Logger log = LoggerFactory.getLogger(ChannelUtils.class);

    /**
     * Provides incomplete Netty channel future.
     *
     * @param targetChannel  Target channel which has channel specific parameters such as handler
     * @param eventLoopGroup Event loop group of inbound IO workers
     * @param eventLoopClass Event loop class if Inbound IO Workers
     * @param httpRoute      Http Route which represents BE connections
     * @return ChannelFuture
     */
    @SuppressWarnings("unchecked")
    public static ChannelFuture getNewChannelFuture(TargetChannel targetChannel, EventLoopGroup eventLoopGroup,
                                                    Class eventLoopClass, HttpRoute httpRoute ,
                                                                         NettyClientInitializer channelInitializer) {
        BootstrapConfiguration bootstrapConfiguration = BootstrapConfiguration.getInstance();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.channel(eventLoopClass);
        clientBootstrap.group(eventLoopGroup);
        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, bootstrapConfiguration.isKeepAlive());
        clientBootstrap.option(ChannelOption.TCP_NODELAY, bootstrapConfiguration.isTcpNoDelay());
        clientBootstrap.option(ChannelOption.SO_REUSEADDR, bootstrapConfiguration.isSocketReuse());
        clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, bootstrapConfiguration.getConnectTimeOut());

        // set the pipeline factory, which creates the pipeline for each newly created channels

        targetChannel.setNettyClientInitializer(channelInitializer);
        clientBootstrap.handler(channelInitializer);
        if (log.isDebugEnabled()) {
            log.debug("Created new TCP client bootstrap connecting to {}:{} with options: {}",
                      httpRoute.getHost(), httpRoute.getPort(), clientBootstrap);
        }

        return clientBootstrap.connect(new InetSocketAddress(httpRoute.getHost(), httpRoute.getPort()));
    }

    /**
     * Open Channel for BE.
     *
     * @param channelFuture ChannelFuture Object
     * @param httpRoute     HttpRoute represents host and port for BE
     * @return Channel
     * @throws Exception Exception to notify any errors occur during opening the channel
     */
    public static Channel openChannel(ChannelFuture channelFuture, HttpRoute httpRoute) throws Exception {

        BootstrapConfiguration bootstrapConfiguration = BootstrapConfiguration.getInstance();
        // blocking for channel to be done
        if (log.isTraceEnabled()) {
            log.trace("Waiting for operation to complete {} for {} millis", channelFuture,
                      bootstrapConfiguration.getConnectTimeOut());
        }

        // here we need to wait it in other thread
        final CountDownLatch channelLatch = new CountDownLatch(1);
        channelFuture.addListener(cf -> channelLatch.countDown());

        try {
            boolean wait = channelLatch.await(bootstrapConfiguration.getConnectTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new Exception("Interrupted while waiting for " + "connection to " + httpRoute.toString());
        }

        Channel channel = null;

        if (channelFuture.isDone() && channelFuture.isSuccess()) {
            channel = channelFuture.channel();
            if (log.isDebugEnabled()) {
                log.debug("Creating connector to address: {}", httpRoute.toString());
            }
        } else if (channelFuture.isDone() && channelFuture.isCancelled()) {
            ConnectException cause = new ConnectException("Request Cancelled, " + httpRoute.toString());
            if (channelFuture.cause() != null) {
                cause.initCause(channelFuture.cause());
            }
            throw cause;
        } else if (!channelFuture.isDone() && !channelFuture.isSuccess() &&
                   !channelFuture.isCancelled() && (channelFuture.cause() == null)) {
            throw new ConnectException("Connection timeout, " + httpRoute.toString());
        } else {
            ConnectException cause = new ConnectException("Connection refused, " + httpRoute.toString());
            if (channelFuture.cause() != null) {
                cause.initCause(channelFuture.cause());
            }
            throw cause;
        }
        return channel;
    }

    public static boolean writeContent(Channel channel, HttpRequest httpRequest, CarbonMessage carbonMessage) {
        channel.write(httpRequest);
        NettyCarbonMessage nettyCMsg = (NettyCarbonMessage) carbonMessage;
        while (true) {
            HttpContent httpContent = nettyCMsg.getHttpContent();
            if (httpContent instanceof LastHttpContent) {
                channel.writeAndFlush(httpContent);
                break;
            }
            if (httpContent != null) {
                channel.write(httpContent);
            }
        }
        return true;
    }


}
