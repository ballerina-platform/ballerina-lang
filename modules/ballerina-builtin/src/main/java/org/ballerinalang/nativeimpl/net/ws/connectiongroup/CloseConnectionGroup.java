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

package org.ballerinalang.nativeimpl.net.ws.connectiongroup;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.services.dispatchers.ws.WebSocketConnectionManager;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.util.List;
import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * Close all the connections from connection group.
 * This will remove all the connections in a connection group and close all the connections.
 */
@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "closeConnectionGroup",
        args = {
                @Argument(name = "connectionGroupName", type = TypeEnum.STRING)
        },
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value",
                                               value = "Close all the connections in connection group")})
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "connectionGroupName", value = "Name of the connection group")})
public class CloseConnectionGroup extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {

        if (context.getServiceInfo() == null) {
            throw new BallerinaException("This function is only working with services");
        }

        String connectionGroupName = getStringArgument(context, 0);
        WebSocketConnectionManager connectionManager = WebSocketConnectionManager.getInstance();
        List<Session> sessions = connectionManager.getInstance().getConnectionGroup(connectionGroupName);
        if (sessions == null) {
            throw new BallerinaException("Connection group name " + connectionGroupName +
                                                 " not exists. Cannot close the connection group");
        }
        sessions.forEach(
                session -> {
                    if (session.isOpen()) {
                        try {
                            session.close(new CloseReason(() -> 1000, "Normal closure"));
                            connectionManager.removeSessionFromAll(session);
                        } catch (IOException e) {
                            throw new BallerinaException("Error occurred in closing connection group: "
                                                                 + connectionGroupName);
                        }
                    }
                }
        );
        connectionManager.removeConnectionGroup(connectionGroupName);
        return VOID_RETURN;
    }
}
