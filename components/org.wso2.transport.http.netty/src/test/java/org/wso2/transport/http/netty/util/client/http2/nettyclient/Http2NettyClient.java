/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.transport.http.netty.util.client.http2.nettyclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Test HTTP/2 Client.
 */
public final class Http2NettyClient {
    private static final Logger LOG = LoggerFactory.getLogger(Http2NettyClient.class);

    private static final String HOST = "127.0.0.1";
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private HttpResponseHandler responseHandler;
    private Channel channel;
    private int port = 9000;

    public void startClient(int port, boolean decompressorEnabled) throws Exception {

        this.port = port;
        Http2ClientInitializer initializer = new Http2ClientInitializer(Integer.MAX_VALUE, decompressorEnabled);

        // Configure the client.
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.remoteAddress(HOST, port);
        bootstrap.handler(initializer);

        // Start the client.
        channel = bootstrap.connect().syncUninterruptibly().channel();
        LOG.debug("Connected to [" + HOST + ':' + port + ']');

        // Wait for the HTTP/2 upgrade to occur.
        Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
        http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);
        responseHandler = initializer.responseHandler();

    }

    public HttpResponseHandler sendPostRequest(String payload, int streamId, String acceptEncoding) {
        AsciiString hostName = new AsciiString(HOST + ':' + port);
        LOG.debug("Sending POST request...");
        // Create a simple POST request with a body.
        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/dummy",
                                                             wrappedBuffer(payload.getBytes(CharsetUtil.UTF_8)));
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HttpScheme.HTTP.name());
        if (acceptEncoding != null) {
            request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, acceptEncoding);
        }
        responseHandler.put(streamId, channel.write(request), channel.newPromise());
        flushChannel();
        return responseHandler;

    }

    public HttpResponseHandler sendGetRequest(int streamId, String acceptEncoding) {
        LOG.debug("Sending GET request...");
        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/dummy");
        request.headers().add(HttpHeaderNames.HOST, HOST);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HttpScheme.HTTP.name());
        if (acceptEncoding != null) {
            request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, acceptEncoding);
        }
        responseHandler.put(streamId, channel.write(request), channel.newPromise());
        flushChannel();
        return responseHandler;
    }

    private void flushChannel() {
        channel.flush();
        responseHandler.awaitResponses(5, TimeUnit.SECONDS);
        LOG.debug("Finished HTTP/2 request(s)");
    }

    public void closeChannel() {
        // Wait until the connection is closed.
        channel.close().syncUninterruptibly();
        workerGroup.shutdownGracefully();
    }
}
