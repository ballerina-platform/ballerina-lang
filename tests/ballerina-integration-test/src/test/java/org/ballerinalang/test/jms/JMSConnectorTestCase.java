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
 */
package org.ballerinalang.test.jms;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.Utils;
import org.ballerinalang.test.jms.util.EmbeddedBroker;
import org.ballerinalang.test.jms.util.JMSClientHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Testing Mutual SSL.
 */
@Test(groups = "jms-test")
public class JMSConnectorTestCase extends BaseTest {

    private BServerInstance serverInstance;
    private EmbeddedBroker embeddedBroker;
    private JMSClientHandler clientHandler;

    @BeforeGroups(value = "jms-test", alwaysRun = true)
    public void start() throws BallerinaTestException {

        int[] requiredPorts = new int[]{9090, 9091, 9092, 9093, 9094, 9095, 9096, 9097, 9098};
        Utils.checkPortsAvailability(requiredPorts);

        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "jms").getAbsolutePath();
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(basePath, "jmsservices");
    }

    @AfterGroups(value = "jms-test", alwaysRun = true)
    public void stop() throws BallerinaTestException {
        serverInstance.shutdownServer();
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        embeddedBroker = new EmbeddedBroker();
        embeddedBroker.startBroker();
        clientHandler = new JMSClientHandler(balServer);
    }

    @Test(description = "Test JMS Connector Queue consumer producer")
    public void testQueueConsumerProducer() throws Exception {
        LogLeecher serverLog = addServerLogLeecher("Message : Test Text");

        LogLeecher clientLog = clientHandler.start("jms_queue_producer.bal", "Message successfully sent by " +
                "QueueSender");

        waitForText(serverLog, TimeUnit.SECONDS, 20);
        waitForText(clientLog, TimeUnit.SECONDS, 20);

    }

    @Test(description = "Test JMS Connector topic subscriber producer")
    public void testTopicSubscriberPublisher() throws Exception {
        LogLeecher serverLog = addServerLogLeecher("Message : Test Text");

        LogLeecher clientLog = clientHandler.start("jms_topic_publisher.bal", "Message successfully sent by " +
                "TopicPublisher");

        waitForText(serverLog, TimeUnit.SECONDS, 20);
        waitForText(clientLog, TimeUnit.SECONDS, 20);

    }

    @Test(description = "Test JMS Connector durable topic subscriber producer")
    public void testDurableTopicSubscriberPublisher() throws Exception {
        LogLeecher serverLog = addServerLogLeecher("Message : Test Text");

        LogLeecher clientLog = clientHandler.start("jms_durable_topic_publisher.bal",
                "Message successfully sent by DurableTopicPublisher");

        waitForText(serverLog, TimeUnit.SECONDS, 20);
        waitForText(clientLog, TimeUnit.SECONDS, 20);

    }

    @Test(description = "Test JMS Connector simple queue receiver and producer")
    public void testJmsSimpleQueueReceiverProducer() throws Exception {
        LogLeecher serverLog = addServerLogLeecher("Message : Test Text");

        LogLeecher clientLog = clientHandler.start("jms_simple_queue_producer.bal",
                "Message successfully sent by jms:SimpleQueueSender");

        waitForText(serverLog, TimeUnit.SECONDS, 20);
        waitForText(clientLog, TimeUnit.SECONDS, 20);

    }

    @Test(description = "Test JMS property setters and getters")
    public void testJMSProperties() throws Exception {
        String expectedLog = "booleanVal:false|intVal:10|floatVal:10.5|stringVal:TestString|message:Test Text";
        LogLeecher serverLog = addServerLogLeecher(expectedLog);

        LogLeecher clientLog = clientHandler.start("jms_properties_queue_sender.bal",
                "Message successfully sent by jms:SimpleQueueSender");

        waitForText(serverLog, TimeUnit.SECONDS, 20);
        waitForText(clientLog, TimeUnit.SECONDS, 20);

    }

    @Test(description = "Test MB Connector simple topic subscriber and publisher")
    public void testJMSMapMessagePublisherAndSubscriber() throws Exception {

        LogLeecher serverLog = addServerLogLeecher("1abctrue1.2");

        LogLeecher clientLog = clientHandler.start("jms_map_message_publisher.bal",
                "Message successfully sent by TopicPublisher");

        waitForText(serverLog, TimeUnit.SECONDS, 20);
        waitForText(clientLog, TimeUnit.SECONDS, 20);

    }

    @AfterClass(alwaysRun = true)
    private void cleanup() throws Exception {
        embeddedBroker.stop();
    }

    private void waitForText(LogLeecher logLeecher, TimeUnit timeUnit, int length) throws BallerinaTestException {
        logLeecher.waitForText(timeUnit.toMillis(length));
    }

    private LogLeecher addServerLogLeecher(String expectedLog) {
        LogLeecher logLeecher = new LogLeecher(expectedLog);
        serverInstance.addLogLeecher(logLeecher);
        return logLeecher;
    }
}
