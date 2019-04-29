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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
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
        orgName = ArtemisConstants.BALLERINA, packageName = ArtemisConstants.ARTEMIS,
        functionName = "createProducer",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = ArtemisConstants.PRODUCER_OBJ,
                             structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS),
        args = {
                @Argument(name = "addressName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.RECORD, structType = "AddressConfiguration"),
                @Argument(name = "rate", type = TypeKind.INT)
        }
)
public class CreateProducer implements NativeCallableUnit {

    private static final Logger logger = LoggerFactory.getLogger(CreateProducer.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        try {
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> producerObj = (BMap<String, BValue>) context.getRefArgument(0);

            SimpleString addressName = new SimpleString(context.getStringArgument(0));

            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> configObj = (BMap<String, BValue>) context.getRefArgument(1);

            String routingType = configObj.get(ArtemisConstants.ROUTING_TYPE).stringValue();
            boolean autoCreated = ((BBoolean) configObj.get(ArtemisConstants.AUTO_CREATED)).booleanValue();

            int rate = ArtemisUtils.getIntFromLong(context.getIntArgument(0), ArtemisConstants.RATE, logger);
            ClientSession session = ArtemisUtils.getClientSessionFromBMap(producerObj);

            if (autoCreated) {
                ClientSession.AddressQuery addressQuery = session.addressQuery(addressName);
                if (!addressQuery.isExists()) {
                    session.createAddress(addressName, ArtemisUtils.getRoutingTypeFromString(routingType), true);
                } else {
                    logger.warn("Address with the name {} already exists. ", addressName);
                }
            }
            ClientProducer producer = session.createProducer(addressName, rate);
            producerObj.addNativeData(ArtemisConstants.ARTEMIS_PRODUCER, producer);

        } catch (ActiveMQException ex) {
            ArtemisUtils.throwBallerinaException("Error occurred while creating the producer.", context, ex, logger);
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
