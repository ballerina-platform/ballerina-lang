/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.common.Util;

import javax.net.ssl.SSLEngine;

/**
 * A handler to check whether TLS handshake has been completed. Rest of the handlers will be added to the pipeline
 * once this becomes successful.
 */
public class SslHandshakeCompletionHandlerForServer extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SslHandshakeCompletionHandlerForServer.class);

    private HttpServerChannelInitializer httpServerChannelInitializer;
    private ChannelPipeline serverPipeline;
    private SSLEngine sslEngine;

    SslHandshakeCompletionHandlerForServer(HttpServerChannelInitializer httpServerChannelInitializer,
            ChannelPipeline serverPipeline, SSLEngine sslEngine) {
        this.httpServerChannelInitializer = httpServerChannelInitializer;
        this.serverPipeline = serverPipeline;
        this.sslEngine = sslEngine;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SslHandshakeCompletionEvent) {

            SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent) evt;

            if (event.isSuccess()) {
                Util.setMutualSslStatus(ctx, sslEngine);
                this.httpServerChannelInitializer.configureHttpPipeline(serverPipeline, Constants.HTTP_SCHEME);
                ctx.pipeline().remove(this);
                ctx.fireChannelActive();
            } else {
                ctx.close();
            }
        }
     }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("Error while SSL handshake: " + cause.getMessage());
    }
}

