/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.extension.output.transport.jms.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Consumes JMS messages on either topics or queues.
 */
public class JMSClient implements Runnable {
    private static Log log = LogFactory.getLog(JMSClient.class);

    private String broker, topic, queue;

    public JMSClient(String broker, String topic, String queue) {
        this.broker = broker;
        this.topic = topic;
        this.queue = queue;
    }

    public void listen() throws InterruptedException {
        Properties properties = new Properties();
        try {
            if ("qpid".equalsIgnoreCase(broker)) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("qpid.properties"));
            } else if ("activemq".equalsIgnoreCase(broker)) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("activemq.properties"));
            } else if ("mb".equalsIgnoreCase(broker)) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mb.properties"));
            } else {
                log.error("Entered broker is invalid! ");
            }
            if (topic != null && topic.isEmpty() || topic.equals("\"\"")) {
                topic = null;
            }
            if (queue != null && queue.isEmpty() || queue.equals("\"\"")) {
                queue = null;
            }
            if (topic == null && queue == null) {
                log.error("Enter topic value or queue value! ");
            } else if (topic != null) {
                Context context = new InitialContext(properties);
                TopicConnectionFactory topicConnectionFactory =
                        (TopicConnectionFactory) context.lookup("ConnectionFactory");
                TopicConsumer topicConsumer = new TopicConsumer(topicConnectionFactory, topic);
                Thread consumerThread = new Thread(topicConsumer);
                log.info("Starting" + broker + "consumerTopic thread...");
                consumerThread.start();
                Thread.sleep(1 * 60000);
                log.info("Shutting down " + broker + " consumerTopic...");
                topicConsumer.shutdown();
            } else {
                Context context = new InitialContext(properties);
                QueueConnectionFactory queueConnectionFactory =
                        (QueueConnectionFactory) context.lookup("ConnectionFactory");
                QueueConsumer queueConsumer = new QueueConsumer(queueConnectionFactory, queue);
                Thread consumerThread = new Thread(queueConsumer);
                log.info("Starting" + broker + "consumerQueue thread...");
                consumerThread.start();
                Thread.sleep(1 * 60000);
                log.info("Shutting down " + broker + " consumerQueue...");
                queueConsumer.shutdown();
            }
        } catch (IOException e) {
            log.error("Cannot read properties file from resources. " + e.getMessage(), e);
        } catch (NamingException e) {
            log.error("Invalid properties in the properties " + e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        try {
            listen();
        } catch (InterruptedException e) {
            log.error("Error starting the JMS consumer: ", e);
        }
    }
}
