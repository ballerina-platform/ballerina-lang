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

package org.ballerinalang.net.jms.nativeimpl.message;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

/**
 * Sets the payload/body of the message.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "setPayload",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class SetPayload {

    public static Object setPayload(Strand strand, ObjectValue msgObj, Object content) {
        String msgType = msgObj.getStringValue(JmsConstants.MESSAGE_TYPE_FIELD_NAME);
        Object message = msgObj.getNativeData(JmsConstants.JMS_MESSAGE_OBJECT);
        try {
            switch (msgType) {
                case JmsConstants.MESSAGE:
                    return BallerinaAdapter.getError("Payload cannot be set for " + msgType);
                case JmsConstants.TEXT_MESSAGE:
                    //Todo: test for all content types
                    ((TextMessage) message).setText(content.toString());
                    break;
                case JmsConstants.BYTES_MESSAGE:
                    writeBytesMessageContent((BytesMessage) message, content);
                    break;
                case JmsConstants.STREAM_MESSAGE:
                    writeStreamMessageContent((StreamMessage) message, content);
                    break;
                case JmsConstants.MAP_MESSAGE:
                    writeMapMessageContent((MapMessage) message, content);
                    break;
                default:
                    return BallerinaAdapter.getError("Invalid message type");
            }
        } catch (JMSException e) {
            return BallerinaAdapter.getError("Error while setting the payload of " + msgType, e);
        }
        return null;
    }

    private static void writeBytesMessageContent(BytesMessage message, Object content) throws JMSException {
        if (content instanceof Long) {
            message.writeLong((Long) content);
        } else if (content instanceof Double) {
            message.writeDouble((Double) content);
        } else if (content instanceof Byte) {
            message.writeByte((Byte) content);
        } else if (content instanceof Integer) {
            message.writeInt((Integer) content);
        } else if (content instanceof Boolean) {
            message.writeBoolean((Boolean) content);
        }  else if (content instanceof ArrayValue) {
            message.writeBytes(JmsUtils.getBytesData((ArrayValue) content));
        } else {
            message.writeUTF(content.toString());
        }
    }

    private static void writeStreamMessageContent(StreamMessage message, Object content) throws JMSException {
        if (content instanceof Long) {
            message.writeLong((Long) content);
        } else if (content instanceof Double) {
            message.writeDouble((Double) content);
        } else if (content instanceof Byte) {
            message.writeByte((Byte) content);
        } else if (content instanceof Integer) {
            message.writeInt((Integer) content);
        } else if (content instanceof Boolean) {
            message.writeBoolean((Boolean) content);
        } else if (content instanceof ArrayValue) {
            message.writeBytes(JmsUtils.getBytesData((ArrayValue) content));
        } else {
            message.writeString(content.toString());
        }
    }

    private static void writeMapMessageContent(MapMessage message, Object content) throws JMSException {
        if (content instanceof MapValue) {
            @SuppressWarnings(JmsConstants.UNCHECKED)
            String[] keys = ((MapValue<String, Object>) content).getKeys();
            for (String keyStr : keys) {
                @SuppressWarnings(JmsConstants.UNCHECKED)
                Object value = ((MapValue<String, Object>) content).get(keyStr);
                if (value instanceof Long) {
                    message.setLong(keyStr, (Long) value);
                } else if (value instanceof Double) {
                    message.setDouble(keyStr, (Double) value);
                } else if (value instanceof Byte) {
                    message.setByte(keyStr, (Byte) value);
                } else if (value instanceof Integer) {
                    message.setInt(keyStr, (Integer) value);
                } else if (value instanceof Boolean) {
                    message.setBoolean(keyStr, (Boolean) value);
                } else if (value instanceof ArrayValue) {
                    message.setBytes(keyStr, JmsUtils.getBytesData((ArrayValue) value));
                } else if (value == null) {
                    message.setObject(keyStr, null);
                } else {
                    message.setString(keyStr, value.toString());
                }
            }
        }
    }

    private SetPayload() {
    }
}
