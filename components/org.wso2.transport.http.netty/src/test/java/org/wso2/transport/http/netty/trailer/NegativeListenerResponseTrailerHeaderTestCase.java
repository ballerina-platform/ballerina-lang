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
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contentaware.listeners.TrailerHeaderListener;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http.HttpClient;

/**
 * Test case for HTTP/1.1 trailer headers for inbound response with chunked disabled.
 *
 * @since 6.3.1
 */
public class NegativeListenerResponseTrailerHeaderTestCase extends TrailerHeaderTestTemplate {

    @BeforeClass
    public void setup() {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setChunkConfig(ChunkConfig.NEVER);

        HttpHeaders trailers = new DefaultLastHttpContent().trailingHeaders();
        trailers.add("foo", "bar");
        trailers.add("baz", "ballerina");
        super.setup(listenerConfiguration, trailers, TrailerHeaderListener.MessageType.RESPONSE);
    }

    @Test
    public void testNegativeTrailer() {
        HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
        FullHttpRequest httpRequest = new
                DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
                                       Unpooled.wrappedBuffer(TestUtil.smallEntity.getBytes()));
        FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

        AssertJUnit.assertEquals(TestUtil.smallEntity, TestUtil.getEntityBodyFrom(httpResponse));
        AssertJUnit.assertNotNull(httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        AssertJUnit.assertNull(httpResponse.headers().get(HttpHeaderNames.TRANSFER_ENCODING));
        AssertJUnit.assertNull(httpResponse.trailingHeaders().get("foo"));
        AssertJUnit.assertNull(httpResponse.trailingHeaders().get("baz"));
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        super.cleanUp();
    }
}
