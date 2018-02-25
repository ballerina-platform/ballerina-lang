/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.sender.http2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameLogger;
import org.wso2.transport.http.netty.config.SenderConfiguration;

import static io.netty.handler.logging.LogLevel.DEBUG;

/**
 * {@code ClientInitializer} is responsible for initializing the channel handlers
 * and setting up handlers for the initial pipeline
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private static final Http2FrameLogger logger = new Http2FrameLogger(DEBUG, ClientInitializer.class);
    // Change mode to INFO for logging frames

    private ClientOutboundHandler clientOutboundHandler;
    private ClientInboundHandler clientInboundHandler;
    private UpgradeRequestHandler upgradeRequestHandler;
    private Http2ConnectionHandler connectionHandler;
    private Http2FrameListener frameListener;
    private Http2Connection connection;

    /* Whether to skip the upgrade and directly use HTTP/2 Frames for communication */
    private boolean skipHttpToHttp2Upgrade = false;

    public ClientInitializer(SenderConfiguration senderConfiguration) {
        connection = new DefaultHttp2Connection(false);
        clientInboundHandler = new ClientInboundHandler();
        frameListener = new DelegatingDecompressorFrameListener(connection, clientInboundHandler);
        connectionHandler =
                new Http2ConnectionHandlerBuilder().
                        connection(connection).frameLogger(logger).frameListener(frameListener).build();
        clientOutboundHandler = new ClientOutboundHandler(connection, connectionHandler.encoder());
        upgradeRequestHandler = new UpgradeRequestHandler(senderConfiguration, clientOutboundHandler);
        skipHttpToHttp2Upgrade = senderConfiguration.skipHttpToHttp2Upgrade();
    }

    /**
     * Set the {@code TargetChannel} associated with the ClientInitializer
     *
     * @param targetChannel associated TargetChannel
     */
    public void setTargetChannel(TargetChannel targetChannel) {
        clientOutboundHandler.setTargetChannel(targetChannel);
        upgradeRequestHandler.setTargetChannel(targetChannel);
        clientInboundHandler.setTargetChannel(targetChannel);
    }

    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {

        if (skipHttpToHttp2Upgrade) {
            socketChannel.pipeline().addLast(connectionHandler, clientOutboundHandler);
        } else {
            configureClearTextUpgrade(socketChannel);
        }
    }

    /**
     * Get the associated {@code ClientOutboundHandler}
     *
     * @return associated ClientOutboundHandler
     */
    public ClientOutboundHandler getClientOutboundHandler() {
        return clientOutboundHandler;
    }

    private void configureClearTextUpgrade(SocketChannel socketChannel) {
        HttpClientCodec sourceCodec = new HttpClientCodec();
        Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler);
        HttpClientUpgradeHandler upgradeHandler =
                new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, Integer.MAX_VALUE);
        socketChannel.pipeline().addLast(sourceCodec, upgradeHandler, upgradeRequestHandler);
    }

}
