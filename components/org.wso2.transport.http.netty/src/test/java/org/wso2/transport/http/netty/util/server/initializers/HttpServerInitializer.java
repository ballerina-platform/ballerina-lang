/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.util.server.initializers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * An initializer class for HTTP Server
 */
public abstract class HttpServerInitializer extends ChannelInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerInitializer.class);

    private SSLContext sslContext;

    @Override
    protected void initChannel(Channel channel) {

        LOG.debug("Server connection established");
        ChannelPipeline serverPipeline = channel.pipeline();
        if (sslContext != null) {
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            serverPipeline.addLast("ssl", new SslHandler(engine));
        }
        serverPipeline.addLast("serverCodec", new HttpServerCodec());
        addBusinessLogicHandler(channel);
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    protected abstract void addBusinessLogicHandler(Channel channel);
}
