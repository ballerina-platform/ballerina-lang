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

package org.ballerinalang.messaging.rabbitmq.nativeimpl.listener;

import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.IOException;

/**
 * Request specific "quality of service" settings.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "setQosSettings",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.LISTENER_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ)
)
public class SetQosSettings {

    public static Object setQosSettings(Strand strand, ObjectValue listenerObjectValue, Object prefetchCount,
                                        Object prefetchSize) {
        ObjectValue channelObject =
                (ObjectValue) listenerObjectValue.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) channelObject.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        boolean isValidCount = prefetchCount != null &&
                RabbitMQUtils.checkIfInt(prefetchCount);
        try {
            if (isValidCount) {
                boolean isValidSize = prefetchSize != null && RabbitMQUtils.checkIfInt(prefetchSize);
                if (isValidSize) {
                    channel.basicQos(Math.toIntExact(((Number) prefetchSize).longValue()),
                            Math.toIntExact(((Number) prefetchCount).longValue()),
                            true);
                    channelObject.addNativeData(RabbitMQConstants.QOS_STATUS, true);
                } else {
                    channel.basicQos(Math.toIntExact(((BInteger) prefetchCount).intValue()),
                            true);
                    channelObject.addNativeData(RabbitMQConstants.QOS_STATUS, true);
                }
            }
        } catch (IOException exception) {
            return BallerinaErrors.createError("An I/O error occurred while setting the global " +
                    "quality of service settings for the listener");
        }
        return null;
    }

    private SetQosSettings() {
    }
}
