/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.transport.http.netty.redirect;

import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.sender.RedirectHandler;
import org.wso2.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.HttpsServer;
import org.wso2.transport.http.netty.util.server.initializers.RedirectServerInitializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.transport.http.netty.common.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.common.Constants.HTTP_SCHEME;

/**
 * Test cases for redirection scenarios
 */
public class HttpClientRedirectTestCase {

    private HttpClientConnector httpClientConnector;
    private HttpClientConnector httpsClientConnector;
    private HttpWsConnectorFactory connectorFactory;
    private TransportsConfiguration transportsConfiguration;
    private ConnectionManager connectionManager;
    private static final String FINAL_DESTINATION = "http://localhost:9092/destination";
    private static final int DESTINATION_PORT1 = 9091;
    private static final int DESTINATION_PORT2 = 9092;
    private static final int DESTINATION_PORT3 = 9093;
    private static final String URL1 = "http://www.mocky.io/v2/59d590762700000a049cd694";
    private static final String URL2 = "http://www.mocky.io/v3/59d590762700000a049cd694";

    private String testValue = "Test Message";
    private String testValueForLoopRedirect = "Test Loop";

    @BeforeClass
    public void setup() {
        transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");

        connectorFactory = new DefaultHttpWsConnectorFactory();

        SenderConfiguration senderConfiguration = HTTPConnectorUtil
                .getSenderConfiguration(transportsConfiguration, HTTP_SCHEME);
        senderConfiguration.setFollowRedirect(true);
        senderConfiguration.setMaxRedirectCount(5);

        Map<String, Object> transportProperties = HTTPConnectorUtil.getTransportProperties(transportsConfiguration);

        PoolConfiguration poolConfiguration = new PoolConfiguration();
        BootstrapConfiguration bootstrapConfig = new BootstrapConfiguration(transportProperties);
        EventLoopGroup clientEventLoopGroup = new NioEventLoopGroup(
                Util.getIntProperty(transportProperties, Constants.CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE, 4));
        this.connectionManager = new ConnectionManager(senderConfiguration, bootstrapConfig, clientEventLoopGroup);

        httpClientConnector = connectorFactory
                .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                        senderConfiguration);

