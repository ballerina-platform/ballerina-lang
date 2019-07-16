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

package org.ballerinalang.messaging.artemis.externimpl.producer;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
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
 * Extern function for creating an ActiveMQ Artemis producer.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "createProducer",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.PRODUCER_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        )
)
public class CreateProducer {

    private static final Logger logger = LoggerFactory.getLogger(CreateProducer.class);

    public static void createProducer(Strand strand, ObjectValue producerObj, String addressStr,
                                      MapValue<String, Object> configObj, long rateArg) {
        try {

            SimpleString addressName = new SimpleString(addressStr);

            String routingType = configObj.getStringValue(ArtemisConstants.ROUTING_TYPE);
            boolean autoCreated = configObj.getBooleanValue(ArtemisConstants.AUTO_CREATED);

            int rate = ArtemisUtils.getIntFromLong(rateArg, ArtemisConstants.RATE, logger);
            ObjectValue sessionObj = producerObj.getObjectValue(ArtemisConstants.SESSION);
            ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);

            if (autoCreated) {
                ClientSession.AddressQuery addressQuery = session.addressQuery(addressName);
                if (!addressQuery.isExists()) {
                    session.createAddress(addressName, ArtemisUtils.getRoutingTypeFromString(routingType), true);
                } else {
                    logger.warn("Address with the name {} already exists. ", addressName);
                }
            }
            ClientProducer producer = session.createProducer(addressName, rate);
            producerObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT,
                    sessionObj.getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT));
            producerObj.addNativeData(ArtemisConstants.ARTEMIS_PRODUCER, producer);

        } catch (ActiveMQException ex) {
            ArtemisUtils.throwException("Error occurred while creating the producer.", ex, logger);
        }
    }

    private CreateProducer() {
    }
}
