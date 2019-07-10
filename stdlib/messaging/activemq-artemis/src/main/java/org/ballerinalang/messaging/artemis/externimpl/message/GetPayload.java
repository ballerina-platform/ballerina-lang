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

import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.reader.MapMessageUtil;
import org.apache.activemq.artemis.reader.TextMessageUtil;
import org.apache.activemq.artemis.utils.collections.TypedProperties;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.Map;

/**
 * Extern function to get the payload from a message.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "getPayload",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.MESSAGE_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        ),
        args = {
                @Argument(
                        name = "key",
                        type = TypeKind.STRING
                )
        }
)
public class GetPayload extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object getPayload(Strand strand, ObjectValue messageObj) {
        ClientMessage message = (ClientMessage) messageObj.getNativeData(ArtemisConstants.ARTEMIS_MESSAGE);
        byte messageType = message.getType();
        if (messageType == Message.TEXT_TYPE) {
            ActiveMQBuffer msgBuffer = message.getBodyBuffer();
            return TextMessageUtil.readBodyText(msgBuffer).toString();
        } else if (messageType == Message.BYTES_TYPE || messageType == Message.DEFAULT_TYPE) {
            return ArtemisUtils.getArrayValue(message);
        } else if (messageType == Message.MAP_TYPE) {
            ActiveMQBuffer msgBuffer = message.getBodyBuffer();
            TypedProperties properties = MapMessageUtil.readBodyMap(msgBuffer);
            Map<String, Object> map = properties.getMap();
            MapValue<String, Object> mapObj = getMapObj(map.entrySet().iterator().next().getValue());
            if (mapObj != null) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    mapObj.put(entry.getKey(), ArtemisUtils.getValidObj(entry.getValue()));
                }
                return mapObj;
            } else {
                return ArtemisUtils.getError("Unsupported type");
            }
        } else if (messageType == Message.STREAM_TYPE) {
            return ArtemisUtils.getError("Use the saveToFile function for STREAM type message");
        } else {
            return ArtemisUtils.getError("Unsupported type");
        }
    }

    private static MapValue<String, Object> getMapObj(Object val) {
        if (val instanceof String) {
            return new MapValueImpl<>(new BMapType(BTypes.typeString));
        } else if (val instanceof Long) {
            return new MapValueImpl<>(new BMapType(BTypes.typeInt));
        } else if (val instanceof Float || val instanceof Double) {
            return new MapValueImpl<>(new BMapType(BTypes.typeFloat));
        } else if (val instanceof Byte || val instanceof Integer) {
            return new MapValueImpl<>(new BMapType(BTypes.typeByte));
        } else if (val instanceof byte[] || val instanceof int[]) {
            return new MapValueImpl<>(new BMapType(BTypes.fromString("byte[]")));
        } else if (val instanceof Boolean) {
            return new MapValueImpl<>(new BMapType(BTypes.typeBoolean));
        }
        return null;
    }
}
