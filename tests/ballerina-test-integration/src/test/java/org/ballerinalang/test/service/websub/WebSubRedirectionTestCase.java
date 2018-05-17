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
 * This class includes integration scenarios which covers redirection with WebSub:
 *  1. 301 - Permanent Redirect for the Topic URL for which discovery happens
 *  2. 302 - Temporary Redirect for the Topic URL for which discovery happens
 *  3. 307 - Temporary redirect to a new hub for subscription
 *  4. 308 - Permanent redirect to a new hub for subscription
 */
public class WebSubRedirectionTestCase extends IntegrationTestCase {

    private static String hubUrl = "https://localhost:9595/websub/hub";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - Mode "
            + "[subscribe], Topic [http://redirectiontopicone.com], Lease Seconds [3600]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed - Mode "
            + "[subscribe], Topic [http://redirectiontopictwo.com], Lease Seconds [1200]";

    private LogLeecher intentVerificationLogLeecherOne;
    private LogLeecher intentVerificationLogLeecherTwo;

    private ServerInstance ballerinaWebSubHubService;
    private ServerInstance ballerinaWebSubPublishers;
    private ServerInstance ballerinaWebSubSubscribers;

    @BeforeClass
    public void setup() throws BallerinaTestException, InterruptedException {
        ballerinaWebSubHubService = ServerInstance.initBallerinaServer();
        String[] publisherArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                                  + File.separator + "websub" + File.separator + "redirection" + File.separator
                                  + "hub_service.bal").getAbsolutePath()};

        intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
        intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);

        ballerinaWebSubPublishers = ServerInstance.initBallerinaServer(9291);
        String publishersBal = new File("src" + File.separator + "test" + File.separator + "resources"
                                + File.separator + "websub" + File.separator + "redirection" + File.separator
                                + "publishers.bal").getAbsolutePath();
        ballerinaWebSubPublishers.startBallerinaServer(publishersBal);

        String subscribersBal = new File("src" + File.separator + "test" + File.separator + "resources"
                                    + File.separator + "websub" + File.separator + "redirection" + File.separator
                                    + "subscribers.bal").getAbsolutePath();
        ballerinaWebSubSubscribers = ServerInstance.initBallerinaServer(8585);
        ballerinaWebSubSubscribers.addLogLeecher(intentVerificationLogLeecherOne);
        ballerinaWebSubSubscribers.addLogLeecher(intentVerificationLogLeecherTwo);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ballerinaWebSubHubService.runMain(publisherArgs);
            } catch (BallerinaTestException e) {
                //ignored since any errors here would be reflected as test failures
            }
        });

        //Allow to bring up the hub
        given().ignoreException(ConnectException.class).with().pollInterval(Duration.FIVE_SECONDS).and()
                .with().pollDelay(Duration.TEN_SECONDS).await().atMost(60, SECONDS).until(() -> {
            HttpResponse response = HttpsClientRequest.doGet(hubUrl, ballerinaWebSubHubService.getServerHome());
            return response.getResponseCode() == 202;
        });

        String[] subscriberArgs = {};
        ballerinaWebSubSubscribers.startBallerinaServer(subscribersBal, subscriberArgs);

        //Allow to start up the subscriber service
        given().ignoreException(ConnectException.class).with().pollInterval(Duration.FIVE_SECONDS).and()
                .with().pollDelay(Duration.TEN_SECONDS).await().atMost(60, SECONDS).until(() -> {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            headers.put("X-Hub-Signature", "SHA256=5262411828583e9dc7eaf63aede0abac8e15212e06320bb021c433a20f27d553");
            HttpResponse response = HttpClientRequest.doPost(
                    ballerinaWebSubSubscribers.getServiceURLHttp("websub"), "{\"dummy\":\"body\"}",
                    headers);
            return response.getResponseCode() == 202;
        });
    }

    @Test
    public void testTopicMovedPermanentlyAndHubTemporaryRedirect() throws BallerinaTestException, InterruptedException {
        intentVerificationLogLeecherOne.waitForText(10000);
    }

    @Test
    public void testTopicRedirectFoundAndHubPermanentRedirect() throws BallerinaTestException, InterruptedException {
        intentVerificationLogLeecherTwo.waitForText(10000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaWebSubHubService.stopServer();
        ballerinaWebSubSubscribers.stopServer();
        ballerinaWebSubPublishers.stopServer();
    }

}
