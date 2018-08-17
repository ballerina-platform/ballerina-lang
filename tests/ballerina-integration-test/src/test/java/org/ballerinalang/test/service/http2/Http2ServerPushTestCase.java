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
package org.ballerinalang.test.service.http2;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test case for HTTP/2.0 server push scenario.
 */
@Test(groups = "http2-test")
public class Http2ServerPushTestCase extends BaseTest {

    @BeforeTest(groups = "http2-test")
    public void start() throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "http2").getAbsolutePath();
        String[] args = new String[]{"--sourceroot", balFile};
        serverInstance.startBallerinaServer("http2services", args);
    }

    @AfterTest(groups = "http2-test")
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.stopServer();
    }

    @Test(description = "Test HTTP/2.0 Server Push scenario")
    public void testPushPromise() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9090, "frontend"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        String responseData = response.getData();
        Assert.assertTrue(responseData.contains("successful"), responseData);
    }
}
