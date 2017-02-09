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

package org.wso2.carbon.transport.http.netty.util.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * An initializer class for HTTP Server
 */
public class HTTPServerInitializer extends ChannelInitializer {

    private SSLContext sslContext;
    private String message;
    private String contentType;
    private int responseCode = 200;

    @Override
    protected void initChannel(Channel channel) throws Exception {

        ChannelPipeline p = channel.pipeline();
        if (sslContext != null) {
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            p.addLast("ssl", new SslHandler(engine));
        }

        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        HTTPServerHandler httpServerHandler = new HTTPServerHandler();
        httpServerHandler.setMessage(message, contentType);
        httpServerHandler.setResponseStatusCode(responseCode);
        p.addLast("handler", httpServerHandler);
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setMessage(String message, String contentType) {
        this.message = message;
        this.contentType = contentType;
    }
}
