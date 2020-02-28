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

package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test post HTTP Listener action functionality.
 */
@Test(groups = "http-test")
public class HTTPCallerActionsTestCase extends HttpBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 20000;
    private final int servicePort = 9229;
    private static final String POST_RESPOND_LOG = "Service Level Variable : respond";
    private static final String POST_REDIRECT_LOG = "Service Level Variable : respond";
    private static final String DIRTY_RESPONSE_LOG = "Couldn't complete the respond operation as the response " +
            "has been already used.";
    private LogLeecher postRespondLogLeecher = new LogLeecher(POST_RESPOND_LOG);
    private LogLeecher postRedirectLogLeecher = new LogLeecher(POST_REDIRECT_LOG);
    private LogLeecher dirtyResponseLogLeecher = new LogLeecher(DIRTY_RESPONSE_LOG);

    @BeforeMethod
    public void setup() {
        serverInstance.addLogLeecher(postRespondLogLeecher);
        serverInstance.addLogLeecher(postRedirectLogLeecher);
        serverInstance.addLogLeecher(dirtyResponseLogLeecher);
    }

    @Test
    public void testNonBlockingRespondAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "listener/respond"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "sample value", "Message content mismatched");
    }

    @Test(dependsOnMethods = "testNonBlockingRespondAction")
    public void testExecutionAfterRespondAction() throws BallerinaTestException {
        postRespondLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testExecutionAfterRespondAction")
    public void testNonBlockingRedirectAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                 "listener/redirect"));
        Assert.assertEquals(response.getResponseCode(), 308, "Response code mismatched");
    }

    @Test(dependsOnMethods = "testNonBlockingRedirectAction")
    public void testExecutionAfterRedirectAction() throws BallerinaTestException {
        postRedirectLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test
    public void testDirtyResponse() throws IOException, BallerinaTestException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9258, "hello"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9258, "hello"));
        dirtyResponseLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
        Assert.assertEquals(response.getData(), "couldn't complete the respond operation as the response has" +
                        " been already used.", "Message content mismatched");
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
    }
}
