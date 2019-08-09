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
/**
 * This class includes integration scenarios which covers redirection with WebSub:
 * 1. 301 - Permanent Redirect for the Topic URL for which discovery happens
 * 2. 302 - Temporary Redirect for the Topic URL for which discovery happens
 * 3. 307 - Temporary redirect to a new hub for subscription
 * 4. 308 - Permanent redirect to a new hub for subscription
 */
@Test(groups = "websub-test")
public class WebSubRedirectionTestCase extends WebSubBaseTest {
    private static final int LOG_LEECHER_TIMEOUT = 45000;
    private BServerInstance webSubSubscriber;

    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - Mode "
            + "[subscribe], Topic [http://one.redir.topic.com], Lease Seconds [3600]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed - Mode "
            + "[subscribe], Topic [http://two.redir.topic.com], Lease Seconds [1200]";

    private LogLeecher intentVerificationLogLeecherOne;
    private LogLeecher intentVerificationLogLeecherTwo;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);

        intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
        intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);

        String subscribersBal = new File("src" + File.separator + "test" + File.separator + "resources" +
                                                 File.separator + "websub" + File.separator + "subscriber" +
                                                 File.separator + "test_redirected_subscribers.bal").getAbsolutePath();
    
        String sourceRoot = new File("src" + File.separator + "test" + File.separator + "resources" +
                                         File.separator + "websub" + File.separator + "subscriber").getAbsolutePath();
        
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherTwo);

        String[] subscriberArgs = {};
        webSubSubscriber.startServer(sourceRoot, subscribersBal, subscriberArgs, new int[]{23484});
    }

    @Test
    public void testTopicMovedPermanentlyAndHubTemporaryRedirect() throws BallerinaTestException {
        intentVerificationLogLeecherOne.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(dependsOnMethods = "testTopicMovedPermanentlyAndHubTemporaryRedirect")
    public void testTopicRedirectFoundAndHubPermanentRedirect() throws BallerinaTestException {
        intentVerificationLogLeecherTwo.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @AfterClass
    private void teardown() throws Exception {
        webSubSubscriber.shutdownServer();
    }
}
