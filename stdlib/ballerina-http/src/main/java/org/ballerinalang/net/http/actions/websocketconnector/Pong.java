/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.ByteBuffer;
import javax.websocket.Session;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "pong",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = WebSocketConstants.WEBSOCKET_CONNECTOR,
                structPackage = "ballerina.http"),
        args = {
                @Argument(name = "wsConnector", type = TypeKind.STRUCT),
                @Argument(name = "data", type = TypeKind.BLOB)
        }
)
public class Pong extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        try {
            BStruct wsConnection = (BStruct) context.getRefArgument(0);
            Session session = (Session) wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION);
            byte[] binaryData = context.getBlobArgument(0);
            session.getAsyncRemote().sendPong(ByteBuffer.wrap(binaryData));
        } catch (Throwable e) {
            throw new BallerinaException("Cannot send the message. Error occurred.");
        }
    }
}
