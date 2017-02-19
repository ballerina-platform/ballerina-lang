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

package org.ballerinalang.test.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.ballerinalang.test.context.Constant;

import javax.jms.ConnectionFactory;

/**
 * JMS Provider for the test case.
 */
public class JMSTestBroker {
    private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Constant.ACTIVEMQ_PROVIDER_URL);
    private BrokerService broker;
    private static JMSTestBroker instance = new JMSTestBroker();

    /**
     * Creates a JMS Broker.
     */
    private JMSTestBroker() {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(true);
    }

    /**
     * To get the instance of the JMS Broker.
     * @return instance of the JMS Broker
     */
    public static JMSTestBroker getInstance() {
        return instance;
    }

    /**
     * To get the connection factory of the particular jms provider.
     *
     * @return Connection factory of the particular jms provider
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * To start the broker.
     *
     * @throws Exception Exception that can be thrown when adding the connector
     */
    public void startBroker() throws Exception {
        if (!broker.isStarted()) {
            broker.addConnector("tcp://localhost:61616");
            broker.start();
        }
    }

    /**
     * To stop the JMS broker
     *
     * @throws Exception Exception that can be thrown when stopping the broker
     */
    public void stopBroker() throws Exception {
        if (broker.isStarted() && !broker.isStopped()) {
            broker.stop();
        }
    }
}
