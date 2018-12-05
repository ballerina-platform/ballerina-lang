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

package org.wso2.transport.http.netty.http1point0test;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.chunkdisable.ChunkClientTemplate;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.TestUtil;

/**
 * A test class for enable chunking behaviour for http 1.0.
 */
public class ChunkAlwaysHttpOnePointZeroClientTestCase extends ChunkClientTemplate {

    @BeforeClass
    public void setUp() {
        senderConfiguration.setChunkingConfig(ChunkConfig.ALWAYS);
        senderConfiguration.setHttpVersion("1.0");
        super.setUp();
    }

    @Test
    public void postTest() {
        try {
            HttpCarbonMessage response = sendRequest(TestUtil.largeEntity);
            Assert.assertNull("Content-Length header present in the response.",
                    response.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString()));
            Assert.assertEquals("Transfer-Encoding header is not present in the response.", Constants.CHUNKED,
                    response.getHeader(HttpHeaderNames.TRANSFER_ENCODING.toString()));

            response = sendRequest(TestUtil.smallEntity);
            Assert.assertNull("Content-Length header present in the response.",
                    response.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString()));
            Assert.assertEquals("Transfer-Encoding header is not present in the response.", Constants.CHUNKED,
                    response.getHeader(HttpHeaderNames.TRANSFER_ENCODING.toString()));

        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running postTest", e);
        }
    }
}
