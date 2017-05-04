/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.output.transport.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.output.transport.jms.util.JMSClient;

public class JMSOutputTestCase {

    private static final String TOPIC_NAME = "DAS_JMS_OUTPUT_TEST";
    private static final String PROVIDER_URL = "vm://localhost?broker.persistent=false";

    @BeforeClass
    public static void setup() throws InterruptedException {
        // starting the ActiveMQ consumer
        Thread listenerThread = new Thread(new JMSClient("activemq", "", "DAS_JMS_OUTPUT_TEST"));
        listenerThread.start();
        Thread.sleep(5000);
    }

    @Test
    public void jmsTopicPublishTest() throws InterruptedException {
        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "@sink(type='jms', @map(type='text'), "
                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "provider.url='vm://localhost',"
                + "destination='DAS_JMS_OUTPUT_TEST', "
                + "connection.factory.type='queue',"
                + "connection.factory.jndi.name='QueueConnectionFactory'"
                +")" +
                "define stream inputStream (name string, age int, country string);";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.
                createExecutionPlanRuntime(inStreamDefinition);
        InputHandler inputStream = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputStream.send(new Object[]{"JAMES", 23, "USA"});
        inputStream.send(new Object[]{"MIKE", 23, "Germany"});
        Thread.sleep(10000);
        executionPlanRuntime.shutdown();
        //todo: add a log assertion here
    }
}
