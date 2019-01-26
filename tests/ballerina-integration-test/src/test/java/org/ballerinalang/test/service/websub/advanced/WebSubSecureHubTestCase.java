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

package org.ballerinalang.test.service.websub.advanced;

import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_JSON;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_XML;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_INTERNAL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_REMOTE;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL_THREE;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.requestUpdate;

/**
 * This class tests basic auth support for WebSub Hub.
 * Tests:
 * 1. Subscriber one is authenticated properly
 * 2. Subscriber two fails the authentication due to incorrect password
 * 3. Subscriber two fails the authorization due to incorrect scope
 */
@Test(groups = "websub-test")
public class WebSubSecureHubTestCase extends WebSubAdvancedBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 45000;
    private BServerInstance webSubSubscriber;

    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - " +
            "Mode [subscribe], Topic [http://one.persistence.topic.com], Lease Seconds [3600]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG =
            "Subscription Request failed at Hub[https://localhost:9191/websub/hub], for Topic[http://two.persistence" +
                    ".topic.com]: Error in request: Mode[subscribe] at Hub[https://localhost:9191/websub/hub] - " +
                    "Authentication failure";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_THREE_LOG =
            "Subscription Request failed at Hub[https://localhost:9191/websub/hub], for Topic[http://one.persistence" +
                    ".topic.com]: Error in request: Mode[subscribe] at Hub[https://localhost:9191/websub/hub] - " +
                    "Authorization failure ";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_FOUR_LOG = "ballerina: Intent Verification agreed - " +
            "Mode [subscribe], Topic [http://one.websub.topic.com], Lease Seconds [1200]";
    private static final String INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "WebSub Notification Received by One: {\"mode\":\"internal\", \"content_type\":\"json\"}";
    private static final String INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "WebSub Notification Received by Four: {\"mode\":\"remote\", \"content_type\":\"xml\"}";

    private LogLeecher intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher intentVerificationLogLeecherThree = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_THREE_LOG);
    private LogLeecher intentVerificationLogLeecherFour = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_FOUR_LOG);
    private LogLeecher internalHubNotificationLogLeecherOne =
            new LogLeecher(INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubNotificationLogLeecherTwo =
            new LogLeecher(INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);

    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);
        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources" +
                                                File.separator + "websub" + File.separator +
                                                "test_subscribers_at_basic_auth_secured_hub.bal").getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        webSubSubscriber.addErrorLogLeecher(intentVerificationLogLeecherTwo);
        webSubSubscriber.addErrorLogLeecher(intentVerificationLogLeecherThree);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherFour);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherTwo);

        webSubSubscriber.startServer(subscriberBal, new String[0], new int[]{8484});
    }

    @Test
    public void testDiscoveryAndIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
        intentVerificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
        intentVerificationLogLeecherThree.waitForText(LOG_LEECHER_TIMEOUT);
        intentVerificationLogLeecherFour.waitForText(LOG_LEECHER_TIMEOUT);
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_INTERNAL, CONTENT_TYPE_JSON);
        requestUpdate(PUBLISHER_NOTIFY_URL_THREE, HUB_MODE_REMOTE, CONTENT_TYPE_XML);
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
