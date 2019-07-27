/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.actions.websocketconnector;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.WebSocketUtil;

import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsConnectionError;
import static org.ballerinalang.net.http.WebSocketUtil.createWebSocketError;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = WebSocketConstants.BALLERINA_ORG,
        packageName = WebSocketConstants.PACKAGE_HTTP,
        functionName = "ready",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = WebSocketConstants.WEBSOCKET_CONNECTOR,
                structPackage = WebSocketConstants.FULL_PACKAGE_HTTP
        )
)
public class Ready {

    public static Object ready(Strand strand, ObjectValue wsConnection) {
        //TODO : NonBlockingCallback is temporary fix to handle non blocking call
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        try {
            WebSocketOpenConnectionInfo connectionInfo = (WebSocketOpenConnectionInfo) wsConnection
                    .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
            boolean isReady = wsConnection.getBooleanValue(WebSocketConstants.CONNECTOR_IS_READY_FIELD);
            if (!isReady) {
                WebSocketUtil.readFirstFrame(connectionInfo.getWebSocketConnection(), wsConnection);
                //TODO remove this call back
                callback.setReturnValues(null);
            } else {
                //TODO remove this call back
                callback.setReturnValues(WebSocketUtil.createWebSocketError("Already started reading frames"));
            }
            callback.notifySuccess();
        } catch (Exception e) {
            //TODO remove this call back
            callback.setReturnValues(createWebSocketError(WsConnectionError, e.getMessage()));
            callback.notifySuccess();
        }
        return null;
    }

    private Ready() {
    }
}
