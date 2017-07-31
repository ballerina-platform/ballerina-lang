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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientInitializer;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;

/**
 * Utility class for Channel handling.
 */
public class ChannelUtils {

    private static final Logger log = LoggerFactory.getLogger(ChannelUtils.class);

    /**
     * Provides incomplete Netty channel future.
     *
     * @param targetChannel       Target channel which has channel specific parameters such as handler
     * @param eventLoopGroup      Event loop group of inbound IO workers
     * @param eventLoopClass      Event loop class if Inbound IO Workers
     * @param httpRoute           Http Route which represents BE connections
     * @param senderConfiguration sender configuration
     * @return ChannelFuture
     */
    @SuppressWarnings("unchecked")
    public static ChannelFuture getNewChannelFuture(TargetChannel targetChannel, EventLoopGroup eventLoopGroup,
            Class eventLoopClass, HttpRoute httpRoute, SenderConfiguration senderConfiguration) {
        BootstrapConfiguration bootstrapConfiguration = BootstrapConfiguration.getInstance();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.channel(eventLoopClass);
        clientBootstrap.group(eventLoopGroup);
        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, bootstrapConfiguration.isKeepAlive());
        clientBootstrap.option(ChannelOption.TCP_NODELAY, bootstrapConfiguration.isTcpNoDelay());
        clientBootstrap.option(ChannelOption.SO_REUSEADDR, bootstrapConfiguration.isSocketReuse());
        clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, bootstrapConfiguration.getConnectTimeOut());

        // set the pipeline factory, which creates the pipeline for each newly created channels
        SSLEngine sslEngine = null;
        SSLConfig sslConfig = senderConfiguration.getSslConfig();
        if (sslConfig != null) {
            SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(sslConfig);
            sslEngine = sslHandlerFactory.build();
            sslEngine.setUseClientMode(true);
            sslHandlerFactory.setSNIServerNames(sslEngine, httpRoute.getHost());
        }

        HTTPClientInitializer httpClientInitializer = new HTTPClientInitializer(sslEngine);
        targetChannel.setHTTPClientInitializer(httpClientInitializer);
        clientBootstrap.handler(httpClientInitializer);
        if (log.isDebugEnabled()) {
            log.debug("Created new TCP client bootstrap connecting to {}:{} with options: {}", httpRoute.getHost(),
                    httpRoute.getPort(), clientBootstrap);
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

        // this wait is okay as we have a timeout in connect method
        channelFuture.awaitUninterruptibly();
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

    /**
     * Method used to write content to outbound endpoint.
     *
     * @param channel       OutboundChanel
     * @param httpRequest   HTTPRequest
     * @param carbonMessage Carbon Message
     * @return
     */
    public static boolean writeContent(Channel channel, HttpRequest httpRequest, CarbonMessage carbonMessage)
                                                                                        throws Exception {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                    executeAtTargetRequestReceiving(carbonMessage);
        }
        channel.write(httpRequest);

        if (carbonMessage instanceof HTTPCarbonMessage) {
            HTTPCarbonMessage nettyCMsg = (HTTPCarbonMessage) carbonMessage;
            while (true) {
                if (nettyCMsg.isEndOfMsgAdded() && nettyCMsg.isEmpty()) {
                    channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    break;
                }
                HttpContent httpContent = nettyCMsg.getHttpContent();
                if (httpContent instanceof LastHttpContent) {
                    channel.writeAndFlush(httpContent);
                    if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                        HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                executeAtTargetRequestSending(carbonMessage);
                    }
                    break;
                }
                if (httpContent != null) {
                    channel.write(httpContent);
                }

            }
        } else if (carbonMessage instanceof DefaultCarbonMessage || carbonMessage instanceof TextCarbonMessage
                || carbonMessage instanceof BinaryCarbonMessage) {
            if (carbonMessage.isEndOfMsgAdded() && carbonMessage.isEmpty()) {
                channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                return true;
            }
            while (true) {
                ByteBuffer byteBuffer = carbonMessage.getMessageBody();
                ByteBuf bbuf = Unpooled.wrappedBuffer(byteBuffer);
                DefaultHttpContent httpContent = new DefaultHttpContent(bbuf);
                channel.write(httpContent);
                if (carbonMessage.isEndOfMsgAdded() && carbonMessage.isEmpty()) {
                    channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                        HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                executeAtTargetRequestSending(carbonMessage);
                    }
                    break;
                }
            }
        } else {
            throw new ClientConnectorException("Unsupported message type");
        }

        return true;
    }

}
