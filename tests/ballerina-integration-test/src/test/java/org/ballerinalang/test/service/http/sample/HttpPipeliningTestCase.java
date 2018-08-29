/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.util.client.HttpClient;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.LinkedList;

import static org.ballerinalang.test.util.TestUtils.getEntityBodyFrom;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Test case for HTTP pipelining.
 *
 * @since 0.982.0
 */
@Test(groups = "http-test")
public class HttpPipeliningTestCase extends BaseTest {

    @Test(description = "Test whether the response order matches the request order when HTTP pipelining is used")
    public void testResponseOrder() throws IOException, InterruptedException {

        HttpClient httpClient = new HttpClient("localhost", 9220);
        LinkedList<FullHttpResponse> fullHttpResponses = httpClient.sendPipeLinedRequests(
                "/pipeliningTest/responseOrder");

        //Verify response order and their body content
        verifyResponse(fullHttpResponses.pop(), "response-one", "Hello1");
        verifyResponse(fullHttpResponses.pop(), "response-two", "Hello2");
        verifyResponse(fullHttpResponses.pop(), "response-three", "Hello3");

        assertFalse(httpClient.waitForChannelClose());
    }

    private void verifyResponse(FullHttpResponse response, String expectedId, String expectedBody) {
        assertEquals(response.status(), HttpResponseStatus.OK);
        assertEquals(response.headers().get("message-id"), expectedId);
        assertEquals(getEntityBodyFrom(response), expectedBody);
    }
}
