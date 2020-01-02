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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_JSON;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_INTERNAL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_REMOTE;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PATH_SEPARATOR;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.requestUpdate;

/**
 * This class tests the core functionality with WebSub:
 * 1. Bringing up the Ballerina Hub
 * 2. Sending the subscription request for WebSub Subscriber services on start up, and auto verifying intent to
 * subscribe, when the hub sends an intent verification request, since an onIntentVerification resource is not
 * specified, or explicitly verifying intent to subscribe in an onIntentVerification resource
 * 3. Functions made available to the Publishers - publishing directly on to the Ballerina Hub or to a Hub by
 * specifying the URL (usecase: remote hubs)
 * 4. Content Delivery process - by verifying content is delivered when update notification is done for a subscribed
 * topic - both directly to the hub and specifying hub URL
 * 5. Subscription and content distribution when a secret is specified and not specified
 * 6. Rejection of requests that fail validation
 */
@Test(groups = "websub-test")
public class WebSubCoreFunctionalityTestCase extends WebSubBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 45000;
    private static final int WEBSUB_PORT = 23181;
    private BServerInstance webSubSubscriber;
    private BMainInstance subscriptionChanger;

    private static String hubUrl = "http://localhost:23191/websub/hub";
    private static final String INTENT_VERIFICATION_LOG = "ballerina: Intent Verification agreed - Mode [subscribe], " +
            "Topic [http://one.websub.topic.com], Lease Seconds [86400]";
    private static final String EXPLICIT_INTENT_VERIFICATION_LOG = "Intent verified explicitly for subscription " +
            "change request";
    private static final String INTENT_VERIFICATION_LOG_THREE = "ballerina: Intent Verification agreed - " +
            "Mode [subscribe], Topic [http://one.websub.topic.com], Lease Seconds [300]";

    private static final String INTERNAL_HUB_NOTIFICATION_LOG = "WebSub Notification Received: " +
            "{\"action\":\"publish\", \"mode\":\"internal-hub\"}";
    private static final String REMOTE_HUB_NOTIFICATION_LOG = "WebSub Notification Received: " +
            "{\"action\":\"publish\", \"mode\":\"remote-hub\"}";

    private static final String INTERNAL_HUB_NOTIFICATION_LOG_TWO = "WebSub Notification Received by Two: " +
            "{\"action\":\"publish\", \"mode\":\"internal-hub\"}";
    private static final String REMOTE_HUB_NOTIFICATION_LOG_TWO = "WebSub Notification Received by Two: " +
            "{\"action\":\"publish\", \"mode\":\"remote-hub\"}";

    private static final String INTERNAL_HUB_NOTIFICATION_LOG_THREE = "WebSub Notification Received by Three: " +
            "{\"action\":\"publish\", \"mode\":\"internal-hub\"}";
    private static final String QUERY_PARAM_LOG = "Query Params: fooVal=barVal topic=http://one.websub.topic.com";

    private static final String UNSUBSCRIPTION_INTENT_VERIFICATION_LOG = "ballerina: Intent Verification agreed - " +
            "Mode [unsubscribe], Topic [http://one.websub.topic.com]";
    private static final String INTENT_VERIFICATION_DENIAL_LOG = "ballerina: Intent Verification denied - " +
            "Mode [subscribe], Topic [http://two.websub.topic.com]";

    private LogLeecher intentVerificationLogLeecher = new LogLeecher(INTENT_VERIFICATION_LOG);
    private LogLeecher explicitIntentVerificationLogLeecher = new LogLeecher(EXPLICIT_INTENT_VERIFICATION_LOG);
    private LogLeecher intentVerificationLogLeecherThree = new LogLeecher(INTENT_VERIFICATION_LOG_THREE);

    private LogLeecher internalHubNotificationLogLeecher = new LogLeecher(INTERNAL_HUB_NOTIFICATION_LOG);
    private LogLeecher remoteHubNotificationLogLeecher = new LogLeecher(REMOTE_HUB_NOTIFICATION_LOG);

    private LogLeecher internalHubNotificationLogLeecherTwo = new LogLeecher(INTERNAL_HUB_NOTIFICATION_LOG_TWO);
    private LogLeecher remoteHubNotificationLogLeecherTwo = new LogLeecher(REMOTE_HUB_NOTIFICATION_LOG_TWO);

    private LogLeecher internalHubNotificationLogLeecherThree = new LogLeecher(INTERNAL_HUB_NOTIFICATION_LOG_THREE);
    private LogLeecher queryParamLogLeecher = new LogLeecher(QUERY_PARAM_LOG);

    private LogLeecher unsubscriptionIntentVerificationLogLeecher = new LogLeecher(
            UNSUBSCRIPTION_INTENT_VERIFICATION_LOG);
    private LogLeecher logAbsenceTestLogLeecher = new LogLeecher(INTERNAL_HUB_NOTIFICATION_LOG);
    private LogLeecher logAbsenceTestLogLeecherTwo = new LogLeecher(QUERY_PARAM_LOG);
    private LogLeecher intentVerificationDenialLogLeecher = new LogLeecher(INTENT_VERIFICATION_DENIAL_LOG);
    private LogLeecher internalHubNotificationLogLeecherTwoAfterOneUnsubscription =
            new LogLeecher(INTERNAL_HUB_NOTIFICATION_LOG_TWO);


    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);
        subscriptionChanger = new BMainInstance(balServer);
        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources" +
                                                File.separator + "websub" + File.separator + "subscriber" +
                                                File.separator + "test_subscriber.bal").getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecher);
        webSubSubscriber.addLogLeecher(explicitIntentVerificationLogLeecher);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherThree);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecher);
        webSubSubscriber.addLogLeecher(remoteHubNotificationLogLeecher);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(remoteHubNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherThree);
        webSubSubscriber.addLogLeecher(queryParamLogLeecher);
        webSubSubscriber.addLogLeecher(unsubscriptionIntentVerificationLogLeecher);
        webSubSubscriber.addLogLeecher(intentVerificationDenialLogLeecher);

        String[] subscriberArgs = {"--test.hub.url=" + hubUrl};
        webSubSubscriber.startServer(subscriberBal, new String[0], subscriberArgs, new int[]{WEBSUB_PORT});
    }

    @Test
    public void testSubscriptionAndAutomaticIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecher.waitForText(2 * LOG_LEECHER_TIMEOUT);
        intentVerificationLogLeecherThree.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndAutomaticIntentVerification")
    public void testSubscriptionAndExplicitIntentVerification() throws BallerinaTestException {
        explicitIntentVerificationLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
        requestUpdate(PUBLISHER_NOTIFY_URL + PATH_SEPARATOR + WEBSUB_PORT, HUB_MODE_INTERNAL, CONTENT_TYPE_JSON);
        requestUpdate(PUBLISHER_NOTIFY_URL + PATH_SEPARATOR + WEBSUB_PORT, HUB_MODE_REMOTE, CONTENT_TYPE_JSON);
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testContentReceiptForDirectHubNotification() throws BallerinaTestException {
        internalHubNotificationLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testContentReceiptForRemoteHubNotification() throws BallerinaTestException {
        remoteHubNotificationLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testAuthenticatedContentReceiptForDirectHubNotification() throws BallerinaTestException {
        internalHubNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testAuthenticatedContentReceiptForRemoteHubNotification() throws BallerinaTestException {
        remoteHubNotificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testContentReceiptForCallbackWithQueryParams() throws BallerinaTestException {
        internalHubNotificationLogLeecherThree.waitForText(LOG_LEECHER_TIMEOUT);
        queryParamLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriberDetailsRetrievalFromHub")
    public void testUnsubscriptionIntentVerification() throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                          "websub" + File.separator + "subscriber" + File.separator +
                                          "test_unsubscription_client.bal").getAbsolutePath();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                subscriptionChanger.runMain(balFile);
            } catch (BallerinaTestException e) {
                //ignored since any errors here would be reflected as test failures
            }
        });
        webSubSubscriber.addLogLeecher(logAbsenceTestLogLeecher);
        webSubSubscriber.addLogLeecher(logAbsenceTestLogLeecherTwo);
        unsubscriptionIntentVerificationLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testUnsubscriptionIntentVerification",
            description = "Tests that no notifications are received after unsubscription",
            expectedExceptions = BallerinaTestException.class,
            expectedExceptionsMessageRegExp = ".*Timeout expired waiting for matching log.*")
    public void testUnsubscription() throws BallerinaTestException {
        try {
            HttpResponse response = HttpClientRequest.doGet("http://localhost:23080/publisher/unsubscribe");
            Assert.assertEquals(response.getData(), "unsubscription successful");
        } catch (IOException e) {
            throw new BallerinaTestException("Error requesting unsubscription");
        }

        requestUpdate(PUBLISHER_NOTIFY_URL + PATH_SEPARATOR + "skip_subscriber_check", HUB_MODE_INTERNAL,
                      CONTENT_TYPE_JSON);
        logAbsenceTestLogLeecher.waitForText(5000);
        logAbsenceTestLogLeecherTwo.waitForText(2500);
        internalHubNotificationLogLeecherTwoAfterOneUnsubscription.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testRemoteTopicRegistration() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_FORM_URL_ENCODED);
        HttpResponse response = HttpClientRequest.doPost(hubUrl,
                                                  "hub.mode=subscribe" +
                                                                  "&hub.topic=http://two.websub.topic.com" +
                                                                  "&hub.callback=http://localhost:23181/websub",
                                                  headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 202, "Remote topic registration unsuccessful "
                + "to allow registering subscription");
    }

    @Test(dependsOnMethods = "testRemoteTopicRegistration")
    public void testIntentVerificationRejectionForIncorrectTopic() throws BallerinaTestException {
        intentVerificationDenialLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testSignatureValidationFailure() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Hub-Signature", "SHA256=incorrect583e9dc7eaf63aede0abac8e15212e06320bb021c433a20f27d553");
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(
                webSubSubscriber.getServiceURLHttp(23181, "subscriberWithNoPathInAnnot"),
                "{\"dummy\":\"body\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 404);
        Assert.assertEquals(response.getData(), "validation failed for notification");
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testRejectionIfNoSignature() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(
                webSubSubscriber.getServiceURLHttp(23181, "websubThree"), "{\"dummy\":\"body\"}",
                headers);
        Assert.assertEquals(response.getResponseCode(), 404);
        Assert.assertEquals(response.getData(), "validation failed for notification");
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testSubscriberDetailsRetrievalFromHub() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-topic", "http://one.websub.topic.com");
        HttpResponse response = HttpClientRequest.doGet("http://localhost:23080/publisher/topicInfo", headers);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertTrue(response.getData().contains("callback=http://localhost:23181/websub") &&
                                  response.getData().contains("callback=http://localhost:23181" +
                                                                      "/subscriberWithNoPathInAnnot") &&
                                  response.getData().contains("callback=http://localhost:23181/websubThree?topic=http" +
                                                                      "://one.websub.topic.com&fooVal=barVal"));
    }

    @Test(dependsOnMethods = "testSubscriptionAndExplicitIntentVerification")
    public void testAvailableTopicsRetrievalFromHub() throws IOException {
        HttpResponse response = HttpClientRequest.doGet("http://localhost:23080/publisher/topicInfo");

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"Topic_1\":\"http://one.websub.topic.com\", " +
                "\"Topic_2\":\"http://three.websub.topic.com\", \"Topic_3\":\"http://four.websub.topic.com\", " +
                "\"Topic_4\":\"http://one.redir.topic.com\", \"Topic_5\":\"http://two.redir.topic.com\", " +
                "\"Topic_6\":\"http://two.websub.topic.com\"}");
    }

    @AfterClass
    public void teardown() throws Exception {
        webSubSubscriber.removeAllLeechers();
        webSubSubscriber.shutdownServer();
    }
}
