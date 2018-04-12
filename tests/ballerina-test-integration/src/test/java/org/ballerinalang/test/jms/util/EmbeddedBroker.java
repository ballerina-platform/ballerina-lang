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

package org.ballerinalang.test.jms.util;

import io.ballerina.messaging.broker.amqp.AmqpServerConfiguration;
import io.ballerina.messaging.broker.amqp.Server;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.core.BrokerImpl;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;

public class EmbeddedBroker {

    private BrokerImpl broker;
    private Server amqpServer;

    public void startBroker() throws Exception {
        StartupContext startupContext = new StartupContext();
        BrokerTestConfigProvider configProvider = new BrokerTestConfigProvider();
        BrokerCommonConfiguration commonConfig = new BrokerCommonConfiguration();
        commonConfig.setEnableInMemoryMode(true);
        configProvider.registerConfigurationObject(BrokerCommonConfiguration.NAMESPACE,
                                                   commonConfig);
        BrokerCoreConfiguration brokerCoreConfiguration = new BrokerCoreConfiguration();
        configProvider.registerConfigurationObject(BrokerCoreConfiguration.NAMESPACE, brokerCoreConfiguration);
        AmqpServerConfiguration serverConfiguration = new AmqpServerConfiguration();
        serverConfiguration.getPlain().setPort("5772");
        configProvider.registerConfigurationObject(AmqpServerConfiguration.NAMESPACE, serverConfiguration);
        startupContext.registerService(BrokerConfigProvider.class, configProvider);
        broker = new BrokerImpl(startupContext);
        broker.startMessageDelivery();
        amqpServer = new Server(startupContext);
        amqpServer.start();
    }

    public void stop() throws Exception {
        amqpServer.stop();
        amqpServer.awaitServerClose();
        broker.shutdown();
    }
}
