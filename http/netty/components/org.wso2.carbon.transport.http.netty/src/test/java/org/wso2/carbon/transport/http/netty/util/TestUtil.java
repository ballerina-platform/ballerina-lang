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
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.sender.HTTPSender;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * A util class to be used for tests
 */
public class TestUtil {

    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

    public static final int TEST_SERVER_PORT = 9000;
    public static final int TEST_ESB_PORT = 8490;
    public static final String TEST_HOST = "localhost";

    public static final int RESPONSE_WAIT_TIME = 10000;
    public static final int SERVERS_SETUP_TIME = 10000;
    public static final int SERVERS_SHUTDOWN_WAIT_TIME = 5000;

    public static final String TRANSPORT_URI = "http://localhost:8490/";

    public static void cleanUp(HTTPTransportListener httpTransportListener, HTTPServer httpServer) {
        try {
            Thread.sleep(TestUtil.SERVERS_SHUTDOWN_WAIT_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
        httpTransportListener.stop();
        httpServer.shutdown();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
    }

    public static HTTPTransportListener startCarbonTransport(ListenerConfiguration listenerConfiguration,
            SenderConfiguration senderConfiguration, CarbonMessageProcessor carbonMessageProcessor) {
        Set<ListenerConfiguration> interfacesSet = new HashSet<>();
        Set<TransportProperty> transportProperties = new HashSet<>();
        Set<SenderConfiguration> senderConfigurationSet = new HashSet<>();
        senderConfigurationSet.add(senderConfiguration);
        interfacesSet.add(listenerConfiguration);
        int bossGroupSize = Runtime.getRuntime().availableProcessors();
        int workerGroupSize = Runtime.getRuntime().availableProcessors() * 2;
        TransportProperty transportProperty = new TransportProperty();
        transportProperty.setName(Constants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE);
        transportProperty.setValue(bossGroupSize);
        TransportProperty workerGroup = new TransportProperty();
        workerGroup.setName(Constants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE);
        workerGroup.setValue(workerGroupSize);
        transportProperties.add(transportProperty);
        transportProperties.add(workerGroup);
        transportProperties.add(workerGroup);
        HTTPTransportListener httpTransportListener = new HTTPTransportListener(transportProperties, interfacesSet);
        TransportSender transportSender = new HTTPSender(senderConfigurationSet, transportProperties);
        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);
        carbonMessageProcessor.setTransportSender(transportSender);
        Thread transportRunner = new Thread(() -> {
            try {
                httpTransportListener.start();
            } catch (Exception e) {
                log.error("Unable to start Netty Listener ", e);
            }
        });
        transportRunner.start();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
        return httpTransportListener;
    }

    public static HTTPTransportListener startCarbonTransport(TransportsConfiguration transportsConfiguration,
            CarbonMessageProcessor carbonMessageProcessor) {

        HTTPTransportListener httpTransportListener = new HTTPTransportListener(
                transportsConfiguration.getTransportProperties(), transportsConfiguration.getListenerConfigurations());

        HTTPSender httpSender = new HTTPSender(transportsConfiguration.getSenderConfigurations(),
                transportsConfiguration.getTransportProperties());

        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);

        carbonMessageProcessor.setTransportSender(httpSender);

        Thread transportRunner = new Thread(() -> {
            try {
                httpTransportListener.start();
            } catch (Exception e) {
                log.error("Unable to start Netty Listener ", e);
            }
        });

        transportRunner.start();
        try {
            Thread.sleep(TestUtil.SERVERS_SETUP_TIME);
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
        return httpTransportListener;
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

    public static void updateMessageProcessor(CarbonMessageProcessor carbonMessageProcessor,
            TransportsConfiguration transportsConfiguration) {

        HTTPSender httpSender = new HTTPSender(transportsConfiguration.getSenderConfigurations(),
                transportsConfiguration.getTransportProperties());

        carbonMessageProcessor.setTransportSender(httpSender);
        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);
    }

}

