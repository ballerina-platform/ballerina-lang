/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.transport.http.netty.trailer;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contentaware.listeners.TrailerHeaderListener;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http.HttpClient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test case for HTTP/1.1 trailer headers come along with inbound chunked response.
 *
 * @since 6.3.1
 */
public class ListenerResponseTrailerHeaderTestCase extends TrailerHeaderTestTemplate {

    @BeforeClass
    public void setup() {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setChunkConfig(ChunkConfig.ALWAYS);

        HttpHeaders trailers = new DefaultLastHttpContent().trailingHeaders();
        trailers.add("foo", "bar");
        trailers.add("baz", "ballerina");
        trailers.add("Max-forwards", "five");

        super.setup(listenerConfiguration, trailers, TrailerHeaderListener.MessageType.RESPONSE);
    }

    @Test
    public void testNoPayload() {
        HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
        FullHttpRequest httpRequest = new
                DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

        assertResponse(httpResponse, "");
    }

    @Test
    public void testSmallPayload() {
        HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
        FullHttpRequest httpRequest = new
                DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
                                       Unpooled.wrappedBuffer(TestUtil.smallEntity.getBytes()));
        FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

        assertResponse(httpResponse, TestUtil.smallEntity);
    }

    @Test
    public void testLargePayload() {
        HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
        FullHttpRequest httpRequest = new
                DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
                                       Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));
        FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

        assertResponse(httpResponse, TestUtil.largeEntity);
    }

    private void assertResponse(FullHttpResponse httpResponse, String payload) {
        assertEquals(payload, TestUtil.getEntityBodyFrom(httpResponse));
        assertNull(httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        assertEquals(httpResponse.headers().get(HttpHeaderNames.TRANSFER_ENCODING), "chunked");
        assertEquals(httpResponse.headers().get("Trailer"), "foo, baz, Max-forwards");
        assertEquals(httpResponse.trailingHeaders().get("foo"), "bar");
        assertEquals(httpResponse.trailingHeaders().get("baz"), "ballerina");
        assertEquals(httpResponse.trailingHeaders().get("Max-forwards"), "five");
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        super.cleanUp();
    }
}
