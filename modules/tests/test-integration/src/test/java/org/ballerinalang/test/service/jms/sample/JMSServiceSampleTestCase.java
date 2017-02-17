/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.jms.sample;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.JMSBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

/**
 * Testing the JMSService sample located in
 * ballerina_home/samples/jmsServiceWithActiveMq/jmsServiceWithActiveMq.bal.
 */
public class JMSServiceSampleTestCase extends IntegrationTestCase {
    private static final Logger logger = LoggerFactory.getLogger(JMSServiceSampleTestCase.class);
    private int messageCount = 0;
    private String mapProperty = "queue message count";

    /**
     * JMS ActiveMq sample listens to a particular queue and then sends that message to a topic. Another service
     * listens to that particular topic. In this test case, we publish the message to the queue and then listens to
     * the topic, which the same message is passed to. We are checking the content and the message count.
     *
     * @throws InterruptedException Interrupted Exception
     * @throws JMSException JMS Exception
     */
    @Test(description = "Test jms service with activemq sample")
    public void testPassingJMSMessageEndToEnd()
            throws InterruptedException, JMSException {
        receiveMessagesFromTopic("ballerinatopic");
        publishMessagesToQueue("ballerinaqueue");
        // Time for getting the relevant messages as these messages need to go through another queue/
        Thread.sleep(1000);
        Assert.assertEquals(messageCount, 10, "The required number of messages are not delivered to the subscriber "
                + "listening to topic ballerina");
    }

    /**
     * To publish the messages to a queue.
     *
     * @throws JMSException         JMS Exception
     * @throws InterruptedException Interrupted exception while waiting in between messages
     */
    private void publishMessagesToQueue(String queueName) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = JMSBroker.getConnectionFactory();
        QueueConnection queueConn = (QueueConnection) connectionFactory.createConnection();
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = queueSession.createQueue(queueName);
        MessageProducer queueSender = queueSession.createProducer(destination);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        for (int index = 0; index < 10; index++) {
            MapMessage queueMessage = queueSession.createMapMessage();
            queueMessage.setInt(mapProperty, index);
            queueSender.send(queueMessage);
            logger.info("Map Message  " + (index + 1) + " sent");
            Thread.sleep(1000);
        }
        queueConn.close();
        queueSession.close();
        queueSender.close();
    }

    /**
     * To receive a message from a topic.
     *
     * @throws JMSException         JMS Exception
     * @throws InterruptedException Interrupted exception while waiting in between messages
     */
    private void receiveMessagesFromTopic(String topicName) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = JMSBroker.getConnectionFactory();

        TopicConnection queueConn = (TopicConnection) connectionFactory.createConnection();
        queueConn.start();
        TopicSession queueSession = queueConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic destination = queueSession.createTopic(topicName);
        TopicSubscriber queueReceiver = queueSession.createSubscriber(destination);
        queueReceiver.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof MapMessage) {
                    try {
                        int value = Integer.parseInt(((MapMessage) message).getString(mapProperty));
                        Assert.assertTrue(value >= 0 && value < 10, value + " is not within the expected value");
                    } catch (JMSException e) {
                        logger.error("JMS Exception occured while getting the map message", e);
                    }
                    messageCount++;
                }
            }
        });
    }
}
