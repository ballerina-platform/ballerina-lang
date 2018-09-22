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
package org.ballerinalang.test.service.websub;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_JSON;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_INTERNAL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_REMOTE;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.requestUpdate;

/**
 * This class includes an integration scenario which covers the following:
 * 1. Bringing up the Ballerina Hub
 * 2. Sending the subscription request for WebSub Subscriber services on start up, and auto verifying intent to
 * subscribe, when the hub sends an intent verification request, since an onIntentVerification resource is not
 * specified
 * 3. Functions made available to the Publishers - publishing directly on to the Ballerina Hub or to a Hub by
 * specifying the URL (usecase: remote hubs)
 * 4. Content Delivery process - by verifying content is delivered when update notification is done for a subscribed
 * topic - both directly to the hub and specifying hub URL
 * 5. Subscription and content distribution when a secret is not specified
 */
@Test(groups = "websub-test")
public class WebSubAutoIntentVerificationTestCase extends WebSubBaseTest {
    private BServerInstance webSubSubscriber;
    private BMainInstance subscriptionChanger;

    private boolean firstTest = true;
    private boolean lastTest = false;

    private static String hubUrl = "https://localhost:9191/websub/hub";
    private static final String INTENT_VERIFICATION_LOG = "ballerina: Intent Verification agreed - Mode [subscribe], "
            + "Topic [http://one.websub.topic.com], Lease Seconds [86400]";
    private static final String INTERNAL_HUB_NOTIFICATION_LOG = "WebSub Notification Received: "
            + "{\"action\":\"publish\", \"mode\":\"internal-hub\"}";
    private static final String REMOTE_HUB_NOTIFICATION_LOG = "WebSub Notification Received: "
            + "{\"action\":\"publish\", \"mode\":\"remote-hub\"}";
    private static final String UNSUBSCRIPTION_INTENT_VERIFICATION_LOG = "ballerina: Intent Verification agreed - Mode "
            + "[unsubscribe], Topic [http://one.websub.topic.com]";
    private static final String INTENT_VERIFICATION_DENIAL_LOG = "ballerina: Intent Verification denied - Mode "
            + "[subscribe], Topic [http://two.websub.topic.com]";

    private LogLeecher intentVerificationLogLeecher = new LogLeecher(INTENT_VERIFICATION_LOG);
    private LogLeecher internalHubNotificationLogLeecher = new LogLeecher(INTERNAL_HUB_NOTIFICATION_LOG);
    private LogLeecher remoteHubNotificationLogLeecher = new LogLeecher(REMOTE_HUB_NOTIFICATION_LOG);
    private LogLeecher unsubscriptionIntentVerificationLogLeecher = new LogLeecher(
            UNSUBSCRIPTION_INTENT_VERIFICATION_LOG);
    private LogLeecher logAbsenceTestLogLeecher = new LogLeecher(INTERNAL_HUB_NOTIFICATION_LOG);
    private LogLeecher intentVerificationDenialLogLeecher = new LogLeecher(INTENT_VERIFICATION_DENIAL_LOG);


    @BeforeMethod
    public void setup() throws BallerinaTestException {
        if (!firstTest) {
            return;
        }
        firstTest = false;
        webSubSubscriber = new BServerInstance(balServer);
        subscriptionChanger = new BMainInstance(balServer);
        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "websub" + File.separator + "websub_test_subscriber.bal").getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecher);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecher);
        webSubSubscriber.addLogLeecher(remoteHubNotificationLogLeecher);
        webSubSubscriber.addLogLeecher(unsubscriptionIntentVerificationLogLeecher);
        webSubSubscriber.addLogLeecher(intentVerificationDenialLogLeecher);

        String[] subscriberArgs = {"-e", "test.hub.url=" + hubUrl};
        webSubSubscriber.startServer(subscriberBal, subscriberArgs, new int[]{8181});
    }

    @Test
    public void testSubscriptionAndIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecher.waitForText(30000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testContentReceiptForDirectHubNotification() throws BallerinaTestException {
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_INTERNAL, CONTENT_TYPE_JSON);
        internalHubNotificationLogLeecher.waitForText(45000);
    }

    @Test(dependsOnMethods = "testContentReceiptForDirectHubNotification")
    public void testContentReceiptForRemoteHubNotification() throws BallerinaTestException {
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_REMOTE, CONTENT_TYPE_JSON);
        remoteHubNotificationLogLeecher.waitForText(45000);
    }

    @Test(dependsOnMethods = "testContentReceiptForRemoteHubNotification")
    public void testUnsubscriptionIntentVerification() throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                                          + File.separator + "websub" + File.separator +
                                          "websub_test_unsubscription_client.bal").getAbsolutePath();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                subscriptionChanger.runMain(balFile);
            } catch (BallerinaTestException e) {
                //ignored since any errors here would be reflected as test failures
            }
        });
        webSubSubscriber.addLogLeecher(logAbsenceTestLogLeecher);
        unsubscriptionIntentVerificationLogLeecher.waitForText(30000);
    }

    @Test(dependsOnMethods = "testUnsubscriptionIntentVerification",
            description = "Tests that no notifications are received after unsubscription",
            expectedExceptions = BallerinaTestException.class,
            expectedExceptionsMessageRegExp = ".*Timeout expired waiting for matching log.*"
    )
    public void testUnsubscription() throws BallerinaTestException {
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_INTERNAL, CONTENT_TYPE_JSON);
        logAbsenceTestLogLeecher.waitForText(5000);
    }

    @Test(dependsOnMethods = "testUnsubscription")
    public void testRemoteTopicRegistration() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_FORM_URL_ENCODED);
        HttpResponse response = HttpsClientRequest.doPost(hubUrl,
                                                          "hub.mode=subscribe"
                                                                  + "&hub.topic=http://two.websub.topic.com"
                                                                  + "&hub.callback=http://localhost:8181/websub",
                                                          headers,
                                                          webSubSubscriber.getServerHome());
        Assert.assertTrue(response != null);
        Assert.assertEquals(response.getResponseCode(), 202, "Remote topic registration unsuccessful "
                + "to allow registering subscription");
    }

    @Test(dependsOnMethods = "testRemoteTopicRegistration")
    public void testIntentVerificationRejectionForIncorrectTopic() throws BallerinaTestException {
        intentVerificationDenialLogLeecher.waitForText(45000);
        lastTest = true;
    }

    @AfterMethod
    public void cleanup() throws Exception {
        if (!lastTest) {
            return;
        }
        webSubSubscriber.removeAllLeechers();
        webSubSubscriber.shutdownServer();
    }
}
