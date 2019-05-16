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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;

import java.util.Map;

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
        ),
        args = {
                @Argument(
                        name = "session",
                        type = TypeKind.OBJECT,
                        structType = ArtemisConstants.SESSION_OBJ
                ),
                @Argument(
                        name = "data",
                        type = TypeKind.UNION
                ),
                @Argument(
                        name = "config",
                        type = TypeKind.RECORD,
                        structType = "ConnectionConfiguration"
                )
        }
)
public class CreateMessage extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> messageObj = (BMap<String, BValue>) context.getRefArgument(0);
        String type = messageObj.get(ArtemisConstants.MESSAGE_TYPE).stringValue();

        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> sessionObj = (BMap<String, BValue>) context.getRefArgument(1);
        BValue dataVal = context.getRefArgument(2);

        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> configObj = (BMap<String, BValue>) context.getRefArgument(3);
        long expiration = getIntFromIntOrNil(configObj.get(ArtemisConstants.EXPIRATION), 0);
        long timeStamp = getIntFromIntOrNil(configObj.get(ArtemisConstants.TIME_STAMP), System.currentTimeMillis());
        byte priority = (byte) ((BByte) configObj.get(ArtemisConstants.PRIORITY)).byteValue();
        boolean durable = ((BBoolean) configObj.get(ArtemisConstants.DURABLE)).booleanValue();
        BValue routingType = configObj.get(ArtemisConstants.ROUTING_TYPE);

        ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);

        byte messageType = getMessageType(type);
        ClientMessage message = session.createMessage(messageType, durable, expiration, timeStamp, priority);
        if (routingType instanceof BString) {
            message.setRoutingType(ArtemisUtils.getRoutingTypeFromString(routingType.stringValue()));
        }

        if (messageType == Message.TEXT_TYPE) {
            TextMessageUtil.writeBodyText(message.getBodyBuffer(), new SimpleString(dataVal.stringValue()));
        } else if (messageType == Message.BYTES_TYPE) {
            BytesMessageUtil.bytesWriteBytes(message.getBodyBuffer(), ArtemisUtils.getBytesData((BValueArray) dataVal));
        } else if (messageType == Message.MAP_TYPE) {
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            Map<String, BValue> mapObj = ((BMap<String, BValue>) dataVal).getMap();
            TypedProperties map = new TypedProperties();
            for (Map.Entry<String, BValue> entry : mapObj.entrySet()) {
                SimpleString key = new SimpleString(entry.getKey());
                BValue value = entry.getValue();
                if (value instanceof BString) {
                    map.putSimpleStringProperty(key, new SimpleString(value.stringValue()));
                } else if (value instanceof BInteger) {
                    map.putLongProperty(key, ((BInteger) value).intValue());
                } else if (value instanceof BFloat) {
                    map.putDoubleProperty(key, ((BFloat) value).floatValue());
                } else if (value instanceof BByte) {
                    map.putByteProperty(key, (byte) ((BByte) value).byteValue());
                } else if (value instanceof BBoolean) {
                    map.putBooleanProperty(key, ((BBoolean) value).booleanValue());
                } else if (value instanceof BValueArray) {
                    map.putBytesProperty(key, ArtemisUtils.getBytesData((BValueArray) value));
                }
                MapMessageUtil.writeBodyMap(message.getBodyBuffer(), map);
            }
        } else if (messageType == Message.STREAM_TYPE) {
            @SuppressWarnings(ArtemisConstants.UNCHECKED)
            BMap<String, BValue> streamObj = (BMap<String, BValue>) dataVal;
            Channel channel = (Channel) streamObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            message.setBodyInputStream(channel.getInputStream());
        }
        messageObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT,
                                 sessionObj.getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT));
        messageObj.addNativeData(ArtemisConstants.ARTEMIS_MESSAGE, message);
    }

    private byte getMessageType(String type) {
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

    private long getIntFromIntOrNil(BValue value, long defaultVal) {
        if (value instanceof BInteger) {
            return ((BInteger) value).intValue();
        }
        return defaultVal;
    }
}
