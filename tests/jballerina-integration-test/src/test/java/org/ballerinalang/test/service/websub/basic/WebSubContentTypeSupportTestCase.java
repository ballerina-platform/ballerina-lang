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
import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_STRING;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_XML;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_INTERNAL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_REMOTE;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.requestUpdate;

/**
 * This class tests support for WebSub notifications of different content types.
 * Tests:
 * 1. Content delivery to multiple subscribers
 * 2. Content delivery for subscriptions made with and without specifying a secret
 * 3. Content delivery for JSON, string and XML types.
 */
@Test(groups = "websub-test")
public class WebSubContentTypeSupportTestCase extends WebSubBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 45000;
    private static final int WEBSUB_PORT = 23282;
    private static final String SKIP_SUBSCRIBER_CHECK = "skip_subscriber_check";
    private static final String CONTENT_TYPE_PUBLISHER_URL = "http://localhost:23080/contentTypePublisher/notify/";
    
    private BServerInstance webSubSubscriber;

    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed -" +
            " Mode [subscribe], Topic [http://one.websub.topic.com], Lease Seconds [3000]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed -" +
            " Mode [subscribe], Topic [http://one.websub.topic.com], Lease Seconds [1000]";

    private static final String XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "XML WebSub Notification Received by websubSubscriber: " +
                    "<websub><request>Notification</request><type>Internal</type></websub>";
    private static final String TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "Text WebSub Notification Received by websubSubscriber: Text update for internal Hub";
    private static final String JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "JSON WebSub Notification Received by websubSubscriber: " +
                    "{\"action\":\"publish\", \"mode\":\"internal-hub\"}";
    private static final String XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "XML WebSub Notification Received by websubSubscriberTwo: " +
                    "<websub><request>Notification</request><type>Internal</type></websub>";
    private static final String TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "Text WebSub Notification Received by websubSubscriberTwo: Text update for internal Hub";
    private static final String JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "JSON WebSub Notification Received by websubSubscriberTwo: " +
                    "{\"action\":\"publish\", \"mode\":\"internal-hub\"}";

    private static final String XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "XML WebSub Notification Received by websubSubscriber: "
                    + "<websub><request>Notification</request><type>Remote</type></websub>";
    private static final String TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "Text WebSub Notification Received by websubSubscriber: Text update for remote Hub";
    private static final String JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "JSON WebSub Notification Received by websubSubscriber: {\"action\":\"publish\", \"mode\":\"remote-hub\"}";
    private static final String XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "XML WebSub Notification Received by websubSubscriberTwo: " +
                    "<websub><request>Notification</request><type>Remote</type></websub>";
    private static final String TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "Text WebSub Notification Received by websubSubscriberTwo: Text update for remote Hub";
    private static final String JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "JSON WebSub Notification Received by websubSubscriberTwo: " +
                    "{\"action\":\"publish\", \"mode\":\"remote-hub\"}";

    private LogLeecher intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);

    private LogLeecher internalHubXmlNotificationLogLeecherOne
            = new LogLeecher(XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubXmlNotificationLogLeecherTwo
            = new LogLeecher(XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher internalHubTextNotificationLogLeecherOne
            = new LogLeecher(TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubTextNotificationLogLeecherTwo
            = new LogLeecher(TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher internalHubJsonNotificationLogLeecherOne
            = new LogLeecher(JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubJsonNotificationLogLeecherTwo
            = new LogLeecher(JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);


    private LogLeecher remoteHubXmlNotificationLogLeecherOne
            = new LogLeecher(XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubXmlNotificationLogLeecherTwo
            = new LogLeecher(XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher remoteHubTextNotificationLogLeecherOne
            = new LogLeecher(TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubTextNotificationLogLeecherTwo
            = new LogLeecher(TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher remoteHubJsonNotificationLogLeecherOne
            = new LogLeecher(JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubJsonNotificationLogLeecherTwo
            = new LogLeecher(JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);


    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);

        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources" +
                                                File.separator + "websub" + File.separator + "subscriber" +
                                                File.separator + "test_different_content_type_subscribers.bal")
                .getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubXmlNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubXmlNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubTextNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubTextNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubJsonNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubJsonNotificationLogLeecherTwo);

        webSubSubscriber.addLogLeecher(remoteHubXmlNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubXmlNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(remoteHubTextNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubTextNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(remoteHubJsonNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubJsonNotificationLogLeecherTwo);

        String[] subscriberArgs = {};
        webSubSubscriber.startServer(subscriberBal, new String[0], subscriberArgs, new int[]{WEBSUB_PORT});
    }

    @Test
    public void testSubscriptionAndIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
        intentVerificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
        requestUpdate(CONTENT_TYPE_PUBLISHER_URL + WEBSUB_PORT, HUB_MODE_INTERNAL, CONTENT_TYPE_STRING);
        requestUpdate(CONTENT_TYPE_PUBLISHER_URL + SKIP_SUBSCRIBER_CHECK, HUB_MODE_REMOTE, CONTENT_TYPE_STRING);
        requestUpdate(CONTENT_TYPE_PUBLISHER_URL + SKIP_SUBSCRIBER_CHECK, HUB_MODE_INTERNAL, CONTENT_TYPE_XML);
        requestUpdate(CONTENT_TYPE_PUBLISHER_URL + SKIP_SUBSCRIBER_CHECK, HUB_MODE_REMOTE, CONTENT_TYPE_XML);
        requestUpdate(CONTENT_TYPE_PUBLISHER_URL + SKIP_SUBSCRIBER_CHECK, HUB_MODE_INTERNAL, CONTENT_TYPE_JSON);
        requestUpdate(CONTENT_TYPE_PUBLISHER_URL + SKIP_SUBSCRIBER_CHECK, HUB_MODE_REMOTE, CONTENT_TYPE_JSON);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedTextContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubTextNotificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification", enabled = false)
    public void testAuthenticatedTextContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubTextNotificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedTextContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubTextNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification", enabled = false)
    public void testUnauthenticatedTextContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubTextNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedXmlContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubXmlNotificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedXmlContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubXmlNotificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedXmlContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubXmlNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedXmlContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubXmlNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedJsonContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubJsonNotificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedJsonContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubJsonNotificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedJsonContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubJsonNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedJsonContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubJsonNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @AfterClass
    private void teardown() throws Exception {
        webSubSubscriber.shutdownServer();
    }
}
