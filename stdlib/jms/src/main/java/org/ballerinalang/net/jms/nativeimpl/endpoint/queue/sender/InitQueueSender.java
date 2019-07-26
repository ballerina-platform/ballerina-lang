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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.nativeimpl.endpoint.common.SessionConnector;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Initialize the queue sender.
 *
 * @since 0.966
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX,
        packageName = JmsConstants.JAVA_JMS,
        functionName = "initQueueSender",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.QUEUE_SENDER_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS),
        args = {
                @Argument(name = "session", type = TypeKind.OBJECT, structType = JmsConstants.SESSION_OBJ_NAME),
                @Argument(name = "destination", type = TypeKind.OBJECT)
        }
)
public class InitQueueSender {

    public static void initQueueSender(Strand strand, ObjectValue queueSenderObj, ObjectValue sessionObj, Object dest) {
        Session session = (Session) sessionObj.getNativeData(JmsConstants.JMS_SESSION);
        String queueName = null;
        ObjectValue destinationBObject = null;
        if (dest instanceof String) {
            queueName = (String) dest;
        } else {
            destinationBObject = (ObjectValue) dest;
        }
        Destination destinationObject = JmsUtils.getDestination(destinationBObject);

        if (JmsUtils.isNullOrEmptyAfterTrim(queueName) && destinationObject == null) {
            throw new BallerinaException("Queue name and destination cannot be null at the same time");
        }
        try {
            Destination queue = destinationObject != null ? destinationObject : session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);
            queueSenderObj.addNativeData(JmsConstants.JMS_PRODUCER_OBJECT, producer);
            queueSenderObj.addNativeData(JmsConstants.SESSION_CONNECTOR_OBJECT,
                                         new SessionConnector(session));
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Error creating queue sender.", e);
        }
    }

    private InitQueueSender() {
    }
}
