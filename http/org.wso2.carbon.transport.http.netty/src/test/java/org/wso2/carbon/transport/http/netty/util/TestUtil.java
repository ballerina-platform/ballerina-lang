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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractImpl.HTTPConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.contractImpl.HTTPServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.listener.ServerConnectorBootstrap;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.fail;

/**
 * A util class to be used for tests.
 */
public class TestUtil {

    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);
//
    public static final int TEST_SERVER_PORT = 9000;
    public static final String TEST_HOST = "localhost";
//
    public static final int SERVERS_SETUP_TIME = 10000;
    public static final int SERVERS_SHUTDOWN_WAIT_TIME = 5000;
    public static final long HTTP2_RESPONSE_TIME_OUT = 30;
    public static final TimeUnit HTTP2_RESPONSE_TIME_UNIT = TimeUnit.SECONDS;
    public static List<ServerConnector> connectors;
    public static List<ServerConnectorFuture> futures;
    //
    public static final String TRANSPORT_URI = "http://localhost:8490/";

    public static void cleanUp(List<ServerConnector> serverConnectors, HTTPServer httpServer)
            throws ServerConnectorException {
        try {
            Thread.sleep(TestUtil.SERVERS_SHUTDOWN_WAIT_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }

        for (ServerConnector httpServerConnector : serverConnectors) {
            httpServerConnector.stop();
        }

        if (ConnectionManager.getInstance() != null) {
            ConnectionManager.getInstance().getTargetChannelPool().clear();
        }
        HTTPTransportContextHolder.getInstance().getBossGroup().shutdownGracefully();
        HTTPTransportContextHolder.getInstance().getWorkerGroup().shutdownGracefully();

        httpServer.shutdown();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
    }

    public static List<ServerConnector> startConnectors(TransportsConfiguration transportsConfiguration,
                                                            HTTPConnectorListener httpConnectorListener) {

        TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                        .build("src/test/resources/simple-test-config/netty-transports.yml");
        ServerBootstrapConfiguration serverBootstrapConfiguration = getServerBootstrapConfiguration(
                configuration.getTransportProperties());
        Set<ListenerConfiguration> listenerConfigurationSet = transportsConfiguration.getListenerConfigurations();

        HTTPTransportContextHolder.getInstance().setWorkerGroup(new NioEventLoopGroup());
        HTTPTransportContextHolder.getInstance().setBossGroup(new NioEventLoopGroup());

        connectors = new ArrayList<>();
        futures = new ArrayList<>();

//        ServerConnectorBootstrap serverConnectorBootstrap = new ServerConnectorBootstrap(transportsConfiguration);

//        Set<ListenerConfiguration> listenerConfigurationSet = transportsConfiguration.getListenerConfigurations();

//        HTTPClientConnector httpClientConnector =
//                new HTTPClientConnector(transportsConfiguration.getSenderConfigurations(),
//                        transportsConfiguration.getTransportProperties());
//
//        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);

//        carbonMessageProcessor.setClientConnector(httpClientConnector);

//        Thread transportRunner = new Thread(() -> {
//            try {
//                serverConnectorBootstrap.initialize();
//            } catch (Exception e) {
//                log.error("Unable to initialize Server Connector Controller ", e);
//            }
//        });
//        transportRunner.start();
//
//        try {
//            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
//        } catch (InterruptedException e) {
//            log.error("Thread Interrupted while sleeping ", e);
//        }

        HTTPConnectorFactoryImpl httpConnectorFactory = new HTTPConnectorFactoryImpl();
        listenerConfigurationSet.forEach(config -> {
//            HTTPServerConnector connector = new HTTPServerConnector(config.getId());
//            connector.setListenerConfiguration(config);
//            connector.setServerConnectorBootstrap(serverConnectorBootstrap);
//            serverConnectorBootstrap.bindInterface(connector);
//            connector.setMessageProcessor(carbonMessageProcessor);
            ServerConnector serverConnector = httpConnectorFactory.getServerConnector(serverBootstrapConfiguration,
                    config);
            ServerConnectorFuture serverConnectorFuture = serverConnector.start();
            serverConnectorFuture.setHTTPConnectorListener(httpConnectorListener);
            futures.add(serverConnectorFuture);
            connectors.add(serverConnector);
        });

        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }

        return connectors;
    }

    public static HTTPServer startHTTPServer(int port) {
        HTTPServer httpServer = new HTTPServer(port);
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                httpServer.start();
            }
        });
        serverThread.start();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
        return httpServer;
    }

    public static HTTPServer startHTTPServer(int port, String message, String contentType) {
        HTTPServer httpServer = new HTTPServer(port);
        Thread serverThread = new Thread(() -> {
            httpServer.start();
            httpServer.setMessage(message, contentType);
        });
        serverThread.start();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
        return httpServer;
    }

    public static void shutDownCarbonTransport(HTTPTransportListener httpTransportListener) {
        httpTransportListener.stop();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interuppted while sleeping ", e);
        }
    }

    public static void shutDownHttpServer(HTTPServer httpServer) {
        httpServer.shutdown();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
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
    public static void removeMessageProcessor(HTTPConnectorListener httpConnectorListener) {
//        HTTPTransportContextHolder.getInstance().removeMessageProcessor(httpConnectorListener);
    }
    public static void updateMessageProcessor(HTTPConnectorListener httpConnectorListener) {
//        HTTPClientConnector httpClientConnector =
//                new HTTPClientConnector(transportsConfiguration.getSenderConfigurations(),
//                        transportsConfiguration.getTransportProperties());
//        carbonMessageProcessor.setClientConnector(httpClientConnector);
//        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);
        futures.forEach(future -> future.setHTTPConnectorListener(httpConnectorListener));
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

}

