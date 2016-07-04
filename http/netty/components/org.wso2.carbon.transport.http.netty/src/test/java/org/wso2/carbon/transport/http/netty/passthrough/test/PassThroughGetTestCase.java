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

package org.wso2.carbon.transport.http.netty.passthrough.test;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.NettyListener;
import org.wso2.carbon.transport.http.netty.sender.NettySender;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.client.HTTPClient;
import org.wso2.carbon.transport.http.netty.util.client.Request;
import org.wso2.carbon.transport.http.netty.util.client.ResponseCallback;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A test class for passthrough transport
 */
public class PassThroughGetTestCase {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PassThroughGetTestCase.class);

    private NettyListener nettyListener;
    private CarbonMessageProcessor messageProcessor;
    private TransportSender transportSender;

    private static final String testValue = "Test Message";

    private HTTPServer httpServer;

    @Before
    public void setUp() {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost(TestUtil.TEST_HOST);
        listenerConfiguration.setId("test-listener");
        listenerConfiguration.setExecHandlerThreadPoolSize(Runtime.getRuntime().availableProcessors());
        listenerConfiguration.setPort(TestUtil.TEST_ESB_PORT);
        SenderConfiguration senderConfiguration = new SenderConfiguration("passthrough-sender");
        nettyListener = new NettyListener(listenerConfiguration);
        messageProcessor = new PassthroughMessageProcessor();
        transportSender = new NettySender(senderConfiguration);
        NettyTransportContextHolder.getInstance().addMessageProcessor(messageProcessor);
        messageProcessor.setTransportSender(transportSender);
        httpServer = new HTTPServer(TestUtil.TEST_SERVER_PORT);

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                httpServer.start();
                httpServer.setMessage(testValue, "text/plain");
            }
        });
        serverThread.start();
        Thread transportRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                nettyListener.start();
            }
        });
        transportRunner.start();
        try {
            Thread.sleep(TestUtil.RESPONSE_WAIT_TIME);
        } catch (InterruptedException e) {
            LOGGER.error("Thread Interuppted while sleeping ", e);
        }
    }

    @Test
    public void passthroughGetTestCase() {
        Object lock = new Object();
        ResponseCallback responseCallback1 = new PassthroughCallBack(lock);
        HTTPClient httpClient = new HTTPClient(responseCallback1, null);
        try {
            Request request = new Request(HttpVersion.HTTP_1_1, HttpMethod.GET, new URI("http://localhost:8080/"));
            httpClient.send(request);
            if (((PassthroughCallBack) responseCallback1).getValue() == null) {
                synchronized (lock) {
                    lock.wait(TestUtil.RESPONSE_WAIT_TIME);
                }
                Assert.assertTrue(testValue.equals(((PassthroughCallBack) responseCallback1).getValue()));
            } else {
                Assert.assertTrue(testValue.equals(((PassthroughCallBack) responseCallback1).getValue()));
            }
        } catch (URISyntaxException e) {
            Assert.assertTrue(false);
        } catch (InterruptedException e) {
            Assert.assertTrue(false);
        }

    }

    @After
    public void cleanUp() {
        TestUtil.cleanUp(nettyListener, httpServer);
    }

}
