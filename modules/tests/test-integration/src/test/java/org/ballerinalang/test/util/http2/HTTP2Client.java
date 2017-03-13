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
package org.ballerinalang.test.util.http2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import org.ballerinalang.test.util.TestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An HTTP2 client that allows you to send HTTP2 frames to a server. Inbound and outbound frames are
 * logged. When run from the command-line, sends a single HEADERS frame to the server and gets back
 * a "Hello World" response.
 */
public class HTTP2Client {

    private static final Logger log = LoggerFactory.getLogger(HTTP2Client.class);

    private AtomicInteger streamId = new AtomicInteger(1);
    private Channel channel;
    private EventLoopGroup workerGroup;
    private HTTP2ResponseHandler responseHandler;
    private HttpScheme scheme;
    private AsciiString hostName;
    public HTTP2Client(boolean ssl, String host, int port) throws Exception {
        try {
            final SslContext sslCtx;
            if (ssl) {
                SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
                sslCtx = SslContextBuilder.forClient()
                        .sslProvider(provider)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                Protocol.ALPN,
                                // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                                SelectorFailureBehavior.NO_ADVERTISE,
                                // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                                SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2,
                                ApplicationProtocolNames.HTTP_1_1))
                        .build();
            } else {
                sslCtx = null;
            }
            workerGroup = new NioEventLoopGroup();
            HTTP2ClientInitializer initializer = new HTTP2ClientInitializer(sslCtx, Integer.MAX_VALUE);

            // Configure the client.
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(host, port);
            b.handler(initializer);

            // Start the client.
            channel = b.connect().syncUninterruptibly().channel();
            log.info("Connected to [" + host + ':' + port + ']');

            // Wait for the HTTP/2 upgrade to occur.
            HTTP2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
            http2SettingsHandler.awaitSettings(TestConstant.HTTP2_RESPONSE_TIME_OUT, TestConstant
                    .HTTP2_RESPONSE_TIME_UNIT);
            responseHandler = initializer.responseHandler();
            scheme = ssl ? HttpScheme.HTTPS : HttpScheme.HTTP;
            hostName = new AsciiString(host + ':' + port);
        } catch (Exception ex) {
            log.error("Error while initializing http2 client " + ex);
            this.close();
        }
    }

    public int send(FullHttpRequest request) throws Exception {
        // Configure ssl.
        int currentStreamId = streamId.addAndGet(2);
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
        responseHandler.put(currentStreamId, channel.write(request), channel.newPromise());
        channel.flush();
        log.info("Finished HTTP/2 request");
        return currentStreamId;
    }

    public void close() {
        // Wait until the connection is closed.
        if (channel != null && channel.isActive()) {
            channel.close().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public FullHttpResponse getResponse(int streamId) {
        return responseHandler.getResponse(streamId);
    }
}
