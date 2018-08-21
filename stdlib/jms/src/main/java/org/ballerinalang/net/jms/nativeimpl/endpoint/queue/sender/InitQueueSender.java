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

package org.ballerinalang.net.jms.nativeimpl.endpoint.queue.sender;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.nativeimpl.endpoint.common.SessionConnector;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jms",
        functionName = "initQueueSender",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "QueueSender", structPackage = "ballerina/jms"),
        args = {
                @Argument(name = "session", type = TypeKind.OBJECT, structType = "Session")
        }
)
public class InitQueueSender implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Struct queueSenderBObject = BallerinaAdapter.getReceiverObject(context);
        Struct queueSenderConfig = queueSenderBObject.getStructField(Constants.QUEUE_SENDER_FIELD_CONFIG);
        Struct destinationConfig = queueSenderConfig.getStructField(Constants.ALIAS_DESTINATION);
        String destinationName = destinationConfig.getStringField(Constants.DESTINATION_NAME);
        BMap<String, BValue> sessionBObject = (BMap<String, BValue>) context.getRefArgument(1);

        Session session = BallerinaAdapter.getNativeObject(sessionBObject,
                                                           Constants.JMS_SESSION,
                                                           Session.class,
                                                           context);

        Destination destination = null;
        if (destinationConfig.getNativeData(Constants.JMS_DESTINATION_OBJECT) != null) {
            destination = BallerinaAdapter.getNativeObject(destinationConfig,
                                                            Constants.JMS_DESTINATION_OBJECT,
                                                            Destination.class,
                                                            context);
        }

        try {
            if (destination == null) {
                destination = session.createQueue(destinationName);
            }
            MessageProducer producer = session.createProducer(destination);
            Struct queueSenderConnectorBObject
                    = queueSenderBObject.getStructField(Constants.QUEUE_SENDER_FIELD_PRODUCER_ACTIONS);
            queueSenderConnectorBObject.addNativeData(Constants.JMS_PRODUCER_OBJECT, producer);
            queueSenderConnectorBObject.addNativeData(Constants.SESSION_CONNECTOR_OBJECT,
                                                      new SessionConnector(session));
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Error creating queue sender.", context, e);
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
