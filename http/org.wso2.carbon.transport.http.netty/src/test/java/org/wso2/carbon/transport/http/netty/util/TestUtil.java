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

package org.wso2.carbon.transport.http.netty.util;

import com.google.common.io.ByteStreams;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.carbon.transport.http.netty.util.server.HttpServer;
import org.wso2.carbon.transport.http.netty.util.server.HttpsServer;
import org.wso2.carbon.transport.http.netty.util.server.ServerThread;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.fail;

/**
 * A util class to be used for tests.
 */
public class TestUtil {

    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

    public static final int TEST_HTTP_SERVER_PORT = 9000;
    public static final int TEST_HTTPS_SERVER_PORT = 9004;
    public static final int TEST_DEFAULT_INTERFACE_PORT = 8490;
    public static final int TEST_ALTER_INTERFACE_PORT = 8590;
    public static final int TEST_REMOTE_WS_SERVER_PORT = 9010;
    public static final String TEST_HOST = "localhost";
    public static final long HTTP2_RESPONSE_TIME_OUT = 30;
    public static final TimeUnit HTTP2_RESPONSE_TIME_UNIT = TimeUnit.SECONDS;
    private static List<ServerConnector> connectors;
    private static List<ServerConnectorFuture> futures;

    public static void cleanUp(List<ServerConnector> serverConnectors, HttpServer httpServer)
            throws ServerConnectorException {
        for (ServerConnector httpServerConnector : serverConnectors) {
            httpServerConnector.stop();
        }

        if (ConnectionManager.getInstance() != null) {
            ConnectionManager.getInstance().getTargetChannelPool().clear();
        }

        try {
            HTTPTransportContextHolder.getInstance().getBossGroup().shutdownGracefully().sync();
            HTTPTransportContextHolder.getInstance().getWorkerGroup().shutdownGracefully().sync();
            httpServer.shutdown();
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
    }

    public static List<ServerConnector> startConnectors(TransportsConfiguration transportsConfiguration,
                                                            HttpConnectorListener httpConnectorListener) {

        TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                        .build("src/test/resources/simple-test-config/netty-transports.yml");
        ServerBootstrapConfiguration serverBootstrapConfiguration = getServerBootstrapConfiguration(
                configuration.getTransportProperties());
        Set<ListenerConfiguration> listenerConfigurationSet = transportsConfiguration.getListenerConfigurations();

        HTTPTransportContextHolder.getInstance().setWorkerGroup(new NioEventLoopGroup());
        HTTPTransportContextHolder.getInstance().setBossGroup(new NioEventLoopGroup());

        connectors = new ArrayList<>();
        futures = new ArrayList<>();

        HttpWsConnectorFactoryImpl httpConnectorFactory = new HttpWsConnectorFactoryImpl();
        listenerConfigurationSet.forEach(config -> {
            ServerConnector serverConnector = httpConnectorFactory.createServerConnector(serverBootstrapConfiguration,
                    config);
            ServerConnectorFuture serverConnectorFuture = serverConnector.start();
            serverConnectorFuture.setHttpConnectorListener(httpConnectorListener);
            try {
                serverConnectorFuture.sync();
            } catch (InterruptedException e) {
                log.error("Thread Interrupted while sleeping ", e);
            }
            futures.add(serverConnectorFuture);
            connectors.add(serverConnector);
        });

        return connectors;
    }

    public static HttpServer startHTTPServer(int port, ChannelInitializer channelInitializer) {
        HttpServer httpServer = new HttpServer(port, channelInitializer);
        CountDownLatch latch = new CountDownLatch(1);

        ServerThread serverThread = new ServerThread(latch, httpServer);
        try {
            serverThread.start();
            latch.await();
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
        return httpServer;
    }

    public static HttpsServer startHttpsServer(int port, ChannelInitializer channelInitializer) {
        HttpsServer httpServer = new HttpsServer(port, channelInitializer);
        CountDownLatch latch = new CountDownLatch(1);
        ServerThread serverThread = new ServerThread(latch, httpServer);
        try {
            serverThread.start();
            latch.await();
        } catch (Exception e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
        return httpServer;
    }

    public static String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }

    public static void writeContent(HttpURLConnection urlConn, String content) throws IOException {
        urlConn.getOutputStream().write(content.getBytes(Charsets.UTF_8));
        urlConn.getOutputStream().flush();
    }

    public static HttpURLConnection request(URI baseURI, String path, String method, boolean keepAlive)
            throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST.name()) || method.equals(HttpMethod.PUT.name())) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty("Connection", "Keep-Alive");
        }

        return urlConn;
    }

    public static void setHeader(HttpURLConnection urlConnection, String key, String value) {
        urlConnection.setRequestProperty(key, value);
    }

    public static void updateMessageProcessor(HttpConnectorListener httpConnectorListener) {
        futures.forEach(future -> future.setHttpConnectorListener(httpConnectorListener));
    }

    public static void handleException(String msg, Exception ex) {
        log.error(msg, ex);
        fail(msg);
    }

    private static ServerBootstrapConfiguration getServerBootstrapConfiguration(
            Set<TransportProperty> transportPropertiesSet) {
        Map<String, Object> transportProperties = new HashMap<>();

        if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
            transportProperties = transportPropertiesSet.stream().collect(
                    Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));
        }
        // Create Bootstrap Configuration from listener parameters
        ServerBootstrapConfiguration.createBootStrapConfiguration(transportProperties);
        return ServerBootstrapConfiguration.getInstance();
    }

    public static TransportsConfiguration getConfiguration(String configFileLocation) {
        TransportsConfiguration transportsConfiguration;

        File file = new File(TestUtil.class.getResource(configFileLocation).getFile());
        if (file.exists()) {
            try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)) {
                Yaml yaml = new Yaml(new CustomClassLoaderConstructor
                                             (TransportsConfiguration.class,
                                              TransportsConfiguration.class.getClassLoader()));
                yaml.setBeanAccess(BeanAccess.FIELD);
                transportsConfiguration = yaml.loadAs(in, TransportsConfiguration.class);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Error while loading " + configFileLocation + " configuration file", e);
            }
        } else { // return a default config
            log.warn("Netty transport configuration file not found in: " + configFileLocation +
                             " ,hence using default configuration");
            transportsConfiguration = TransportsConfiguration.getDefault();
        }

        return transportsConfiguration;
    }
}

