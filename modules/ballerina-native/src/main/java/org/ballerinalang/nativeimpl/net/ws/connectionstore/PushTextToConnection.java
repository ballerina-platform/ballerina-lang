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

package org.ballerinalang.nativeimpl.net.ws.connectionstore;

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
import javax.websocket.Session;

/**
 * Push text to the connection chose by the user from the connection store.
 */
@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "pushTextToConnection",
        args = {
                @Argument(name = "connectionName", type = TypeEnum.STRING),
                @Argument(name = "text", type = TypeEnum.STRING)
        },
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value", value = "TPush text to the connection chose by " +
                             "the user from the connection store.") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "connectionName", value = "name of the stored connection") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "text", value = "Text which should be sent") })
public class PushTextToConnection extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {

        if (context.getServiceInfo() == null) {
            throw new BallerinaException("This function is only working with services");
        }

        String connectionName = getStringArgument(context, 0);
        String text = getStringArgument(context, 1);
        Session session = WebSocketConnectionManager.getInstance().getStoredConnection(connectionName);
        if (session == null) {
            throw new BallerinaException("Cannot find a connection for the connection name: " + connectionName);
        }
        try {
            session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            throw new BallerinaException("IO exception occurred during pushing text to client", e, context);
        }
        return VOID_RETURN;
    }
}
