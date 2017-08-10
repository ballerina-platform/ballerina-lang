/*
 *  Copyright (c) 2017 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.RequestSizeValidationConfiguration;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.listener.http2.HTTPProtocolNegotiationHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLEngine;

/**
 * A class that responsible for build server side channels.
 */
public class HTTPServerChannelInitializer extends ChannelInitializer<SocketChannel>
        implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(HTTPServerChannelInitializer.class);

    private ConnectionManager connectionManager;
    private ListenerConfiguration listenerConfiguration;
    private ServerConnectorFuture serverConnectorFuture;
    private SSLEngine sslEngine;
    private int socketIdleTimeout;

    public HTTPServerChannelInitializer(ListenerConfiguration listenerConfiguration) {
        this.listenerConfiguration = listenerConfiguration;
    }

//    public void registerListenerConfig(ListenerConfiguration listenerConfiguration, SslContext sslContext) {
//        listenerConfigurationMap.put(listenerConfiguration.getPort(), listenerConfiguration);
//        sslContextMap.put(listenerConfiguration.getPort(), sslContext);
//    }

//    public void unRegisterListenerConfig(ListenerConfiguration listenerConfiguration) {
//        listenerConfigurationMap.remove(listenerConfiguration.getPort());
//        sslContextMap.remove(listenerConfiguration.getPort());
//    }

    @Override
    public void setup(Map<String, String> parameters) {
    }

    public void setupConnectionManager(Map<String, Object> transportProperties) {
        try {
            ConnectionManager.init(transportProperties);
            connectionManager = ConnectionManager.getInstance();
        } catch (Exception e) {
            log.error("Error initializing the transport ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Initializing source channel pipeline");
        }
//        int port = ch.localAddress().getPort();
        ListenerConfiguration listenerConfiguration = this.listenerConfiguration;
        ChannelPipeline pipeline = ch.pipeline();
//        /*
//         * HTTP2 required ALPN Protocol support. Server is required to have ALPN protocol libraries at class path
//         * If http2 in enabled , Assume required libraries available and HTTP/1 and HTTP2 requests will be handled.
//         */
//        if (listenerConfiguration.isHttp2()) {
//            SslContext sslContext = this.sslContext;
//            if (sslContext != null) {
//                /*
//                 * In HTTP2 we have two upgrade handlers for HTTPS and HTTP
//                 * HTTP - Default upgrade handler
//                 * HTTPS - Upgrade during SSL Handshake using Application Layer Protocol negotiation .
//                 */
//                // Configure Upgrade handler for HTTP/2 requests Over TLS
//                configureHttp2TLSPipeline(ch, listenerConfiguration, sslContext);
//            } else {
//                // Configure Upgrade handler for HTTP/2 requests
//                configureHttp2Pipeline(ch, listenerConfiguration);
//            }
//        } else {
//            // Configure Pipeline to handle HTTP/1 requests if HTTP/2 not enabled.
//            if (listenerConfiguration.getSslConfig() != null) {
//                SSLEngine sslEngine = new SSLHandlerFactory(listenerConfiguration.getSslConfig()).build();
//                ch.pipeline().addLast("ssl", new SslHandler(sslEngine));
//
//            }
//            p.addLast("encoder", new HttpResponseEncoder());
//            configureHTTPPipeline(ch, listenerConfiguration);
//        }

//        if (listenerConfiguration.getSslConfig() != null) {
//            SSLEngine sslEngine = new SSLHandlerFactory(listenerConfiguration.getSslConfig()).build();
//            ch.pipeline().addLast("ssl", new SslHandler(sslEngine));
//        }
        pipeline.addLast("encoder", new HttpResponseEncoder());
        configureHTTPPipeline(pipeline, listenerConfiguration);

        if (socketIdleTimeout > 0) {
            pipeline.addBefore(
                    Constants.HTTP_SOURCE_HANDLER, Constants.IDLE_STATE_HANDLER,
                    new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, socketIdleTimeout,
                                         TimeUnit.MILLISECONDS));
        }

        if (sslEngine != null) {
            pipeline.addFirst("ssl", new SslHandler(sslEngine));
        }
    }

    /**
     * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.0
     *
     * @param ch                    Channel
     * @param listenerConfiguration HTTPConnectorListener Configuration
     */
//    private void configureHttp2Pipeline(SocketChannel ch, ListenerConfiguration listenerConfiguration) {
//        ChannelPipeline p = ch.pipeline();
//        // Add http2 upgrade decoder and upgrade handler for check http version
//        final HttpServerCodec sourceCodec = new HttpServerCodec();
//        final HttpServerUpgradeHandler.UpgradeCodecFactory upgradeCodecFactory = protocol -> {
//            if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
//                return new Http2ServerUpgradeCodec("http2-handler", new
//                        HTTP2SourceHandlerBuilder(connectionManager, listenerConfiguration).build());
//            } else {
//                return null;
//            }
//        };
//        p.addLast("encoder", sourceCodec);
//        p.addLast("http2-upgrade", new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory));
//        /**
//         * Requests will be propagated to next handlers if no upgrade has been attempted and the client is just
//         * talking HTTP.
//         */
//        configureHTTPPipeline(ch, listenerConfiguration);
//    }

    /**
     * Configure the pipeline if user sent HTTP requests
     *
     * @param pipeline                    Channel
     * @param listenerConfiguration HTTPConnectorListener Configuration
     */
    public void configureHTTPPipeline(ChannelPipeline pipeline, ListenerConfiguration listenerConfiguration) {
        // Removed the default encoder since http/2 version upgrade already added to pipeline
        if (RequestSizeValidationConfiguration.getInstance().isHeaderSizeValidation()) {
            pipeline.addLast("decoder", new CustomHttpRequestDecoder());
        } else {
            pipeline.addLast("decoder", new HttpRequestDecoder());
        }
        if (RequestSizeValidationConfiguration.getInstance().isRequestSizeValidation()) {
            pipeline.addLast("custom-aggregator", new CustomHttpObjectAggregator());
        }
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast("chunkWriter", new ChunkedWriteHandler());
        try {
//            int socketIdleTimeout = listenerConfiguration.getSocketIdleTimeout(120000);
//            p.addLast(Constants.IDLE_STATE_HANDLER,
//                    new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, socketIdleTimeout,
//                            TimeUnit.MILLISECONDS));
            pipeline.addLast(Constants.HTTP_SOURCE_HANDLER,
                             new SourceHandler(connectionManager, listenerConfiguration, this.serverConnectorFuture));
        } catch (Exception e) {
            log.error("Cannot Create SourceHandler ", e);
        }
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

//    public void setSslContext(SslContext sslContext) {
//        this.sslContext = sslContext;
//    }

//    public ServerConnectorFuture getServerConnectorFuture() {
//        return serverConnectorFuture;
//    }

    public void setServerConnectorFuture(ServerConnectorFuture serverConnectorFuture) {
        this.serverConnectorFuture = serverConnectorFuture;
    }

    public void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.socketIdleTimeout = idleTimeout;
    }
}