        TransportsConfiguration transportsConfigurationForHttps = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");
        Set<SenderConfiguration> senderConfig = transportsConfigurationForHttps.getSenderConfigurations();
        senderConfig.forEach(config -> {
            if (config.getId().contains(Constants.HTTPS_SCHEME)) {
                config.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
                config.setTrustStoreFile(TestUtil.getAbsolutePath(config.getTrustStoreFile()));
                config.setKeyStorePassword(TestUtil.KEY_STORE_PASSWORD);
            }
        });
        httpsClientConnector = connectorFactory.createHttpClientConnector(HTTPConnectorUtil
                .getTransportProperties(transportsConfigurationForHttps), HTTPConnectorUtil
                .getSenderConfiguration(transportsConfigurationForHttps, Constants.HTTPS_SCHEME));
    }

    /**
     * Check whether, redirect request is written to the backend when a redirect response is received.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void unitTestForRedirectHandler() throws URISyntaxException, IOException {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new HttpResponseDecoder());
        embeddedChannel.pipeline().addLast(new HttpRequestEncoder());
        embeddedChannel.pipeline().addLast(new RedirectHandler(null, false,
                5, this.connectionManager));
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT,
                Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, FINAL_DESTINATION);
        embeddedChannel.attr(Constants.ORIGINAL_REQUEST)
                .set(createHttpRequest(Constants.HTTP_GET_METHOD, FINAL_DESTINATION));
        embeddedChannel.writeInbound(response);
        embeddedChannel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        assertNotNull(embeddedChannel.readOutbound());
    }

    /**
     * When the maximum redirect count reached, channel should not do any more redirects.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void unitTestForRedirectLoop() throws URISyntaxException, IOException {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new HttpResponseDecoder());
        embeddedChannel.pipeline().addLast(new HttpRequestEncoder());
        embeddedChannel.pipeline()
                .addLast(Constants.IDLE_STATE_HANDLER, new IdleStateHandler(50000, 50000, 0, TimeUnit.MILLISECONDS));
        embeddedChannel.pipeline().addLast(new RedirectHandler(null, false, 5, null, false, connectionManager));
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT,
                Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, FINAL_DESTINATION);
        embeddedChannel.attr(Constants.ORIGINAL_REQUEST)
                .set(createHttpRequest(Constants.HTTP_GET_METHOD, FINAL_DESTINATION));
        embeddedChannel.attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).set(new DefaultHttpResponseFuture());
        TargetChannel targetChannel = new TargetChannel(null, null, null, null);
        targetChannel.setChannel(embeddedChannel);
        embeddedChannel.attr(Constants.TARGET_CHANNEL_REFERENCE).set(targetChannel);
        embeddedChannel.attr(Constants.REDIRECT_COUNT).set(5);
        embeddedChannel.writeInbound(response);
        embeddedChannel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        assertNull(embeddedChannel.readOutbound());
    }

    /**
     * Unit test for redirect code 303.
     */
    @Test
    public void unitTestFor303() {
        try {
            RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
            Method method = RedirectHandler.class
                    .getDeclaredMethod("getRedirectState", String.class, Integer.TYPE, HTTPCarbonMessage.class);
            method.setAccessible(true);
            Map<String, String> returnValue = (Map<String, String>) method
                    .invoke(redirectHandler, FINAL_DESTINATION, HttpResponseStatus.SEE_OTHER.code(),
                            createHttpRequest(Constants.HTTP_POST_METHOD, FINAL_DESTINATION));
            assertEquals(Constants.HTTP_GET_METHOD, returnValue.get(Constants.HTTP_METHOD));
        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running unitTestFor300", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running unitTestFor300", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running unitTestFor300", e);
        }
    }

    /**
     * This unit test is applicable to redirect status codes 300, 305, 307 ans 308.
     */
    @Test
    public void unitTestForOriginalMethod() {
        try {
            RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
            Method method = RedirectHandler.class
                    .getDeclaredMethod("getRedirectState", String.class, Integer.TYPE, HTTPCarbonMessage.class);
            method.setAccessible(true);
            Map<String, String> returnValue = (Map<String, String>) method
                    .invoke(redirectHandler, FINAL_DESTINATION, HttpResponseStatus.TEMPORARY_REDIRECT.code(),
                            createHttpRequest(Constants.HTTP_HEAD_METHOD, FINAL_DESTINATION));
            assertEquals(Constants.HTTP_HEAD_METHOD, returnValue.get(Constants.HTTP_METHOD));
        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running unitTestForOriginalMethod", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running unitTestForOriginalMethod", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running unitTestForOriginalMethod", e);
        }
    }

    /**
     * This unit test is applicable to redirect status codes 301 and 302.
     */
    @Test
    public void unitTestForAlwaysGet() {
        try {
            RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
            Method method = RedirectHandler.class
                    .getDeclaredMethod("getRedirectState", String.class, Integer.TYPE, HTTPCarbonMessage.class);
            method.setAccessible(true);
            Map<String, String> returnValue = (Map<String, String>) method
                    .invoke(redirectHandler, FINAL_DESTINATION, HttpResponseStatus.MOVED_PERMANENTLY.code(),
                            createHttpRequest(Constants.HTTP_HEAD_METHOD, FINAL_DESTINATION));
            assertEquals(Constants.HTTP_GET_METHOD, returnValue.get(Constants.HTTP_METHOD));
        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running unitTestForAlwaysGet", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running unitTestForAlwaysGet", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running unitTestForAlwaysGet", e);
        }
    }

    /**
     * Original request and the redirect request goes to two different domains.
     */
    @Test
    public void unitTestToDetermineCrossDomainURLs() {
        RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
        try {
            Method method = RedirectHandler.class
                    .getDeclaredMethod("isCrossDomain", String.class, HTTPCarbonMessage.class);
            method.setAccessible(true);
            boolean isCrossDomainURL = (boolean) method
                    .invoke(redirectHandler, FINAL_DESTINATION, createHttpRequest(Constants.HTTP_HEAD_METHOD, URL1));
            assertEquals(true, isCrossDomainURL);
        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running unitTestToDetermineCrossDomainURLs",
                    e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException(
                    "InvocationTargetException occurred while running unitTestToDetermineCrossDomainURLs", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running unitTestToDetermineCrossDomainURLs",
                    e);
        }
    }

    /**
     * Original request and the redirect request uses the same domain.
     */
    @Test
    public void unitTestForSameDomain() {
        RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
        try {
            Method method = RedirectHandler.class
                    .getDeclaredMethod("isCrossDomain", String.class, HTTPCarbonMessage.class);
            method.setAccessible(true);
            boolean isCrossDomainURL = (boolean) method
                    .invoke(redirectHandler, URL1, createHttpRequest(Constants.HTTP_HEAD_METHOD, URL2));
            assertEquals(false, isCrossDomainURL);
        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running unitTestForSameDomain", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running unitTestForSameDomain", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running unitTestForSameDomain", e);
        }
    }

    /**
     * Check whether the redirect url is built properly when relative path starts with "/"
     */
    @Test
    public void relativePathStartsWithSlash() {
        RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
        try {
            Method method = RedirectHandler.class
                    .getDeclaredMethod("buildRedirectURL", String.class, String.class, String.class, String.class,
                            Integer.class);
            method.setAccessible(true);
            String redirectUrl = (String) method
                    .invoke(redirectHandler, "/test", "/redirect1", "https", "localhost", 8080);
            assertEquals("https://localhost:8080/redirect1", redirectUrl);

        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running relativePathStartsWithSlash", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running relativePathStartsWithSlash", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running relativePathStartsWithSlash", e);
        }
    }

    /**
     * Check whether the redirect url is built properly when relative path ends with "/"
     */
    @Test
    public void relativePathEndsWithSlash() {
        RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
        try {
            Method method = RedirectHandler.class
                    .getDeclaredMethod("buildRedirectURL", String.class, String.class, String.class, String.class,
                            Integer.class);
            method.setAccessible(true);
            String redirectUrl = (String) method
                    .invoke(redirectHandler, "/test", "redirect1/", "https", "localhost", 8080);
            assertEquals("https://localhost:8080/test/redirect1/", redirectUrl);

        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running relativePathEndsWithSlash", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running relativePathEndsWithSlash", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running relativePathEndsWithSlash", e);
        }
    }

    /**
     * Check whether the redirect url is built properly when relative path doesn't contain any slashes.
     */
    @Test
    public void justRelativePathName() {
        RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
        try {
            Method method = RedirectHandler.class
                    .getDeclaredMethod("buildRedirectURL", String.class, String.class, String.class, String.class,
                            Integer.class);
            method.setAccessible(true);
            String redirectUrl = (String) method
                    .invoke(redirectHandler, "/test", "redirect1", "https", "localhost", 8080);
            assertEquals("https://localhost:8080/test/redirect1", redirectUrl);

        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running justRelativePathName", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running justRelativePathName", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running justRelativePathName", e);
        }
    }

    /**
     * Check whether the redirect url is built properly when request path ends with "/"
     */
    @Test
    public void requestPathEndsWithSlash() {
        RedirectHandler redirectHandler = new RedirectHandler(null, false, 0, this.connectionManager);
        try {
            Method method = RedirectHandler.class
                    .getDeclaredMethod("buildRedirectURL", String.class, String.class, String.class, String.class,
                            Integer.class);
            method.setAccessible(true);
            String redirectUrl = (String) method
                    .invoke(redirectHandler, "test/", "/redirect1", "https", "localhost", 8080);
            assertEquals("https://localhost:8080/redirect1", redirectUrl);

        } catch (NoSuchMethodException e) {
            TestUtil.handleException("NoSuchMethodException occurred while running requestPathEndsWithSlash", e);
        } catch (InvocationTargetException e) {
            TestUtil.handleException("InvocationTargetException occurred while running requestPathEndsWithSlash", e);
        } catch (IllegalAccessException e) {
            TestUtil.handleException("IllegalAccessException occurred while running requestPathEndsWithSlash", e);
        }
    }

    /**
     * Test for single redirection in cross domain situation where the location is defined as an absolute path.
     */
    @Test
    public void integrationTestForSingleRedirect() {
        try {
            //This server starts on port 9092 and give testValue as an output
            HttpServer httpServer = TestUtil.startHTTPServer(DESTINATION_PORT2,
                    new RedirectServerInitializer(testValue, Constants.TEXT_PLAIN, 200, null, 0));
            //This server starts on port 9091 and give a redirect response to 9092
            HttpServer redirectServer = TestUtil.startHTTPServer(DESTINATION_PORT1,
                    new RedirectServerInitializer(testValue, Constants.TEXT_PLAIN,
                            HttpResponseStatus.TEMPORARY_REDIRECT.code(), FINAL_DESTINATION, 0));

            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            //Send a request to server that runs on port 9091 and it should redirect to server that runs on port 9092
            HttpResponseFuture responseFuture = httpClientConnector
                    .send(createHttpCarbonRequest(null, DESTINATION_PORT1, HTTP_SCHEME));
            responseFuture.setHttpConnectorListener(listener);

            latch.await(60, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            //Output should match with the response given by 9000 server
            assertEquals(testValue, result);
            redirectServer.shutdown();
            httpServer.shutdown();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running singleRedirectionTest", e);
        }
    }

    /**
     * Test for single redirection in cross domain situation with https protocol.
     */
    @Test
    public void integrationTestForRedirectWithHttps() {
        try {
            //This server starts on port 9092 and give testValue as an output
            HttpsServer httpServer = TestUtil.startHttpsServer(DESTINATION_PORT2,
                    new RedirectServerInitializer(testValue, Constants.TEXT_PLAIN, 200, null, 0));
            //This server starts on port 9091 and give a redirect response to 9092
            HttpsServer redirectServer = TestUtil.startHttpsServer(DESTINATION_PORT1,
                    new RedirectServerInitializer(testValue, Constants.TEXT_PLAIN,
                            HttpResponseStatus.TEMPORARY_REDIRECT.code(), FINAL_DESTINATION, 0));

            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            //Send a request to server that runs on port 9091 and it should redirect to server that runs on port 9092
            HttpResponseFuture responseFuture = httpsClientConnector
                    .send(createHttpCarbonRequest(null, DESTINATION_PORT1, HTTPS_SCHEME));
            responseFuture.setHttpConnectorListener(listener);

            latch.await(60, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            //Output should match with the response given by 9092 server
            assertEquals(testValue, result);
            redirectServer.shutdown();
            httpServer.shutdown();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running integrationTestForRedirectWithHttps", e);
        }
    }

    /**
     * Integration test for redirection loop.
     */
    @Test
    public void integrationTestForRedirectLoop() {
        try {
            SenderConfiguration senderConfiguration = HTTPConnectorUtil
                    .getSenderConfiguration(transportsConfiguration, HTTP_SCHEME);
            senderConfiguration.setFollowRedirect(true);
            senderConfiguration.setMaxRedirectCount(1); //Max redirect count is 1

            HttpClientConnector httpClientConnector = connectorFactory
                    .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                            senderConfiguration);

            //Server1 starts on port 9091 and give a redirect response that should goto port 9092 server
            HttpServer redirectServer1 = TestUtil.startHTTPServer(DESTINATION_PORT1,
                    new RedirectServerInitializer(testValue, Constants.TEXT_PLAIN,
                            HttpResponseStatus.TEMPORARY_REDIRECT.code(), FINAL_DESTINATION, 0));

            //Server2 starts on port 9092 and give a redirect response to some other domain
            HttpServer redirectServer2 = TestUtil.startHTTPServer(DESTINATION_PORT2,
                    new RedirectServerInitializer(testValueForLoopRedirect, Constants.TEXT_PLAIN,
                            HttpResponseStatus.TEMPORARY_REDIRECT.code(), URL1, 0));

            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector
                    .send(createHttpCarbonRequest(null, DESTINATION_PORT1, HTTP_SCHEME));
            responseFuture.setHttpConnectorListener(listener);

            latch.await(60, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            //Response should be equal to the response receive from Server 2 as there cannot be any more redirects.
            assertEquals(testValueForLoopRedirect, result);
            redirectServer1.shutdown();
            redirectServer2.shutdown();

        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testRedirectionLoop", e);
        }
    }

    /**
     * In case of a timeout during redirection, check whether the proper error response is sent to the client.
     */
    @Test
    public void integrationTestForTimeout() {
        try {

            SenderConfiguration senderConfiguration = HTTPConnectorUtil
                    .getSenderConfiguration(transportsConfiguration, HTTP_SCHEME);
            senderConfiguration.setFollowRedirect(true);
            senderConfiguration.setMaxRedirectCount(5);
            senderConfiguration.setSocketIdleTimeout(2000);

            HttpClientConnector httpClientConnector = connectorFactory
                    .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                            senderConfiguration);

            //Server starts on port 9092 and give 200 response
            HttpServer httpServer = TestUtil.startHTTPServer(DESTINATION_PORT2,
                    new RedirectServerInitializer(testValue, Constants.TEXT_PLAIN, 200, null, 0));

            //Server starts on port 9091 and give a redirect response to server on port 9092
            HttpServer redirectServer2 = TestUtil.startHTTPServer(DESTINATION_PORT1,
                    new RedirectServerInitializer(testValueForLoopRedirect, Constants.TEXT_PLAIN,
                            HttpResponseStatus.TEMPORARY_REDIRECT.code(), FINAL_DESTINATION, 3000));

            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector
                    .send(createHttpCarbonRequest(null, DESTINATION_PORT1, HTTP_SCHEME));
            responseFuture.setHttpConnectorListener(listener);

            latch.await(60, TimeUnit.SECONDS);

            Throwable response = listener.getHttpErrorMessage();
            assertNotNull(response);
            String result = response.getMessage();
            assertEquals(result, Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_READING_INBOUND_RESPONSE);
            redirectServer2.shutdown();
            httpServer.shutdown();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running integrationTestForTimeout", e);
        }
    }

    /**
     * Redirect is on, but the first response received is not a redirect
     */
    @Test
    public void redirectOnButNotARedirect() {
        try {
            //This server starts on port 9092 and give testValue as an output
            HttpServer httpServer = TestUtil.startHTTPServer(DESTINATION_PORT3,
                    new RedirectServerInitializer(testValue, Constants.TEXT_PLAIN, 200, null, 0));
            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            //Send a request to server that runs on port 9092
            HttpResponseFuture responseFuture = httpClientConnector
                    .send(createHttpCarbonRequest(null, DESTINATION_PORT3, HTTP_SCHEME));
            responseFuture.setHttpConnectorListener(listener);

            latch.await(60, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            //Output should match with the response given by 9092 server
            assertEquals(testValue, result);
            httpServer.shutdown();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running singleRedirectionTest", e);
        }
    }

    private HTTPCarbonMessage createHttpCarbonRequest(String requestUrl, int destinationPort, String protocol) {
        HTTPCarbonMessage msg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""));
        msg.setProperty(Constants.HTTP_PORT, destinationPort);
        msg.setProperty(Constants.PROTOCOL, protocol);
        msg.setProperty(Constants.HTTP_HOST, "localhost");
        msg.setProperty(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
        if (requestUrl != null) {
            msg.setProperty(Constants.REQUEST_URL, requestUrl);
        }
        msg.completeMessage();
        return msg;
    }

    private HTTPCarbonMessage createHttpRequest(String method, String location) {
        URL locationUrl = null;
        try {
            locationUrl = new URL(location);
        } catch (MalformedURLException e) {
            TestUtil.handleException("MalformedURLException occurred while running unitTestForRedirectHandler ", e);
        }

        HttpMethod httpMethod = new HttpMethod(method);
        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, ""));
        httpCarbonRequest.setProperty(Constants.HTTP_PORT, locationUrl.getPort());
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HTTP_HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, method);
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.getPath());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.getPath());

        httpCarbonRequest.setHeader(HttpHeaderNames.HOST.toString(), locationUrl.getHost());
        httpCarbonRequest.setHeader(Constants.HTTP_PORT, Integer.toString(locationUrl.getPort()));
        httpCarbonRequest.completeMessage();
        return httpCarbonRequest;
    }
}
