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

package org.ballerinalang.test.service.websub.advanced;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL_TWO;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.requestUpdateWithContent;

/**
 * This class topic/subscription persistence.
 */
@Test(groups = "websub-test")
public class WebSubPersistenceTestCase extends WebSubAdvancedBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 45000;
    private BServerInstance webSubSubscriber;

    private static final String PUBLISHER_ONE_BEFORE_CONTENT =
            "{\"action\":\"publish before hub restart\", \"id\":\"one\"}";
    private static final String PUBLISHER_TWO_BEFORE_CONTENT =
            "{\"action\":\"publish before hub restart\", \"id\":\"two\"}";
    private static final String PUBLISHER_ONE_AFTER_CONTENT =
            "{\"action\":\"publish after hub restart\", \"id\":\"one\"}";
    private static final String PUBLISHER_TWO_AFTER_CONTENT =
            "{\"action\":\"publish after hub restart\", \"id\":\"two\"}";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - " +
            "Mode [subscribe], Topic [http://one.persistence.topic.com], Lease Seconds [3600]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed - " +
            "Mode [subscribe], Topic [http://two.persistence.topic.com], Lease Seconds [1200]";
    private static final String NOTIFICATION_SUBSCRIBER_ONE_BEFORE_LOG =
            "WebSub Notification Received by One: " + PUBLISHER_ONE_BEFORE_CONTENT;
    private static final String NOTIFICATION_SUBSCRIBER_TWO_BEFORE_LOG =
            "WebSub Notification Received by Two: " + PUBLISHER_TWO_BEFORE_CONTENT;
    private static final String NOTIFICATION_SUBSCRIBER_ONE_AFTER_LOG =
            "WebSub Notification Received by One: " + PUBLISHER_ONE_AFTER_CONTENT;
    private static final String NOTIFICATION_SUBSCRIBER_TWO_AFTER_LOG =
            "WebSub Notification Received by Two: " + PUBLISHER_TWO_AFTER_CONTENT;

    private LogLeecher intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher notificationBeforeLogLeecherOne = new LogLeecher(NOTIFICATION_SUBSCRIBER_ONE_BEFORE_LOG);
    private LogLeecher notificationBeforeLogLeecherTwo = new LogLeecher(NOTIFICATION_SUBSCRIBER_TWO_BEFORE_LOG);
    private LogLeecher notificationAfterLogLeecherOne = new LogLeecher(NOTIFICATION_SUBSCRIBER_ONE_AFTER_LOG);
    private LogLeecher notificationAfterLogLeecherTwo = new LogLeecher(NOTIFICATION_SUBSCRIBER_TWO_AFTER_LOG);

    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);
        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources" +
                                                File.separator + "websub" + File.separator +
                                                "test_subscribers_at_persistence_enabled_hub.bal").getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(notificationBeforeLogLeecherOne);
        webSubSubscriber.addLogLeecher(notificationBeforeLogLeecherTwo);
        webSubSubscriber.addLogLeecher(notificationAfterLogLeecherOne);
        webSubSubscriber.addLogLeecher(notificationAfterLogLeecherTwo);

        webSubSubscriber.startServer(subscriberBal, new String[0], new int[]{8383});
    }

    @Test
    public void testDiscoveryAndIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
        intentVerificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
        requestUpdateWithContent(PUBLISHER_NOTIFY_URL, PUBLISHER_ONE_BEFORE_CONTENT);
        requestUpdateWithContent(PUBLISHER_NOTIFY_URL_TWO, PUBLISHER_TWO_BEFORE_CONTENT);
    }

    @Test(dependsOnMethods = "testDiscoveryAndIntentVerification")
    public void testContentReceiptBeforeRestart() throws BallerinaTestException, IOException {
        notificationBeforeLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
        notificationBeforeLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);

        // Restart the hub and trigger content delivery requests
        HttpResponse response = HttpClientRequest.doPost("http://localhost:8080/helperService/restartHub", "",
                                                         Collections.emptyMap());
        if (response.getResponseCode() != HttpResponseStatus.ACCEPTED.code()) {
            Assert.fail("hub restart failed!");
        }
        requestUpdateWithContent(PUBLISHER_NOTIFY_URL, PUBLISHER_ONE_AFTER_CONTENT);
        requestUpdateWithContent(PUBLISHER_NOTIFY_URL_TWO, PUBLISHER_TWO_AFTER_CONTENT);
    }

    @Test(dependsOnMethods = "testContentReceiptBeforeRestart")
    public void testContentReceiptAfterRestart() throws BallerinaTestException {
        notificationAfterLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
        notificationAfterLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @AfterClass
    private void teardown() throws Exception {
        webSubSubscriber.shutdownServer();
    }
}
