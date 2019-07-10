/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.messaging.artemis.externimpl.connection;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function for Artemis connection creation.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA, packageName = ArtemisConstants.ARTEMIS,
        functionName = "createConnection",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = ArtemisConstants.CONNECTION_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS)
)
public class CreateConnection {
    private static final Logger logger = LoggerFactory.getLogger(CreateConnection.class);

    public static void createConnection(Strand strand, ObjectValue connection, String url, MapValue config) {
        try {
            long connectionTTL = config.getIntValue(ArtemisConstants.TIME_TO_LIVE);
            long callTimeout = config.getIntValue(ArtemisConstants.CALL_TIMEOUT);
            int consumerWindowSize = ArtemisUtils.getIntFromConfig(
                    config, ArtemisConstants.CONSUMER_WINDOW_SIZE, logger);
            int consumerMaxRate = ArtemisUtils.getIntFromConfig(config, ArtemisConstants.CONSUMER_MAX_RATE, logger);
            int producerWindowSize = ArtemisUtils.getIntFromConfig(
                    config, ArtemisConstants.PRODUCER_WINDOW_SIZE, logger);
            int producerMaxRate = ArtemisUtils.getIntFromConfig(config, ArtemisConstants.PRODUCER_MAX_RATE, logger);
            long retryInterval = config.getIntValue(ArtemisConstants.RETRY_INTERVAL);
            double retryIntervalMultiplier = config.getFloatValue(ArtemisConstants.RETRY_INTERVAL_MULTIPLIER);
            long maxRetryInterval = config.getIntValue(ArtemisConstants.MAX_RETRY_INTERVAL);
            int reconnectAttempts = ArtemisUtils.getIntFromConfig(config, ArtemisConstants.RECONNECT_ATTEMPTS, logger);
            int initialConnectAttempts = ArtemisUtils.getIntFromConfig(
                    config, ArtemisConstants.INITIAL_CONNECT_ATTEMPTS, logger);

            ServerLocator connectionPool = ActiveMQClient.createServerLocator(url);

            //Add config values to the serverLocator before creating the sessionFactory
            connectionPool.setConnectionTTL(connectionTTL);
            connectionPool.setCallTimeout(callTimeout);
            connectionPool.setConsumerWindowSize(consumerWindowSize);
            connectionPool.setConsumerMaxRate(consumerMaxRate);
            connectionPool.setProducerWindowSize(producerWindowSize);
            connectionPool.setProducerMaxRate(producerMaxRate);
            connectionPool.setRetryInterval(retryInterval);
            connectionPool.setRetryIntervalMultiplier(retryIntervalMultiplier);
            connectionPool.setMaxRetryInterval(maxRetryInterval);
            connectionPool.setReconnectAttempts(reconnectAttempts);
            connectionPool.setInitialConnectAttempts(initialConnectAttempts);
            connectionPool.setConfirmationWindowSize(1024);

            ClientSessionFactory factory = connectionPool.createSessionFactory();

            connection.addNativeData(ArtemisConstants.ARTEMIS_CONNECTION_POOL, connectionPool);
            connection.addNativeData(ArtemisConstants.ARTEMIS_SESSION_FACTORY, factory);

        } catch (Exception e) { //catching Exception as it is thrown by the createSessionFactory method
            ArtemisUtils.throwException("Error occurred while starting connection.", e, logger);
        }
    }

    private CreateConnection() {
    }
}
