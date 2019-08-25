/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.http2;

import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A test case for http2 with multiparts (with and without prior knowledge).
 */
@Test(groups = "http2-test")
public class MultipartTestCase extends Http2BaseTest {

    @Test(description = "Test http2 with multiparts without prior knowledge")
    public void testMultipart() throws IOException {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("priorKnowledge", "false");
        assertResponse(headersMap);
    }

    @Test(description = "Test http2 with multiparts with prior knowledge.")
    public void testMultipartsWithPriorKnowledge() throws IOException {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("priorKnowledge", "true");
        assertResponse(headersMap);
    }

    private void assertResponse(Map<String, String> headersMap) throws IOException {
        int servicePort = 9100;
        HttpResponse response = HttpClientRequest
                .doGet(serverInstance.getServiceURLHttp(servicePort, "multiparts/initial"), headersMap);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"name\":\"wso2\"}<message>Hello world</message>text content",
                "Incorrect data received");
    }
}
