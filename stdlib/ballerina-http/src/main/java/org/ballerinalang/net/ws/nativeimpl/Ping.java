/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.ws.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.ByteBuffer;
import javax.websocket.Session;

/**
 * Push binary data to the other end of the connection.
 *
 * @since 0.94
 */

@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "ping",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection",
                             structPackage = "ballerina.net.ws"),
        args = {@Argument(name = "binaryData", type = TypeKind.BLOB),
                @Argument(name = "timeoutInSecs", type = TypeKind.INT)},
        isPublic = true
)
public class Ping extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct wsConnection = (BStruct) getRefArgument(context, 0);
            Session session = (Session) wsConnection.getNativeData(Constants.NATIVE_DATA_WEBSOCKET_SESSION);
            byte[] binaryData = getBlobArgument(context, 0);
            session.getBasicRemote().sendPing(ByteBuffer.wrap(binaryData));
        } catch (Throwable e) {
            throw new BallerinaException("Cannot send the message. Error occurred.");
        }
        return VOID_RETURN;
    }
}
