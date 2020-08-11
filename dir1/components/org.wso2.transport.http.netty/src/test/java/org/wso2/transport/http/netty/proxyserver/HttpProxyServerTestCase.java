/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.proxyserver;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.mockserver.integration.ClientAndProxy;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.TestUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_HOST;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_PORT;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_POST_METHOD;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.IS_PROXY_ENABLED;
import static org.wso2.transport.http.netty.contract.Constants.PROTOCOL;

/**
 * A test for connecting to a proxy server over HTTP.
 */
public class HttpProxyServerTestCase {
    private ClientAndProxy proxy;
    private HttpCarbonMessage msg;
    private String testValue = "Test";

    @BeforeClass
    public void setup() throws InterruptedException {
        proxy = startClientAndProxy(TestUtil.SERVER_PORT2);

        ByteBuffer byteBuffer = ByteBuffer.wrap(testValue.getBytes(Charset.forName("UTF-8")));
        msg = new HttpCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, ""));
        msg.setProperty(HTTP_PORT, TestUtil.SERVER_PORT1);
        msg.setProperty(PROTOCOL, HTTP_SCHEME);
        msg.setProperty(HTTP_HOST, TestUtil.TEST_HOST);
        msg.setHttpMethod(HTTP_POST_METHOD);
        msg.setHeader("Host", "localhost:9001");
        msg.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));

        ProxyServerUtil.setUpClientAndServerConnectors(getListenerConfiguration(), HTTP_SCHEME);
    }

    private ListenerConfiguration getListenerConfiguration() {
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(TestUtil.SERVER_PORT1);
        return listenerConfiguration;
    }

    @Test (description = "An integration test for connecting to a proxy over http. "
            + "This will not go through netty proxy handler.")
    public void testHttpProxyServer() {
        ProxyServerUtil.sendRequest(msg, testValue);
    }

    @Test (description = "Tests the request going to the proxy. This should contain the entire URL as the path as it is"
            + " not a CONNECT request.")
    public void testHttpProxyRequestUrl() {
        msg.setProperty(IS_PROXY_ENABLED, true);
        HttpRequest request = Util.createHttpRequest(msg);
        String expectedUri = "http://localhost:9001";
        Assert.assertEquals(request.uri(), expectedUri);
    }

    @AfterClass
    public void cleanUp() {
        ProxyServerUtil.shutDown();
        proxy.stop();
    }
}

