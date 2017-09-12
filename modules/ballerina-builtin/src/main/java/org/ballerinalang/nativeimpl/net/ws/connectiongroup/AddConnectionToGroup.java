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
import org.ballerinalang.services.dispatchers.ws.Constants;
import org.ballerinalang.services.dispatchers.ws.WebSocketConnectionManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;

import javax.websocket.Session;

/**
 * This adds connection to a connection group so those connections become global and can be used in other services
 * as well.
 */
@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "addConnectionToGroup",
        args = {
                @Argument(name = "connectionGroupName", type = TypeEnum.STRING)
        },
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value", value = "This pushes text from server to all the " +
                             "connected clients of the service.") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "connectionGroupName", value = "Name of the connection group") })
public class AddConnectionToGroup extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {

        if (context.getServiceInfo() == null) {
            throw new BallerinaException("This function is only working with services");
        }

        CarbonMessage carbonMessage = context.getCarbonMessage();
        Session session = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
        String connectionGroupName = getStringArgument(context, 0);
        WebSocketConnectionManager.getInstance().addConnectionToGroup(connectionGroupName, session);
        return VOID_RETURN;
    }
}
