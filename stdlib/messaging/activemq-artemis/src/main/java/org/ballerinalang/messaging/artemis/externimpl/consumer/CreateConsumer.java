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

package org.ballerinalang.messaging.artemis.externimpl.consumer;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientSession;
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
 * Extern function to create a sync Artemis consumer.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "createConsumer",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.CONSUMER_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        )
)
public class CreateConsumer {
    private static final Logger logger = LoggerFactory.getLogger(CreateConsumer.class);

    // Todo: using an object for union. Check if correct
    public static Object createConsumer(Strand strand, ObjectValue consumerObj, ObjectValue sessionObj,
                                        MapValue queueConfig,
                                        boolean autoAck, Object consumerFilter) {
        try {
            ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);

            String queueName = queueConfig.getStringValue(ArtemisConstants.QUEUE_NAME);
            SimpleString addressName = new SimpleString(ArtemisUtils.getAddressName(queueConfig, queueName));
            boolean autoCreated = queueConfig.getBooleanValue(ArtemisConstants.AUTO_CREATED);
            String routingType = queueConfig.getStringValue(ArtemisConstants.ROUTING_TYPE);
            boolean temporary = queueConfig.getBooleanValue(ArtemisConstants.TEMPORARY);
            String queueFilter = ArtemisUtils.getStringFromObjOrNull(queueConfig.get(ArtemisConstants.FILTER));
            boolean durable = queueConfig.getBooleanValue(ArtemisConstants.DURABLE);
            int maxConsumers = ArtemisUtils.getIntFromConfig(queueConfig, ArtemisConstants.MAX_CONSUMERS, logger);
            boolean purgeOnNoConsumers = queueConfig.getBooleanValue(ArtemisConstants.PURGE_ON_NO_CONSUMERS);
            boolean exclusive = queueConfig.getBooleanValue(ArtemisConstants.EXCLUSIVE);
            boolean lastValue = queueConfig.getBooleanValue(ArtemisConstants.LAST_VALUE);

            ArtemisUtils.getClientConsumer(consumerObj, session, ArtemisUtils.getStringFromObjOrNull(consumerFilter),
                    queueName, addressName, autoCreated, routingType, temporary, queueFilter,
                    durable, maxConsumers, purgeOnNoConsumers, exclusive, lastValue, logger);
            consumerObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT,
                    sessionObj.getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT));
            consumerObj.addNativeData(ArtemisConstants.ARTEMIS_AUTO_ACK, autoAck);
            session.start();
        } catch (ActiveMQException e) {
            return ArtemisUtils.getError(e);
        }
        return null;
    }

    private CreateConsumer() {
    }
}
