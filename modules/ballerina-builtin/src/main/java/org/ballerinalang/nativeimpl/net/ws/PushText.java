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
 *
 */

package org.ballerinalang.nativeimpl.net.ws;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.services.dispatchers.ws.Constants;
import org.ballerinalang.services.dispatchers.ws.WebSocketServicesRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;

import javax.websocket.Session;

/**
 * Send text to the same client who sent the message to the given WebSocket Upgrade Path.
 */

@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "pushText",
        args = {
                @Argument(name = "text", type = TypeEnum.STRING)
        },
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value", value = "This pushes text from server to the the same " +
                             "client who sent the message.") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "text", value = "Text which should be sent") })
public class PushText extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {

        if (context.getServiceInfo() == null ||
                !context.getServiceInfo().getProtocolPkgPath().equals(Constants.WEBSOCKET_PACKAGE_PATH)) {
            throw new BallerinaException("This function is only working with WebSocket services");
        }

        try {
            CarbonMessage carbonMessage = context.getCarbonMessage();
            Session session = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);;
            if (WebSocketServicesRegistry.getInstance().isClientService(context.getServiceInfo()) &&
                    session == null) {
                    throw new BallerinaException("pushing text from client service is not supported. " +
                                    "This is only supported for WebSocket to WebSocket mediation");
            }

            String text = getStringArgument(context, 0);
            session.getBasicRemote().sendText(text);
        } catch (Throwable e) {
            throw new BallerinaException("Cannot send the message. Error occurred.");
        }
        return VOID_RETURN;
    }
}
