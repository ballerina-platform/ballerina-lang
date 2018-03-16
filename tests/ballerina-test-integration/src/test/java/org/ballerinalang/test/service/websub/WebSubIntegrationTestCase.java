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

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * This class includes an integration scenario which covers the following:
 *  1. Bringing up the Ballerina Hub
 *  2. Sending the subscription request for WebSub Subscriber services on start up, and verifying intent to subscribe,
 *      when the hub sends an intent verification request
 *  3. Functions made available to the Publishers - publishing directly on to the Ballerina Hub or to a Hub by
 *      specifying the URL (usecase: remote hubs)
 *  4. Content Delivery process - by verifying content is delivered when update notification is done for a subscribed
 *      topic - both directly to the hub and specifying hub URL
 *  5. Signature Validation for authenticated content distribution
 */
public class WebSubIntegrationTestCase extends IntegrationTestCase {

    private static final String INTENT_VERIFICATION_SUBSCRIBER_LOG = "\"Intent verified for subscription request\"";
    private static final String DIRECT_BALLERINA_HUB_PAYLOAD = "WebSub Notification Received: {\"action\":\"publish\","
            + "\"mode\":\"ballerina-hub\"}";
    private static final String REMOTE_URL_HUB_PAYLOAD = "WebSub Notification Received: {\"action\":\"publish\","
            + "\"mode\":\"remote-hub\"}";

    private LogLeecher intentVerificationLogLeecher;
    private LogLeecher directHubLogLeecher;
    private LogLeecher urlHubLogLeecher;

    private ServerInstance ballerinaWebSubSubscriber;
    private ServerInstance ballerinaWebSubPublisher;


    @BeforeClass
    public void setUp() throws BallerinaTestException, InterruptedException {
        String[] clientArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "websub" + File.separator + "websub_test_publisher.bal").getAbsolutePath()};

        intentVerificationLogLeecher = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_LOG);
        directHubLogLeecher = new LogLeecher(DIRECT_BALLERINA_HUB_PAYLOAD);
        urlHubLogLeecher = new LogLeecher(REMOTE_URL_HUB_PAYLOAD);

        ballerinaWebSubPublisher = ServerInstance.initBallerinaServer();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ballerinaWebSubPublisher.runMain(clientArgs);
            } catch (BallerinaTestException e) {
                //ignored since any errors here would be reflected as test failures
            }
        });

        //Allow to bring up the hub
        Thread.sleep(10000);

        String subscriberBal = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "websub"
                        + File.separator + "websub_test_subscriber.bal").getAbsolutePath();
        ballerinaWebSubSubscriber = ServerInstance.initBallerinaServer(8181);
        ballerinaWebSubSubscriber.addLogLeecher(intentVerificationLogLeecher);
        ballerinaWebSubSubscriber.addLogLeecher(directHubLogLeecher);
        ballerinaWebSubSubscriber.addLogLeecher(urlHubLogLeecher);
        ballerinaWebSubSubscriber.startBallerinaServer(subscriberBal);

        //Allow for subscription and publishing
        Thread.sleep(10000);
    }

    @Test
    public void testSendingSubscriptionRequestAndIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecher.waitForText(10000);
    }

    @Test
    public void testContentReceiptForDirectHubNotification() throws BallerinaTestException {
        directHubLogLeecher.waitForText(10000);
    }

    @Test
    public void testContentReceiptForUrlHubNotification() throws BallerinaTestException {
        urlHubLogLeecher.waitForText(10000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaWebSubSubscriber.stopServer();
        ballerinaWebSubPublisher.stopServer();
    }

}
