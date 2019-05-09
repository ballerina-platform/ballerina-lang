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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisTransactionContext;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
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
        args = {
                @Argument(
                        name = "timeoutInMilliSeconds",
                        type = TypeKind.INT
                )
        },
        isPublic = true
)
public class Receive extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> consumerObj = (BMap<String, BValue>) context.getRefArgument(0);
        long timeInMilliSeconds = context.getIntArgument(0);
        ClientConsumer consumer = (ClientConsumer) consumerObj.getNativeData(ArtemisConstants.ARTEMIS_CONSUMER);
        ArtemisTransactionContext transactionContext =
                (ArtemisTransactionContext) consumerObj.getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT);
        boolean autoAck = (boolean) consumerObj.getNativeData(ArtemisConstants.ARTEMIS_AUTO_ACK);
        try {
            ClientMessage clientMessage = consumer.receive(timeInMilliSeconds);
            BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                    context, ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS, ArtemisConstants.MESSAGE_OBJ);
            messageObj.addNativeData(ArtemisConstants.ARTEMIS_MESSAGE, clientMessage);
            if (autoAck) {
                clientMessage.acknowledge();
                if (transactionContext != null) {
                    transactionContext.handleTransactionBlock(context, ArtemisConstants.CONSUMER_OBJ);
                }
            }
            context.setReturnValues(messageObj);
        } catch (ActiveMQException e) {
            context.setReturnValues(ArtemisUtils.getError(context, e));
        }
    }
}
