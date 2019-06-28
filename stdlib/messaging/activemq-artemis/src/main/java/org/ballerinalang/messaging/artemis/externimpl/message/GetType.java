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
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * Extern function for getting the type of a message.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "getType",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.MESSAGE_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        )
)
public class GetType extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object getType(Strand strand, ObjectValue messageObj) {
        ClientMessage message = (ClientMessage) messageObj.getNativeData(ArtemisConstants.ARTEMIS_MESSAGE);
        byte messageType = message.getType();
        switch (messageType) {
            case Message.TEXT_TYPE:
                return "TEXT";
            case Message.BYTES_TYPE:
                return "BYTES";
            case Message.MAP_TYPE:
                return "MAP";
            case Message.STREAM_TYPE:
                return "STREAM";
            default:
                return "UNSUPPORTED";
        }
    }

}
