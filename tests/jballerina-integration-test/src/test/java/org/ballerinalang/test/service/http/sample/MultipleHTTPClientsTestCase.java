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

package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test multiple clients with different configurations that are defined in global scope.
 */
@Test(groups = "http-test")
public class MultipleHTTPClientsTestCase extends HttpBaseTest {
    @Test
    public void testH1Client() throws IOException {
        String responseData = sendRequest("test/h1");
        Assert.assertEquals(responseData, "Connection and upgrade headers are not present--HTTP/1.1 request--1.1");
    }

    @Test
    public void testH2Client() throws IOException {
        String responseData = sendRequest("test/h2");
        Assert.assertEquals(responseData,
                            "Connection and upgrade headers are not present--HTTP/2 with prior knowledge--2.0");
    }

    private String sendRequest(String path) throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9230, path));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        return response.getData();
    }
}
