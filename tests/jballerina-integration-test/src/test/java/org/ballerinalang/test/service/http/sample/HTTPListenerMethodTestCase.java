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
import java.net.ConnectException;

/**
 * Test HTTP Listener method functionality. __attach, __start and __gracefulStop methods have been tested.
 */
@Test(groups = "http-test")
public class HTTPListenerMethodTestCase extends HttpBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 20000;
    private final int servicePort = 9251;
    private final int backendServicePort = 9252;
    private static final String START_LISTENER_LOG = "[ballerina/http] started HTTP/WS listener 0.0.0.0:9252";
    private static final String STOP_LISTENER_LOG = "[ballerina/http] stopped HTTP/WS listener 0.0.0.0:9252";
    private LogLeecher startListenerLogLeecher = new LogLeecher(START_LISTENER_LOG);
    private LogLeecher stopListenerLogLeecher = new LogLeecher(STOP_LISTENER_LOG);

    @BeforeMethod
    public void setup() {
        serverInstance.addLogLeecher(startListenerLogLeecher);
        serverInstance.addLogLeecher(stopListenerLogLeecher);
    }

    @Test
    public void testServiceAttachAndStart() throws BallerinaTestException, IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "startService/test"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Backend service started!", "Message content mismatched");

        startListenerLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testServiceAttachAndStart")
    public void testAvailabilityOfAttachedService() throws BallerinaTestException, IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(backendServicePort, "mock1"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Mock1 invoked!", "Message content mismatched");
    }

    @Test(dependsOnMethods = "testAvailabilityOfAttachedService")
    public void testGracefulStopMethod() throws IOException, BallerinaTestException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(backendServicePort, "mock2"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Mock2 invoked!", "Message content mismatched");

        stopListenerLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testGracefulStopMethod", expectedExceptions = ConnectException.class,
          expectedExceptionsMessageRegExp = ".*Connection refused.*")
    public void testInvokingStoppedService() throws IOException, InterruptedException {
        // Wait until service is stopped before sending the next request
        Thread.sleep(10000);
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(backendServicePort, "mock1"));
    }
}
