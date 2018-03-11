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

package org.ballerinalang.net.ws.nativeimpl.connection;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.ws.WebSocketConnectionManager;
import org.ballerinalang.net.ws.WebSocketConstants;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;

/**
 * Get the ID of the connection.
 *
 * @since 0.94
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "net.ws",
        functionName = "closeConnection",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection",
                             structPackage = "net.ws"),
        args = {@Argument(name = "statusCode", type = TypeKind.INT),
                @Argument(name = "reason", type = TypeKind.STRING)},
        isPublic = true
)
public class CloseConnection extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct wsConnection = (BStruct) getRefArgument(context, 0);
        int statusCode = (int) getIntArgument(context, 0);
        String reason = getStringArgument(context, 0);
        Session session = (Session) wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION);
        try {
            session.close(new CloseReason(() -> statusCode, reason));
        } catch (IOException e) {
            throw new BallerinaException("Could not close the connection: " + e.getMessage());
        } finally {
            WebSocketConnectionManager.getInstance().removeConnection(session.getId());
        }
        return VOID_RETURN;
    }
}
