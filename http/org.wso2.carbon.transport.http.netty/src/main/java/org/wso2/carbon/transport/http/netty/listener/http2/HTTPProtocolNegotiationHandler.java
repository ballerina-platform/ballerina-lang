/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.transport.http.netty.listener.http2;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.RequestSizeValidationConfiguration;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.CustomHttpObjectAggregator;
import org.wso2.carbon.transport.http.netty.listener.CustomHttpRequestDecoder;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

/**
 * {@code HTTPProtocolNegotiationHandler}  negotiates with the client if HTTP2 or HTTP is going to be used. Once
 * decided, the Netty pipeline is setup with the correct handlers for the selected protocol.
 */
public class HTTPProtocolNegotiationHandler extends ApplicationProtocolNegotiationHandler {

    private static final Logger log = LoggerFactory.getLogger(HTTPProtocolNegotiationHandler.class);
    protected ConnectionManager connectionManager;
    protected ListenerConfiguration listenerConfiguration;

    public HTTPProtocolNegotiationHandler(ConnectionManager connectionManager, ListenerConfiguration
            listenerConfiguration) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.listenerConfiguration = listenerConfiguration;
        this.connectionManager = connectionManager;
    }

    @Override
    /**
     *  Configure pipeline after SSL handshake
     */
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        // handles pipeline for HTTP/2 requests after SSL handshake
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            ctx.pipeline().addLast("http2-handler", new HTTP2SourceHandlerBuilder(connectionManager,
                    listenerConfiguration).build());
            return;
        }
        // handles pipeline for HTTP/1 requests after SSL handshake
        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            p.addLast("encoder", new HttpResponseEncoder());
            if (RequestSizeValidationConfiguration.getInstance().isHeaderSizeValidation()) {
                p.addLast("decoder", new CustomHttpRequestDecoder());
            } else {
                p.addLast("decoder", new HttpRequestDecoder());
            }
            if (RequestSizeValidationConfiguration.getInstance().isRequestSizeValidation()) {
                p.addLast("custom-aggregator", new CustomHttpObjectAggregator());
            }
            p.addLast("compressor", new HttpContentCompressor());
            p.addLast("chunkWriter", new ChunkedWriteHandler());
            try {
                // TODO: Properly fix this part once we start HTTP2 integration
                p.addLast("handler", new SourceHandler(
                        new HttpWsServerConnectorFuture(null), null));
            } catch (Exception e) {
                log.error("Cannot Create SourceHandler ", e);
            }
            return;
        }

        throw new IllegalStateException("unknown protocol: " + protocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        // Start the server connection Timer
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
    }
}

