/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.service.http2;

import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test case for HTTP/2.0 server to HTTP/1.1 fallback scenario.
 */
@Test(groups = "http2-test")
public class Http2ToHttp1FallbackTestCase extends Http2BaseTest {

    @Test(description = "Test HTTP/2.0 to HTTP/1.1 server fallback scenario")
    public void testFallback() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9095, "hello"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        String responseData = response.getData();
        Assert.assertEquals("Version: 1.1", responseData, "HTTP/2.0 to HTTP/1.1 server fallback scenario failed");
    }

    @Test(description = "Test HTTP/2.0 to HTTP/1.1 server fallback scenario with SSL enabled")
    public void testFallbackWithSSL() throws IOException {
        String serviceUrl = "https://localhost:9096/hello";
        String serverHome = serverInstance.getServerHome();
        HttpResponse response = HttpsClientRequest.doGet(serviceUrl, serverHome);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        String responseData = response.getData();
        Assert.assertEquals("Version: 1.1", responseData,
                "HTTP/2.0 to HTTP/1.1 server fallback scenario with SSL enabled failed");
    }
}
