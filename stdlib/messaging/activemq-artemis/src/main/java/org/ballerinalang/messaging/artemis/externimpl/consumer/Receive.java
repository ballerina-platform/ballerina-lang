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
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisTransactionContext;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * Extern function for the blocking receive operation.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "receive",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.CONSUMER_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        ),
        isPublic = true
)
public class Receive {

    public static Object receive(Strand strand, ObjectValue consumerObj, long timeoutInMilliSeconds) {
        ClientConsumer consumer = (ClientConsumer) consumerObj.getNativeData(ArtemisConstants.ARTEMIS_CONSUMER);
        ArtemisTransactionContext transactionContext =
                (ArtemisTransactionContext) consumerObj.getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT);
        boolean autoAck = (boolean) consumerObj.getNativeData(ArtemisConstants.ARTEMIS_AUTO_ACK);
        try {
            ClientMessage clientMessage = consumer.receive(timeoutInMilliSeconds);
            ObjectValue messageObj = BallerinaValues.createObjectValue(ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS,
                    ArtemisConstants.MESSAGE_OBJ);
            ArtemisUtils.populateMessageObj(clientMessage, transactionContext, messageObj);
            if (autoAck) {
                clientMessage.acknowledge();
                if (transactionContext != null) {
                    transactionContext.handleTransactionBlock(ArtemisConstants.CONSUMER_OBJ);
                }
            }
            return messageObj;
        } catch (ActiveMQException e) {
            return ArtemisUtils.getError(e);
        }
    }

    private Receive() {
    }
}
