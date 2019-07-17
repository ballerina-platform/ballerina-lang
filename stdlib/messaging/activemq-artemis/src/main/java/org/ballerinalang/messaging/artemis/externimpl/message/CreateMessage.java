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

package org.ballerinalang.messaging.artemis.externimpl.message;

import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.reader.BytesMessageUtil;
import org.apache.activemq.artemis.reader.MapMessageUtil;
import org.apache.activemq.artemis.reader.TextMessageUtil;
import org.apache.activemq.artemis.utils.collections.TypedProperties;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function to create an ActiveMQ Artemis message.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "createMessage",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.MESSAGE_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        )
)
public class CreateMessage {
    private static final Logger logger = LoggerFactory.getLogger(CreateMessage.class);

    public static void createMessage(Strand strand, ObjectValue messageObj, ObjectValue sessionObj, Object dataVal,
                                     MapValue configObj) {
        String type = messageObj.getStringValue(ArtemisConstants.MESSAGE_TYPE);
        long expiration = configObj.getIntValue(ArtemisConstants.EXPIRATION);
        long timeStamp = configObj.getIntValue(ArtemisConstants.TIME_STAMP);
        byte priority = Byte.valueOf(configObj.get(ArtemisConstants.PRIORITY).toString());
        boolean durable = configObj.getBooleanValue(ArtemisConstants.DURABLE);
        Object routingType = configObj.get(ArtemisConstants.ROUTING_TYPE);
        Object groupId = configObj.get(ArtemisConstants.GROUP_ID);
        int groupSequence = ArtemisUtils.getIntFromConfig(configObj, ArtemisConstants.GROUP_SEQUENCE, logger);
        Object correlationId = configObj.get(ArtemisConstants.CORRELATION_ID);
        Object replyTo = configObj.get(ArtemisConstants.REPLY_TO);

        ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);

        byte messageType = getMessageType(type);
        ClientMessage message = session.createMessage(messageType, durable, expiration, timeStamp, priority);
        if (routingType instanceof String) {
            message.setRoutingType(ArtemisUtils.getRoutingTypeFromString((String) routingType));
        }
        if (groupId instanceof String) {
            message.setGroupID((String) groupId);
        }
        message.setGroupSequence(groupSequence);
        if (correlationId instanceof String) {
            message.setCorrelationID(correlationId);
        }
        if (replyTo instanceof String) {
            message.setReplyTo(new SimpleString((String) replyTo));
        }
        if (messageType == Message.TEXT_TYPE) {
            TextMessageUtil.writeBodyText(message.getBodyBuffer(), new SimpleString((String) dataVal));
        } else if (messageType == Message.BYTES_TYPE) {
            BytesMessageUtil.bytesWriteBytes(message.getBodyBuffer(), ArtemisUtils.getBytesData((ArrayValue) dataVal));
        } else if (messageType == Message.MAP_TYPE) {
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            String[] keys = ((MapValue<String, Object>) dataVal).getKeys();
            TypedProperties map = new TypedProperties();
            for (String keyStr : keys) {
                SimpleString key = new SimpleString(keyStr);
                @SuppressWarnings(ArtemisConstants.UNCHECKED)
                Object value = ((MapValue<String, Object>) dataVal).get(keyStr);
                if (value instanceof String) {
                    map.putSimpleStringProperty(key, new SimpleString((String) value));
                } else if (value instanceof Long) {
                    map.putLongProperty(key, (long) value);
                } else if (value instanceof Double) {
                    map.putDoubleProperty(key, (Double) value);
                } else if (value instanceof Integer) {
                    map.putIntProperty(key, (Integer) value);
                } else if (value instanceof Byte) {
                    map.putByteProperty(key, (Byte) value);
                } else if (value instanceof Boolean) {
                    map.putBooleanProperty(key, (Boolean) value);
                } else if (value instanceof ArrayValue) {
                    map.putBytesProperty(key, ArtemisUtils.getBytesData((ArrayValue) value));
                }
                MapMessageUtil.writeBodyMap(message.getBodyBuffer(), map);
            }
        } else if (messageType == Message.STREAM_TYPE) {
            ObjectValue streamObj = (ObjectValue) dataVal;
            Channel channel = (Channel) streamObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            message.setBodyInputStream(channel.getInputStream());
        }
        messageObj.addNativeData(ArtemisConstants.ARTEMIS_MESSAGE, message);
    }

    private static byte getMessageType(String type) {
        switch (type) {
            case "TEXT":
                return Message.TEXT_TYPE;
            case "BYTES":
                return Message.BYTES_TYPE;
            case "MAP":
                return Message.MAP_TYPE;
            case "STREAM":
                return Message.STREAM_TYPE;
            default:
                return Message.DEFAULT_TYPE;
        }
    }

    private CreateMessage() {
    }
}
