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

package org.ballerinalang.messaging.rabbitmq.nativeimpl.channel.listener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Closing the channel listener.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "stop",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.CHANNEL_LISTENER_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ)
)
public class Stop extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> channelListObject = (BMap<String, BValue>) context.getRefArgument(0);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> channelOb =
                (BMap<String, BValue>) channelListObject.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) channelOb.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        if (channel == null) {
            throw new BallerinaException("ChannelListener not properly initialised");
        } else {
            try {
                Connection connection = channel.getConnection();
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException exception) {
                RabbitMQUtils.returnError(RabbitMQConstants.CLOSE_CHANNEL_ERROR
                        + " " + exception.getMessage(), context, exception);
            }
        }
    }
}
