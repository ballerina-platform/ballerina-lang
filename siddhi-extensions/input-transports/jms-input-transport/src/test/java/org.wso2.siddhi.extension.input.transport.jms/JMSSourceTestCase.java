/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.siddhi.extension.input.transport.jms;

import junit.framework.Assert;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.input.transport.jms.client.JMSClient;

import java.util.ArrayList;
import java.util.List;

public class JMSSourceTestCase {
    private final String PROVIDER_URL = "vm://localhost?broker.persistent=false";
    private List<String> receivedEventNameList;

    @Test
    public void TestJMSTopicSource() throws InterruptedException {
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "@source(type='jms', @map(type='text'), "
                + "factoryInitial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "providerUrl='vm://localhost',"
                + "destination='DAS_JMS_TEST', "
                + "connectionFactoryType='topic',"
                + "connectionFactoryJNDIName='QueueConnectionFactory',"
                + "transport.jms.SubscriptionDurable='true', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'"
                + ")" +
                "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();

        // publishing events
        publishEvents("DAS_JMS_TEST", null, "activemq", "text", "src/test/resources/events/events_text.txt");

        List<String> expected = new ArrayList<>(2);
        expected.add("\nJohn");
        expected.add("\nMike");
        Assert.assertEquals("JMS Input Transport expected input not received", expected, receivedEventNameList);
    }

    private void publishEvents(String topicName, String queueName, String broker, String format, String filePath)
            throws InterruptedException {
        JMSClient jmsClient = new JMSClient();
        jmsClient.sendJMSEvents(filePath, topicName, queueName, format, broker, PROVIDER_URL);
        Thread.sleep(5000);
    }
}
