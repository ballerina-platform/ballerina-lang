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

package org.ballerinalang.test.service.websub.basic;

import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_JSON;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_INTERNAL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL_TWO;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.requestUpdate;

/**
 * This class includes an integration scenario which covers the following:
 * 1. Multiple subscriber services binding to the same endpoint
 * 2. Sending a subscription request to the hub and topic identified via WebSub discovery for the resource URL
 * specified as an annotation
 * 3. Intent verification for subscription request sent following WebSub discovery
 * 4. Prioritizing hub and topic specified as annotations over the resource URL if specified
 */
@Test(groups = "websub-test")
public class WebSubDiscoveryWithMultipleSubscribersTestCase extends WebSubBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 45000;
    private BServerInstance webSubSubscriber;

    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - " +
            "Mode [subscribe], Topic [http://three.websub.topic.com], Lease Seconds [3600]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed - " +
            "Mode [subscribe], Topic [http://four.websub.topic.com], Lease Seconds [1200]";
    private static final String INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "WebSub Notification Received by One: {\"action\":\"publish\", \"mode\":\"internal-hub\"}";
    private static final String INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "WebSub Notification Received by Two: {\"action\":\"publish\", \"mode\":\"internal-hub-two\"}";

    private LogLeecher intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher internalHubNotificationLogLeecherOne =
            new LogLeecher(INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubNotificationLogLeecherTwo =
            new LogLeecher(INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);

    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);
        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources" +
                                                File.separator + "websub" + File.separator + "subscriber" +
                                                File.separator + "test_multiple_subscribers.bal").getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherTwo);

        String[] subscriberArgs = {"-e", "test.hub.url=" + "https://localhost:23191/websub/hub"};
        webSubSubscriber.startServer(subscriberBal, subscriberArgs, new int[]{23383});
    }

    @Test
    public void testDiscoveryAndIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
        intentVerificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
        requestUpdate(PUBLISHER_NOTIFY_URL_TWO, HUB_MODE_INTERNAL, CONTENT_TYPE_JSON);
    }

    @Test(dependsOnMethods = "testDiscoveryAndIntentVerification")
    public void testContentReceipt() throws BallerinaTestException {
        internalHubNotificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
        internalHubNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @AfterClass
    private void teardown() throws Exception {
        webSubSubscriber.shutdownServer();
    }
}
