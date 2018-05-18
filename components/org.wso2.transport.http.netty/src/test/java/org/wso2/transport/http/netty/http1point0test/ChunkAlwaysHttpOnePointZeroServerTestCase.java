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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.http1point0test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.chunkdisable.ChunkServerTemplate;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.util.TestUtil;

import static org.testng.AssertJUnit.assertEquals;

/**
 * A test class for always chunking behaviour.
 */
public class ChunkAlwaysHttpOnePointZeroServerTestCase extends ChunkServerTemplate {

    @BeforeClass
    public void setUp() {
        listenerConfiguration.setChunkConfig(ChunkConfig.ALWAYS);
        super.setUp();
    }

    @Test
    public void postTest() {
        try {
            HttpResponse<String> response = Unirest.post(baseURI.resolve("/").toString())
                    .header("Connection", "Keep-Alive").body(TestUtil.largeEntity).asString();

            Assert.assertNull("Content-Length header present in the response.",
                    response.getHeaders().getFirst(HttpHeaderNames.CONTENT_LENGTH.toString()));
            assertEquals(response.getHeaders().getFirst(HttpHeaderNames.TRANSFER_ENCODING.toString()),
                    Constants.CHUNKED);

            response = Unirest.post(baseURI.resolve("/").toString())
                    .header("Connection", "Keep-Alive").body(TestUtil.smallEntity).asString();

            Assert.assertNull("Content-Length header present in the response.",
                    response.getHeaders().getFirst(HttpHeaderNames.CONTENT_LENGTH.toString()));
            assertEquals(response.getHeaders().getFirst(HttpHeaderNames.TRANSFER_ENCODING.toString()),
                    Constants.CHUNKED);
        } catch (UnirestException e) {
            TestUtil.handleException("Exception occurred while running postTest", e);
        }
    }
}
