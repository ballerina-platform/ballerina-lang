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
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

/**
 * Extern function to create an async Artemis consumer.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "createConsumer",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.LISTENER_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        )
)
public class CreateServiceConsumer extends BlockingNativeCallableUnit {
    private static final Logger logger = LoggerFactory.getLogger(CreateServiceConsumer.class);
    private static final PrintStream console = System.out;

    @Override
    public void execute(Context context) {
    }

    public static Object createConsumer(Strand strand, ObjectValue listenerObj, ObjectValue service) {
        try {
            ObjectValue sessionObj = (ObjectValue) listenerObj.get(ArtemisConstants.SESSION);
            ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);

            MapValue serviceAnnotation = getServiceConfigAnnotation(service);
            boolean autoAck = serviceAnnotation.getBooleanValue(ArtemisConstants.AUTO_ACK);
            String consumerFilter = ArtemisUtils.getStringFromObjOrNull(serviceAnnotation.get(ArtemisConstants.FILTER));

            MapValue queueConfig = serviceAnnotation.getMapValue(ArtemisConstants.QUEUE_CONFIG);
            String queueName = queueConfig.getStringValue(ArtemisConstants.QUEUE_NAME);
            SimpleString addressName = new SimpleString(ArtemisUtils.getAddressName(queueConfig, queueName));
            boolean autoCreated = queueConfig.getBooleanValue(ArtemisConstants.AUTO_CREATED);
            String routingType = queueConfig.getStringValue(ArtemisConstants.ROUTING_TYPE);
            boolean temporary = queueConfig.getBooleanValue(ArtemisConstants.TEMPORARY);
            String queueFilter = ArtemisUtils.getStringFromObjOrNull(queueConfig.get(ArtemisConstants.FILTER));
            boolean durable = queueConfig.getBooleanValue(ArtemisConstants.DURABLE);
            int maxConsumers = ArtemisUtils.getIntFromLong(queueConfig.getIntValue(ArtemisConstants.MAX_CONSUMERS),
                                                           ArtemisConstants.MAX_CONSUMERS, logger);
            boolean purgeOnNoConsumers = queueConfig.getBooleanValue(ArtemisConstants.PURGE_ON_NO_CONSUMERS);
            boolean exclusive = queueConfig.getBooleanValue(ArtemisConstants.EXCLUSIVE);
            boolean lastValue = queueConfig.getBooleanValue(ArtemisConstants.LAST_VALUE);

            ClientConsumer consumer = ArtemisUtils.getClientConsumer(listenerObj, session, consumerFilter, queueName,
                                                                     addressName, autoCreated, routingType, temporary,
                                                                     queueFilter, durable, maxConsumers,
                                                                     purgeOnNoConsumers, exclusive, lastValue, logger);
            console.println("[" + ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS + "] Client Consumer created for queue " +
                                    queueName);
            //Todo: Validate onMessage resource is not null at compiler level
            consumer.setMessageHandler(new ArtemisMessageHandler(strand.scheduler, service, sessionObj, autoAck));
        } catch (ActiveMQException e) {
            return ArtemisUtils.getError(e);
        }
        return null;
    }


    private static MapValue getServiceConfigAnnotation(ObjectValue service) {
        ArrayValue annotation = service.getType().getAnnotation(ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS,
                                                                "ServiceConfig");
        return (MapValue) annotation.get(0);
    }
}
