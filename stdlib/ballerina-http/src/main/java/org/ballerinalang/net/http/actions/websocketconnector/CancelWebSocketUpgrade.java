/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.http.actions.websocketconnector;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "cancelWebSocketUpgrade",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = HttpConstants.CONNECTION,
                             structPackage = "ballerina.http"),
        args = {
                @Argument(name = "status", type = TypeKind.INT),
                @Argument(name = "reason", type = TypeKind.STRING)
        },
        isPublic = true
)
public class CancelWebSocketUpgrade extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct httpConnection = (BStruct) context.getRefArgument(0);
        int statusCode = (int) context.getIntArgument(0);
        String reason = context.getStringArgument(0);
        WebSocketInitMessage initMessage =
                (WebSocketInitMessage) httpConnection.getNativeData(WebSocketConstants.WEBSOCKET_MESSAGE);
        if (initMessage == null) {
            throw new BallerinaConnectorException("Not a WebSocket upgrade request. Cannot cancel the request");
        }
        initMessage.cancelHandShake(statusCode, reason);
        context.setReturnValues();
    }
}
