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
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisTransactionContext;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * Extern function of the producer to send a message.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "externSend",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.PRODUCER_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        ),
        args = {
                @Argument(
                        name = "data",
                        type = TypeKind.OBJECT,
                        structType = ArtemisConstants.MESSAGE_OBJ
                )
        }
)
public class Send {

    public static Object externSend(Strand strand, ObjectValue producerObj, ObjectValue data) {
        try {
            ClientProducer producer = (ClientProducer) producerObj.getNativeData(ArtemisConstants.ARTEMIS_PRODUCER);
            ClientMessage message = (ClientMessage) data.getNativeData(ArtemisConstants.ARTEMIS_MESSAGE);
            ArtemisTransactionContext transactionContext = (ArtemisTransactionContext) ((ObjectValue) producerObj.get(
                    ArtemisConstants.SESSION)).getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT);
            // Having to use the blocking function because of an issue in Artemis:
            // https://issues.apache.org/jira/browse/ARTEMIS-2325
            producer.send(message);
            if (transactionContext != null) {
                transactionContext.handleTransactionBlock(ArtemisConstants.PRODUCER_OBJ, strand);
            }
        } catch (ActiveMQException e) {
            return ArtemisUtils.getError(e);
        }
        return null;
    }

    private Send() {
    }
}
