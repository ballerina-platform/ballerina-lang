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
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

/**
 * Get the ID of the connection.
 *
 * @since 0.94
 */

@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "cancelHandshake",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "HandshakeConnection",
                             structPackage = "ballerina.net.ws"),
        args = {@Argument(name = "statusCode", type = TypeKind.INT),
                @Argument(name = "reason", type = TypeKind.STRING)},
        isPublic = true
)
public class CancelHandshake extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct handshakeConnection = (BStruct) getRefArgument(context, 0);
        int statusCode = (int) getIntArgument(context, 0);
        String reason = getStringArgument(context, 0);
        WebSocketInitMessage initMessage =
                (WebSocketInitMessage) handshakeConnection.getNativeData(Constants.WEBSOCKET_MESSAGE);
        initMessage.cancelHandShake(statusCode, reason);
        return VOID_RETURN;
    }
}
