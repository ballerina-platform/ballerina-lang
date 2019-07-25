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

import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

/**
 * Returns the payload of the message.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "getPayload",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class GetPayload {
    private static final Logger log = LoggerFactory.getLogger(GetPayload.class);

    private static final String INVALID_XML_DATA = "Invalid XML data";
    private static final String INVALID_DATA_TYPE = "Invalid data type";

    public static Object getPayload(Strand strand, ObjectValue msgObj, Object type, Object bytesLength) {
        String msgType = msgObj.getStringValue(JmsConstants.MESSAGE_TYPE_FIELD_NAME);
        Object message = msgObj.getNativeData(JmsConstants.JMS_MESSAGE_OBJECT);
        try {
            switch (msgType) {
                case JmsConstants.MESSAGE:
                    return null;
                case JmsConstants.TEXT_MESSAGE:
                    if (type == null) {
                        return ((TextMessage) message).getText();
                    }
                    return castMessageToType((TextMessage) message, (String) type);
                case JmsConstants.BYTES_MESSAGE:
                    if (type == null) {
                        return BallerinaAdapter.getError("DataType has to be specified for " + msgType);
                    }
                    return readByteMessageContent((BytesMessage) message, (String) type, bytesLength);
                case JmsConstants.STREAM_MESSAGE:
                    return readStreamMessageContent((StreamMessage) message, (String) type, bytesLength);
                case JmsConstants.MAP_MESSAGE:
                    return readMapMessageContent((MapMessage) message);
                default:
                    return BallerinaAdapter.getError("Invalid message type");
            }
        } catch (JMSException e) {
            return BallerinaAdapter.getError("Error while getting the payload of " + msgType, e);
        }
    }

    private static Object castMessageToType(TextMessage message, String dataType) throws JMSException {
        String msgContent = message.getText();

        switch (dataType) {
            case JmsConstants.INT:
                return Long.valueOf(msgContent);
            case JmsConstants.FLOAT:
                return Float.valueOf(msgContent);
            case JmsConstants.BYTE:
                return Byte.valueOf(msgContent);
            case JmsConstants.BOOLEAN:
                return Boolean.valueOf(msgContent);
            case JmsConstants.STRING:
                return msgContent;
            case JmsConstants.JSON:
                return JSONParser.parse(msgContent);
            case JmsConstants.XML:
                XMLValue bxml = XMLFactory.parse(msgContent);
                if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                    return BallerinaAdapter.getError(INVALID_XML_DATA);
                }
                return bxml;
            case JmsConstants.BYTES:
                return msgContent.getBytes(StandardCharsets.UTF_8);
            default:
                return BallerinaAdapter.getError(INVALID_DATA_TYPE);

        }
    }

    private static Object readMapMessageContent(MapMessage message) throws JMSException {
        @SuppressWarnings(JmsConstants.UNCHECKED)
        Enumeration<String> names = message.getMapNames();
        MapValue<String, Object> mapObj = new MapValueImpl<>(new BMapType(new BUnionType(
                Arrays.asList(BTypes.typeString, BTypes.typeByte, BTypes.typeInt, BTypes.typeFloat, BTypes.typeBoolean,
                              BTypes.fromString("byte[]"), BTypes.typeNull))));
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            Object val = message.getObject(key);
            if (val instanceof byte[]) {
                val = new ArrayValue((byte[]) val);
            }
            mapObj.put(key, val);
        }
        return mapObj;
    }

    private static Object readByteMessageContent(BytesMessage message, String dataType, Object bytesLength)
            throws JMSException {
        switch (dataType) {
            case JmsConstants.INT:
                return message.readLong();
            case JmsConstants.FLOAT:
                return message.readDouble();
            case JmsConstants.BYTE:
                return message.readInt();
            case JmsConstants.BOOLEAN:
                return message.readBoolean();
            case JmsConstants.STRING:
                return message.readUTF();
            case JmsConstants.JSON:
                return JSONParser.parse(message.readUTF());
            case JmsConstants.XML:
                XMLValue bxml = XMLFactory.parse(message.readUTF());
                if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                    return BallerinaAdapter.getError(INVALID_XML_DATA);
                }
                return bxml;
            case JmsConstants.BYTES:
                if (bytesLength == null) {
                    return BallerinaAdapter.getError("The byte array length need to be specified for the " + dataType);
                }
                byte[] bytes = new byte[BallerinaAdapter.getIntFromLong((Long) bytesLength, "bytesLength", log)];
                message.readBytes(bytes);
                return new ArrayValue(bytes);
            default:
                return BallerinaAdapter.getError(INVALID_DATA_TYPE);

        }
    }

    private static Object readStreamMessageContent(StreamMessage message, String dataType, Object bytesLength)
            throws JMSException {
        switch (dataType) {
            case JmsConstants.INT:
                return message.readLong();
            case JmsConstants.FLOAT:
                return message.readDouble();
            case JmsConstants.BYTE:
                return message.readInt();
            case JmsConstants.BOOLEAN:
                return message.readBoolean();
            case JmsConstants.STRING:
                return message.readString();
            case JmsConstants.JSON:
                return JSONParser.parse(message.readString());
            case JmsConstants.XML:
                XMLValue bxml = XMLFactory.parse(message.readString());
                if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                    return BallerinaAdapter.getError(INVALID_XML_DATA);
                }
                return bxml;
            case JmsConstants.BYTES:
                if (bytesLength == null) {
                    return BallerinaAdapter.getError("The byte array length need to be specified for the " + dataType);
                }
                byte[] bytes = new byte[BallerinaAdapter.getIntFromLong((Long) bytesLength, "bytesLength", log)];
                message.readBytes(bytes);
                return new ArrayValue(bytes);
            default:
                return BallerinaAdapter.getError(INVALID_DATA_TYPE);

        }
    }

    private GetPayload() {
    }
}
