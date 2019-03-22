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

import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
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

/**
 * Extern function for setting a property to a Artemis message.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA, packageName = ArtemisConstants.ARTEMIS,
        functionName = "putProperty",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = ArtemisConstants.MESSAGE_OBJ,
                             structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS),
        args = {
                @Argument(name = "key", type = TypeKind.STRING),
                @Argument(name = "value", type = TypeKind.UNION)
        }
)
public class PutProperty extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> messageObj = (BMap<String, BValue>) context.getRefArgument(0);
        ClientMessage message = (ClientMessage) messageObj.getNativeData(ArtemisConstants.ARTEMIS_MESSAGE);

        String key = context.getStringArgument(0);
        BValue valObj = context.getRefArgument(1);
        if (valObj instanceof BString) {
            message.putStringProperty(key, valObj.stringValue());
        } else if (valObj instanceof BInteger) {
            message.putLongProperty(key, ((BInteger) valObj).intValue());
        } else if (valObj instanceof BFloat) {
            message.putDoubleProperty(key, ((BFloat) valObj).floatValue());
        } else if (valObj instanceof BBoolean) {
            message.putBooleanProperty(key, ((BBoolean) valObj).booleanValue());
        } else if (valObj instanceof BByte) {
            message.putByteProperty(key, (byte) ((BByte) valObj).byteValue());
        } else if (valObj instanceof BValueArray) {
            message.putBytesProperty(key, ((BValueArray) valObj).getBytes());
        }//else is not needed because these are the only values supported by the Ballerina the method
    }
}
