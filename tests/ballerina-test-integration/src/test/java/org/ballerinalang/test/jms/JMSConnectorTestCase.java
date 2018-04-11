/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.jms;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.jms.util.BallerinaClientHandler;
import org.ballerinalang.test.jms.util.BallerinaServiceHandler;
import org.ballerinalang.test.jms.util.EmbeddedBroker;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

/**
 * Testing Mutual SSL.
 */
public class JMSConnectorTestCase extends IntegrationTestCase {

    private EmbeddedBroker embeddedBroker;

    @BeforeClass()
    public void setUp() throws Exception {
        embeddedBroker = new EmbeddedBroker();
        embeddedBroker.startBroker();
    }

    @Test(description = "Test JMS Connector")
    public void testQueueConsumerProducer() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("jms-queue-consumer.bal",
                                                                             "Message : Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler = new BallerinaClientHandler("jms-queue-producer.bal",
                                                                          "Message successfully sent");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }

    @AfterClass()
    private void cleanup() throws Exception {
        embeddedBroker.stop();
    }

}
