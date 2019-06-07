/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.nativeimpl.channel;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQTransactionContext;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * Retrieves the RabbitMQ Connection object.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "getConnection",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.CHANNEL_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class GetConnection extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> channelBObject = (BMap<String, BValue>) context.getRefArgument(0);
        Channel channel = RabbitMQUtils.getNativeObject(channelBObject, RabbitMQConstants.CHANNEL_NATIVE_OBJECT,
                Channel.class, context);
        RabbitMQTransactionContext transactionContext = (RabbitMQTransactionContext) channelBObject.
                getNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT);
        try {
            Connection connection = channel.getConnection();
            ProgramFile programFile = context.getProgramFile();
            BMap<String, BValue> connectionObject = BLangConnectorSPIUtil.createBStruct(
                    programFile, RabbitMQConstants.PACKAGE_RABBITMQ, RabbitMQConstants.CONNECTION_OBJECT);
            connectionObject.addNativeData(RabbitMQConstants.CONNECTION_NATIVE_OBJECT, connection);
            context.setReturnValues(connectionObject);
            if (transactionContext != null) {
                transactionContext.handleTransactionBlock(context);
            }
        } catch (AlreadyClosedException exception) {
            RabbitMQUtils.returnError(RabbitMQConstants.CHANNEL_CLOSED_ERROR, context, exception);
        }
    }
}
