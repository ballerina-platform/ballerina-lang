/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.chunkdisable;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http.HttpClient;

import static org.testng.AssertJUnit.assertEquals;

/**
 * A test class for auto chunking behaviour.
 */
public class ChunkAutoServerTestCase extends ChunkServerTemplate {

    @BeforeClass
    public void setUp() {
        listenerConfiguration.setChunkConfig(ChunkConfig.AUTO);
        super.setUp();
    }

    @Test
    public void postTest() {

        HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "/", Unpooled.wrappedBuffer(TestUtil.smallEntity.getBytes()));
        FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

        assertEquals(TestUtil.smallEntity, TestUtil.getEntityBodyFrom(httpResponse));
        assertEquals(httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH), "70");
    }
}
