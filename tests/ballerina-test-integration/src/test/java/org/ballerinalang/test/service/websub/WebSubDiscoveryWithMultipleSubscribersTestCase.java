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
import org.awaitility.Duration;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.awaitility.Awaitility.given;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This class includes an integration scenario which covers the following:
 *  1. Multiple subscriber services binding to the same endpoint
 *  2. Sending a subscription request to the hub and topic identified via WebSub discovery for the resource URL
 *  specified as an annotation
 *  3. Intent verification for subscription request sent following WebSub discovery
 *  4. Prioritizing hub and topic specified as annotations over the resource URL if specified
 */
public class WebSubDiscoveryWithMultipleSubscribersTestCase extends IntegrationTestCase {

    private static String hubUrl = "https://localhost:9494/websub/hub";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - Mode "
            + "[subscribe], Topic [http://www.websubpubtopic.com], Lease Seconds [3600]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed - Mode "
            + "[subscribe], Topic [http://websubpubtopictwo.com], Lease Seconds [1200]";
    private static final String INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "WebSub Notification Received: {\"action\":\"publish\",\"mode\":\"internal-hub\"}";
    private static final String INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "WebSub Notification Received: {\"action\":\"publish\",\"mode\":\"internal-hub-two\"}";

    private LogLeecher intentVerificationLogLeecherOne;
    private LogLeecher intentVerificationLogLeecherTwo;
    private LogLeecher internalHubNotificationLogLeecherOne;
    private LogLeecher internalHubNotificationLogLeecherTwo;

    private ServerInstance ballerinaWebSubPublisherService;
    private ServerInstance ballerinaWebSubSubscriber;
    private ServerInstance ballerinaWebSubPublisher;

    @BeforeClass
    public void setup() throws BallerinaTestException, InterruptedException {
        ballerinaWebSubPublisher = ServerInstance.initBallerinaServer();
        String[] publisherArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                          + File.separator + "websub" + File.separator + "websub_test_publisher.bal").getAbsolutePath(),
                          "-e b7a.websub.hub.port=9494", "-e b7a.websub.hub.remotepublish=true",
                          "-e test.hub.url=" + hubUrl};

        intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
        intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);
        internalHubNotificationLogLeecherOne = new LogLeecher(INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
        internalHubNotificationLogLeecherTwo = new LogLeecher(INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);

        ballerinaWebSubPublisherService = ServerInstance.initBallerinaServer(9290);
        String publisherServiceBal = new File("src" + File.separator + "test" + File.separator + "resources"
             + File.separator + "websub" + File.separator + "websub_test_publisher_service.bal").getAbsolutePath();
        ballerinaWebSubPublisherService.startBallerinaServer(publisherServiceBal);

        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "websub" + File.separator + "websub_test_multiple_subscribers.bal").getAbsolutePath();
        ballerinaWebSubSubscriber = ServerInstance.initBallerinaServer(8484);
        ballerinaWebSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        ballerinaWebSubSubscriber.addLogLeecher(intentVerificationLogLeecherTwo);
        ballerinaWebSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherOne);
        ballerinaWebSubSubscriber.addLogLeecher(internalHubNotificationLogLeecherTwo);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ballerinaWebSubPublisher.runMain(publisherArgs);
            } catch (BallerinaTestException e) {
                //ignored since any errors here would be reflected as test failures
            }
        });

        //Allow to bring up the hub
        given().ignoreException(ConnectException.class).with().pollInterval(Duration.FIVE_SECONDS).and()
                .with().pollDelay(Duration.TEN_SECONDS).await().atMost(60, SECONDS).until(() -> {
            HttpResponse response = HttpsClientRequest.doGet(hubUrl, ballerinaWebSubPublisher.getServerHome());
            return response.getResponseCode() == 202;
        });

        String[] subscriberArgs = {"-e test.hub.url=" + hubUrl};
        ballerinaWebSubSubscriber.startBallerinaServer(subscriberBal, subscriberArgs);

        //Allow to start up the subscriber service
        given().ignoreException(ConnectException.class).with().pollInterval(Duration.FIVE_SECONDS).and()
                .with().pollDelay(Duration.TEN_SECONDS).await().atMost(60, SECONDS).until(() -> {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            headers.put("X-Hub-Signature", "SHA256=5262411828583e9dc7eaf63aede0abac8e15212e06320bb021c433a20f27d553");
            HttpResponse response = HttpClientRequest.doPost(
                    ballerinaWebSubSubscriber.getServiceURLHttp("websub"), "{\"dummy\":\"body\"}",
                    headers);
            return response.getResponseCode() == 202;
        });
    }

    @Test
    public void testDiscoveryAndIntentVerification() throws BallerinaTestException, InterruptedException {
        intentVerificationLogLeecherOne.waitForText(30000);
        intentVerificationLogLeecherTwo.waitForText(30000);
    }

    @Test(dependsOnMethods = "testDiscoveryAndIntentVerification")
    public void testContentReceipt() throws BallerinaTestException {
        internalHubNotificationLogLeecherOne.waitForText(45000);
        internalHubNotificationLogLeecherTwo.waitForText(45000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaWebSubPublisher.stopServer();
        ballerinaWebSubSubscriber.stopServer();
        ballerinaWebSubPublisherService.stopServer();
    }

}
