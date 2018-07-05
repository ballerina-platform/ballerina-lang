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

    @Test(description = "Test JMS Connector Queue consumer producer")
    public void testQueueConsumerProducer() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("jms_queue_consumer.bal",
                                                                             "Message : Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler = new BallerinaClientHandler("jms_queue_producer.bal",
                                                                          "Message successfully sent by QueueSender");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }

    @Test(description = "Test JMS Connector topic subscriber producer")
    public void testTopicSubscriberPublisher() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("jms_topic_subscriber.bal",
                                                                             "Message : Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler
                = new BallerinaClientHandler("jms_topic_publisher.bal",
                                             "Message successfully sent by TopicPublisher");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }

    @Test(description = "Test JMS Connector durable topic subscriber producer")
    public void testDurableTopicSubscriberPublisher() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("jms_durable_topic_subscriber.bal",
                                                                             "Message : Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler
                = new BallerinaClientHandler("jms_durable_topic_publisher.bal",
                                             "Message successfully sent by DurableTopicPublisher");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }

    @Test(description = "Test JMS Connector simple queue receiver and producer")
    public void testJmsSimpleQueueReceiverProducer() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("jms_simple_queue_consumer.bal",
                                                                             "Message : Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler
                = new BallerinaClientHandler("jms_simple_queue_producer.bal",
                                             "Message successfully sent by jms:SimpleQueueSender");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }

    @Test(description = "Test JMS property setters and getters")
    public void testJMSProperties() throws Exception {
        BallerinaServiceHandler serviceHandler =
                new BallerinaServiceHandler("jms_properties_queue_receiver.bal",
                                            "booleanVal:false|intVal:10|floatVal:10.5|stringVal:TestString|"
                                                    + "message:Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler
                = new BallerinaClientHandler("jms_properties_queue_sender.bal",
                                             "Message successfully sent by jms:SimpleQueueSender");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }

    @Test(description = "Test MB Connector simple queue receiver and producer")
    public void testMbSimpleQueueReceiverProducer() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("mb_simple_queue_consumer.bal",
                                                                             "Message : Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler
                = new BallerinaClientHandler("mb_simple_queue_producer.bal",
                                             "Message successfully sent by mb:SimpleQueueSender");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }

    @Test(description = "Test MB Connector simple topic subscriber and publisher")
    public void testMbSimpleTopicSubscriberPublisher() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("mb_simple_topic_subscriber.bal",
                                                                             "Message : Test Text");
        serviceHandler.start();

        BallerinaClientHandler clientHandler
                = new BallerinaClientHandler("mb_simple_topic_publisher.bal",
                                             "Message successfully sent by mb:SimpleTopicPublisher");
        clientHandler.start();

        serviceHandler.waitForText(TimeUnit.SECONDS, 20);
        clientHandler.waitForText(TimeUnit.SECONDS, 20);

        serviceHandler.stop();
        clientHandler.stop();
    }
    @Test(description = "Test MB Connector simple topic subscriber and publisher")
    public void testJMSMapMessagePublisherAndSubscriber() throws Exception {
        BallerinaServiceHandler serviceHandler = new BallerinaServiceHandler("jms_map_message_subscriber.bal",
                "1abctrue1.2abcde");
        serviceHandler.start();

        BallerinaClientHandler clientHandler
                = new BallerinaClientHandler("jms_map_message_publisher.bal",
                "Message successfully sent by TopicPublisher");
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
