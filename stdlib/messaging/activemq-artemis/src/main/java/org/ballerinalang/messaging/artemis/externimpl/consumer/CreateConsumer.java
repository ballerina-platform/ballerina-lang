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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
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
        orgName = ArtemisConstants.BALLERINA, packageName = ArtemisConstants.ARTEMIS,
        functionName = "createConsumer",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = ArtemisConstants.CONSUMER_OBJ,
                             structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS)
)
public class CreateConsumer extends BlockingNativeCallableUnit {
    private static final Logger logger = LoggerFactory.getLogger(CreateConsumer.class);

    @Override
    public void execute(Context context) {
        try {
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> consumerObj = (BMap<String, BValue>) context.getRefArgument(0);

            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> sessionObj = (BMap<String, BValue>) context.getRefArgument(1);
            ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> queueConfig = (BMap<String, BValue>) context.getRefArgument(2);
            String consumerFilter = getStringFromBValueOrNull(context.getNullableRefArgument(3));
            boolean autoAck = context.getBooleanArgument(0);

            String queueName = queueConfig.get(ArtemisConstants.QUEUE_NAME).stringValue();
            SimpleString addressName = new SimpleString(
                    getStringFromBValueOrNull(queueConfig.get(ArtemisConstants.ADDRESS_NAME), queueName));
            boolean autoCreated = getBooleanFromBValue(queueConfig.get(ArtemisConstants.AUTO_CREATED));
            String routingType = queueConfig.get(ArtemisConstants.ROUTING_TYPE).stringValue();
            boolean temporary = getBooleanFromBValue(queueConfig.get(ArtemisConstants.TEMPORARY));
            String queueFilter = getStringFromBValueOrNull(queueConfig.get(ArtemisConstants.FILTER));
            boolean durable = getBooleanFromBValue(queueConfig.get(ArtemisConstants.DURABLE));
            int maxConsumers = ArtemisUtils.getIntFromConfig(queueConfig, ArtemisConstants.MAX_CONSUMERS, logger);
            boolean purgeOnNoConsumers = getBooleanFromBValue(queueConfig.get(ArtemisConstants.PURGE_ON_NO_CONSUMERS));
            boolean exclusive = getBooleanFromBValue(queueConfig.get(ArtemisConstants.EXCLUSIVE));
            boolean lastValue = getBooleanFromBValue(queueConfig.get(ArtemisConstants.LAST_VALUE));

            ArtemisUtils.getClientConsumer(consumerObj, session, consumerFilter, queueName, addressName,
                                           autoCreated, routingType, temporary, queueFilter, durable, maxConsumers,
                                           purgeOnNoConsumers, exclusive, lastValue, logger);
            consumerObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT,
                                      sessionObj.getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT));
            consumerObj.addNativeData(ArtemisConstants.ARTEMIS_AUTO_ACK, autoAck);
            session.start();
        } catch (ActiveMQException e) {
            context.setReturnValues(ArtemisUtils.getError(context, e));
        }
    }

    private String getStringFromBValueOrNull(BValue value, String defaultVal) {
        return value != null ? value.stringValue() : defaultVal;
    }

    private String getStringFromBValueOrNull(BValue value) {
        return value != null ? value.stringValue() : null;
    }

    private boolean getBooleanFromBValue(BValue value) {
        return ((BBoolean) value).booleanValue();
    }
}
