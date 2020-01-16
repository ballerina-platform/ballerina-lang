/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.test.util.websocket.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslHandler;
import org.ballerinalang.test.util.TestUtils;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Initializer for WebSocket server for Testing.
 */
public class WebSocketRemoteServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";
    private boolean sslEnabled;

    public WebSocketRemoteServerInitializer(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    @Override
    public void initChannel(SocketChannel ch)
            throws IOException, UnrecoverableKeyException, KeyManagementException,
                   NoSuchAlgorithmException, KeyStoreException, CertificateException {
        ChannelPipeline pipeline = ch.pipeline();
        SSLEngine sslEngine = createSSLContextFromKeystores().createSSLEngine();
        sslEngine.setUseClientMode(false);

        if (sslEnabled) {
            pipeline.addLast(new SslHandler(sslEngine));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(8192));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        WebSocketHeadersHandler headersHandler = new WebSocketHeadersHandler();
        pipeline.addLast(headersHandler);
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, "xml, json", true));
        WebSocketRemoteServerFrameHandler frameHandler = new WebSocketRemoteServerFrameHandler(headersHandler);
        pipeline.addLast(frameHandler);
    }

    public SSLContext createSSLContextFromKeystores()
            throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, IOException,
                   KeyManagementException, CertificateException {
        KeyManager[] keyManagers = null;
        KeyStore ks = TestUtils.getKeyStore(new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "security" +
                                 File.separator + "keystore" + File.separator + "ballerinaKeystore.p12"));
        // Set up key manager factory to use our key store
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        if (ks != null) {
            kmf.init(ks, "ballerina".toCharArray());
            keyManagers = kmf.getKeyManagers();
        }
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, null, null);
        return sslContext;
    }
}
