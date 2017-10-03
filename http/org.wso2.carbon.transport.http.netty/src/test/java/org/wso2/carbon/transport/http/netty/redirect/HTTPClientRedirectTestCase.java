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

package org.wso2.carbon.transport.http.netty.redirect;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.https.HTTPSConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.carbon.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.carbon.transport.http.netty.sender.RedirectHandler;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HttpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Test cases for redirection scenarios
 */
public class HTTPClientRedirectTestCase {

    private HttpServer httpServer;
    private HttpServer redirectServer;
    private HttpClientConnector httpClientConnector;
    HttpWsConnectorFactory connectorFactory;
    TransportsConfiguration transportsConfiguration;
    public static final String FINAL_DESTINATION = "http://localhost:9000/destination";
    public static final String RELATIVE_REDIRECT_URL = "/redirect2";
    public static final int REDIRECT_DESTINATION_PORT = 9091;

    private String testValue = "Test Message";
    private String testValueForLoopRedirect = "Test Loop";

    @BeforeClass
    public void setup() {
        transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");

        connectorFactory = new HttpWsConnectorFactoryImpl();

        SenderConfiguration senderConfiguration = HTTPConnectorUtil
                .getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME);
        senderConfiguration.setFollowRedirect(true);
        senderConfiguration.setMaxRedirectCount(5);

        httpClientConnector = connectorFactory
                .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                        senderConfiguration);

    }

    @Test
    public void unitTestForRedirectHandler() throws URISyntaxException, IOException {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new HttpResponseDecoder());
        embeddedChannel.pipeline().addLast(new HttpRequestEncoder());
        embeddedChannel.pipeline().addLast(new RedirectHandler(null, false, 5));
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT,
                Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaders.Names.LOCATION, FINAL_DESTINATION);
        embeddedChannel.attr(Constants.ORIGINAL_REQUEST).set(createHttpRequestForFinalRedirectLocation());
        embeddedChannel.writeInbound(response);
        embeddedChannel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        assertNotNull(embeddedChannel.readOutbound());
    }

    /**
     * Test for single redirection in cross domain situation where the location is defined as an absolute path.
     */
    @Test
    public void singleRedirectionTest() {
        try {

            httpServer = TestUtil.startHTTPServer(TestUtil.TEST_HTTP_SERVER_PORT, testValue, Constants.TEXT_PLAIN);

            redirectServer = TestUtil
                    .startHTTPServerForRedirect(REDIRECT_DESTINATION_PORT, testValue, Constants.TEXT_PLAIN,
                            HttpResponseStatus.TEMPORARY_REDIRECT.code(), FINAL_DESTINATION);

            CountDownLatch latch = new CountDownLatch(1);
            HTTPSConnectorListener listener = new HTTPSConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(createHttpCarbonRequest(null));
            responseFuture.setHttpConnectorListener(listener);

            latch.await(60, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));

            assertEquals(testValue, result);
            redirectServer.shutdown();
            httpServer.shutdown();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running singleRedirectionTest", e);
        }
    }

    /**
     * Test for redirection loop. Cross domain false situation with a relative path.
     */
    @Test
    public void testRedirectionLoop() {
        try {

            redirectServer = TestUtil.startHTTPServerForRedirect(REDIRECT_DESTINATION_PORT, testValueForLoopRedirect,
                    Constants.TEXT_PLAIN, HttpResponseStatus.TEMPORARY_REDIRECT.code(), RELATIVE_REDIRECT_URL);

            CountDownLatch latch = new CountDownLatch(1);
            HTTPSConnectorListener listener = new HTTPSConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector
                    .send(createHttpCarbonRequest(RELATIVE_REDIRECT_URL));
            responseFuture.setHttpConnectorListener(listener);

            latch.await(60, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));

            assertEquals(testValueForLoopRedirect, result);
            redirectServer.shutdown();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testRedirectionLoop", e);
        }
    }

    private HTTPCarbonMessage createHttpCarbonRequest(String requestUrl) {
        HTTPCarbonMessage msg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""));
        msg.setProperty(Constants.PORT, REDIRECT_DESTINATION_PORT);
        msg.setProperty(Constants.PROTOCOL, "http");
        msg.setProperty(Constants.HOST, "localhost");
        msg.setProperty(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
        if (requestUrl != null) {
            msg.setProperty(Constants.REQUEST_URL, requestUrl);
        }
        msg.setEndOfMsgAdded(true);
        return msg;
    }

    private HTTPCarbonMessage createHttpRequestForFinalRedirectLocation() {
        URL locationUrl = null;
        try {
            locationUrl = new URL(FINAL_DESTINATION);
        } catch (MalformedURLException e) {
            TestUtil.handleException("MalformedURLException occurred while running unitTestForRedirectHandler ", e);
        }

        HttpMethod httpMethod = new HttpMethod(Constants.HTTP_GET_METHOD);
        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, ""));
        httpCarbonRequest.setProperty(Constants.PORT, locationUrl.getPort());
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.getPath());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.getPath());

        httpCarbonRequest.setHeader(Constants.HOST, locationUrl.getHost());
        httpCarbonRequest.setHeader(Constants.PORT, Integer.toString(locationUrl.getPort()));
        httpCarbonRequest.setEndOfMsgAdded(true);
        return httpCarbonRequest;
    }

}
