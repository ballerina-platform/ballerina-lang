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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
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
                             structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS),
        args = {
                @Argument(name = "url", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.RECORD, structType = "ConnectionConfiguration")
        }
)
public class CreateConnection extends BlockingNativeCallableUnit {
    private static final Logger logger = LoggerFactory.getLogger(CreateConnection.class);

    @Override
    public void execute(Context context) {
        try {
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);

            String url = context.getStringArgument(0);

            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> configObj = (BMap<String, BValue>) context.getRefArgument(1);
            long connectionTTL = ((BInteger) configObj.get(ArtemisConstants.TIME_TO_LIVE)).intValue();
            long callTimeout = ((BInteger) configObj.get(ArtemisConstants.CALL_TIMEOUT)).intValue();
            int consumerWindowSize = ArtemisUtils.getIntFromConfig(
                    configObj, ArtemisConstants.CONSUMER_WINDOW_SIZE, logger);
            int consumerMaxRate = ArtemisUtils.getIntFromConfig(configObj, ArtemisConstants.CONSUMER_MAX_RATE, logger);
            int producerWindowSize = ArtemisUtils.getIntFromConfig(
                    configObj, ArtemisConstants.PRODUCER_WINDOW_SIZE, logger);
            int producerMaxRate = ArtemisUtils.getIntFromConfig(configObj, ArtemisConstants.PRODUCER_MAX_RATE, logger);
            long retryInterval = ((BInteger) configObj.get(ArtemisConstants.RETRY_INTERVAL)).intValue();
            double retryIntervalMultiplier = ((BFloat) configObj.get(
                    ArtemisConstants.RETRY_INTERVAL_MULTIPLIER)).floatValue();
            long maxRetryInterval = ((BInteger) configObj.get(ArtemisConstants.MAX_RETRY_INTERVAL)).intValue();
            int reconnectAttempts = ArtemisUtils.getIntFromConfig(
                    configObj, ArtemisConstants.RECONNECT_ATTEMPTS, logger);
            int initialConnectAttempts = ArtemisUtils.getIntFromConfig(
                    configObj, ArtemisConstants.INITIAL_CONNECT_ATTEMPTS, logger);

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
            ArtemisUtils.logAndSetError("Error occurred while starting connection.", context, e, logger);
        }
    }
}
