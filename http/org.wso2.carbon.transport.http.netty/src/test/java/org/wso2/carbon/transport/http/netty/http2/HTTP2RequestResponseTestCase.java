/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.http2;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.client.http2.HTTP2Client;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.util.List;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.testng.AssertJUnit.assertEquals;

/**
 * A test class for http2 transport
 */
public class HTTP2RequestResponseTestCase {

    private static final Logger log = LoggerFactory.getLogger(HTTP2RequestResponseTestCase.class);
    private List<HTTPServerConnector> serverConnectors;
    private HTTPServer httpServer;
    HTTP2Client http2Client = null;

    @BeforeClass
    public void setUp() throws Exception {
        TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/http2/netty-transports.yml");
        serverConnectors = TestUtil.startConnectors(configuration, new HTTP2MessageProcessor());
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT, HTTP2MessageProcessor.TEST_VALUE, Constants
                .TEXT_PLAIN);
        http2Client = new HTTP2Client(false, "localhost", 8490);
    }

    @Test
    public void http2GetRequestResponseTest() throws Exception {
        try {
            // Send Get http2 request and get response
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");
            int send = http2Client.send(request);
            String response = http2Client.getResponse(send);
            assertEquals(HTTP2MessageProcessor.TEST_VALUE, response);
        } catch (Exception ex) {
            TestUtil.handleException("Error while sending http2 request ", ex);
        }
    }

    @Test
    public void http2PostRequestResponseTest() throws Exception {
        try {
            // Send Post http2 request and get response
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
            String json = HTTP2MessageProcessor.TEST_VALUE;
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            request.headers().set(HttpHeaderNames.ACCEPT, "text/plain");
            ByteBuf buffer = request.content().clear();
            int p0 = buffer.writerIndex();
            buffer.writeBytes(json.getBytes());
            int p1 = buffer.writerIndex();
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));

            int send = http2Client.send(request);
            String response = http2Client.getResponse(send);
            assertEquals(HTTP2MessageProcessor.TEST_VALUE, response);
        } catch (Exception ex) {
            TestUtil.handleException("Error while sending http2 request ", ex);
        }
    }


    @AfterClass
    public void cleanUp() throws ServerConnectorException {

        TestUtil.cleanUp(serverConnectors, httpServer);
        if (http2Client != null) {
            http2Client.close();
        }
    }

}
