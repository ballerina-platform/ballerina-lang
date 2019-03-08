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

import com.rabbitmq.client.Channel;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.util.ChannelUtils;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.CHANNEL_NATIVE_OBJECT;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.ORG_NAME;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.PACKAGE_RABBITMQ;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.STRUCT_PACKAGE_RABBITMQ;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.CHANNEL_STRUCT;

/**
 *  Declare a queue.
 */
@BallerinaFunction(
        orgName = ORG_NAME, packageName = PACKAGE_RABBITMQ,
        functionName = "queueDeclare",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CHANNEL_STRUCT, structPackage = STRUCT_PACKAGE_RABBITMQ),
        args = {@Argument(name = "queueConfig", type = TypeKind.RECORD, structType = "QueueConfiguration")
        },
        isPublic = true
)
public class QueueDeclare implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> channelObject = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> queueConfig = (BMap<String, BValue>) context.getRefArgument(1);
        Channel channel = RabbitMQUtils.getNativeObject(channelObject, CHANNEL_NATIVE_OBJECT, Channel.class, context);
        ChannelUtils.queueDeclare(channel, queueConfig);
        System.out.println("Queue Declared");
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
