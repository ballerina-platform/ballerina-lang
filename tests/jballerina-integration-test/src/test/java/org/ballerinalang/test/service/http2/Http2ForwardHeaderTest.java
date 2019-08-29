/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.service.http2;

import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test HTTP2 client with forwarded enabled.
 */
@Test(groups = "http2-test")
public class Http2ForwardHeaderTest extends Http2BaseTest {
    private final int servicePort = 9105;

    @Test
    public void testForwardHeader() throws IOException {
        HttpResponse response = HttpClientRequest
                .doGet(serverInstance.getServiceURLHttp(servicePort, "initiatingService/initiatingResource"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get("forwarded"), "for=127.0.0.1; by=127.0.0.1; proto=http");
    }
}
