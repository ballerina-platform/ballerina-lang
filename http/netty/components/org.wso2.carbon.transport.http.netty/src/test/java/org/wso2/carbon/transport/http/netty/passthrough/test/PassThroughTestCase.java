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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.wso2.carbon.transport.http.netty.util.client.Response;
import org.wso2.carbon.transport.http.netty.util.client.ResponseCallback;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A test class for passthrough transport
 */
public class PassThroughTestCase {

    private NettyListener nettyListener;
    private CarbonMessageProcessor messageProcessor;
    private TransportSender transportSender;

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
    }

    @Test
    public void passthroughTestCase() {
        String testValue = "Test Message";
        Object lock = new Object();
        ResponseCallback responseCallback1 = new PassthroughCallBack(lock);
        HTTPClient httpClient = new HTTPClient(responseCallback1, null);
        try {
            Request request = new Request(HttpVersion.HTTP_1_1, HttpMethod.POST, new URI("http://localhost:8080/"));
            request.setMessageBody(testValue, "text/plain");
            httpClient.send(request);
            if (((PassthroughCallBack) responseCallback1).getValue() == null) {
                synchronized (lock) {
                    lock.wait(100000);
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


    public void cleanUp() {
        nettyListener.stop();
        httpServer.shutdown();
    }

    private class PassthroughCallBack implements ResponseCallback {

        private String value;

        Object lock;

        public PassthroughCallBack(Object lock) {
            this.lock = lock;
        }

        @Override
        public void received(Response response) {
            value = response.getStringValue();
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        public String getValue() {
            return value;
        }
    }

}
