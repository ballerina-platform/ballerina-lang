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

package org.ballerinalang.test.service.websub.basic;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * This class tests the startup functionality of Hub and Subscriber.
 */
@Test(groups = "websub-test")
public class WebSubStartupFunctionalityTestCase extends BaseTest {
    private static final int WEBSUB_SUB_PORT = 23386;
    private static final int WEBSUB_HUB_PORT = 23190;
    private BServerInstance webSubSubscriber;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);
        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources" +
                                                File.separator + "websub" + File.separator + "subscriber" +
                                                File.separator + "test_subscriber_startup.bal").getAbsolutePath();

        webSubSubscriber.startServer(subscriberBal, new int[]{WEBSUB_SUB_PORT, WEBSUB_HUB_PORT});
    }

    @Test(description = "Test multiple Subscriber service startup in a single port")
    public void testMultipleSubscribersStartUpInSamePort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                webSubSubscriber.getServiceURLHttp(WEBSUB_SUB_PORT, "subscriber/start"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(),
                            "failed to start server connector '0.0.0.0:23387': Address already in use", "Mismatched");
    }

    @Test
    public void testHubStartUp() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                webSubSubscriber.getServiceURLHttp(WEBSUB_HUB_PORT, "hub/startup"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "hub(lis2) start successfully", "Mismatched");
    }

    @Test
    public void testPublisherAndSubscriptionInvalidSameResourcePath() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(webSubSubscriber.getServiceURLHttp(WEBSUB_HUB_PORT,
                                     "hub/testPublisherAndSubscriptionInvalidSameResourcePath"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(),
                            "publisher and subscription resource paths cannot be the same", "Mismatched");
    }

    @AfterClass
    public void teardown() throws Exception {
        webSubSubscriber.shutdownServer();
    }
}
