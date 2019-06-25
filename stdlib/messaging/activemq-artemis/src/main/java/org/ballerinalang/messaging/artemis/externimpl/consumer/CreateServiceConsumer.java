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
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

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
        try {
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> listenerObj = (BMap<String, BValue>) context.getRefArgument(0);
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> sessionObj = (BMap<String, BValue>) listenerObj.get(ArtemisConstants.SESSION);
            ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);

            Service service = BLangConnectorSPIUtil.getServiceRegistered(context);

            Annotation serviceAnnotation = getServiceConfigAnnotation(service);
            Struct annotationValue = serviceAnnotation.getValue();
            boolean autoAck = annotationValue.getBooleanField(ArtemisConstants.AUTO_ACK);
            String consumerFilter = getStringFromValueOrNull(annotationValue.getRefField(ArtemisConstants.FILTER));

            Map<String, Value> queueConfig = annotationValue.getMapField(ArtemisConstants.QUEUE_CONFIG);
            String queueName = queueConfig.get(ArtemisConstants.QUEUE_NAME).getStringValue();
            SimpleString addressName = new SimpleString(getAddressName(queueConfig, queueName));
            boolean autoCreated = queueConfig.get(ArtemisConstants.AUTO_CREATED).getBooleanValue();
            String routingType = queueConfig.get(ArtemisConstants.ROUTING_TYPE).getStringValue();
            boolean temporary = queueConfig.get(ArtemisConstants.TEMPORARY).getBooleanValue();
            String queueFilter = getStringFromValueOrNull(queueConfig.get(ArtemisConstants.FILTER));
            boolean durable = queueConfig.get(ArtemisConstants.DURABLE).getBooleanValue();
            int maxConsumers = ArtemisUtils.getIntFromLong(queueConfig.
                    get(ArtemisConstants.MAX_CONSUMERS).getIntValue(), ArtemisConstants.MAX_CONSUMERS, logger);
            boolean purgeOnNoConsumers = queueConfig.get(ArtemisConstants.PURGE_ON_NO_CONSUMERS).getBooleanValue();
            boolean exclusive = queueConfig.get(ArtemisConstants.EXCLUSIVE).getBooleanValue();
            boolean lastValue = queueConfig.get(ArtemisConstants.LAST_VALUE).getBooleanValue();

            ClientConsumer consumer = ArtemisUtils.getClientConsumer(listenerObj, session, consumerFilter, queueName,
                                                                     addressName, autoCreated, routingType, temporary,
                                                                     queueFilter, durable, maxConsumers,
                                                                     purgeOnNoConsumers, exclusive, lastValue, logger);
            console.println("[" + ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS + "] Client Consumer created for queue " +
                                    queueName);

            Resource onMessageResource = service.getResources()[0];
            if (onMessageResource != null) {
                consumer.setMessageHandler(new ArtemisMessageHandler(onMessageResource, sessionObj, autoAck));
            }
        } catch (ActiveMQException e) {
            context.setReturnValues(ArtemisUtils.getError(context, e));
        }
    }

    private String getAddressName(Map<String, Value> queueConfig, String queueName) {
        Value addressName = queueConfig.get(ArtemisConstants.ADDRESS_NAME);
        return addressName != null ? addressName.getStringValue() : queueName;
    }

    private String getStringFromValueOrNull(Value value) {
        return value != null ? value.getStringValue() : null;
    }

    private Annotation getServiceConfigAnnotation(Service service) {
        List<Annotation> annotationList = service
                .getAnnotationList(ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS,
                                   "ServiceConfig");

        if (annotationList == null) {
            return null;
        }
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
}
