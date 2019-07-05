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

package org.ballerinalang.messaging.rabbitmq.nativeimpl.message;

import com.rabbitmq.client.AMQP;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * Retrieves the basic properties of the message.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "getProperties",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.MESSAGE_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class GetProperties extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

    }

    public static Object getProperties(Strand strand, ObjectValue messageObjectValue) {
        AMQP.BasicProperties basicProperties =
                (AMQP.BasicProperties) messageObjectValue.getNativeData(RabbitMQConstants.BASIC_PROPERTIES);
        if (basicProperties == null) {
            return RabbitMQUtils.returnErrorValue
                    ("Failure in retrieving the basic properties for this message");
        }
        String replyTo = basicProperties.getReplyTo();
        String contentType = basicProperties.getContentType();
        String contentEncoding = basicProperties.getContentEncoding();
        String correlationId = basicProperties.getCorrelationId();
        MapValue<String, Object> properties = BallerinaValues.createRecordValue(RabbitMQConstants.PACKAGE_RABBITMQ,
                RabbitMQConstants.RECORD_BASIC_PROPERTIES);
        Object[] values = new Object[4];
        values[0] = replyTo;
        values[1] = contentType;
        values[2] = contentEncoding;
        values[3] = correlationId;
        return BallerinaValues.createRecord(properties, values);
    }
}
