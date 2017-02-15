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

package org.wso2.ballerina.nativeimpl.net.ws;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.Attribute;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAnnotation;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.carbon.connector.framework.WebSocketSessionManager;
import org.wso2.carbon.transport.http.netty.common.Constants;

import javax.websocket.Session;

/**
 * Send text to the same user who sent the message to the given WebSocket Upgrade Path.
 *
 * @since 0.8.0
 */

@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "sendText",
        args = {
                @Argument(name = "message", type = TypeEnum.MESSAGE),
                @Argument(name = "text", type = TypeEnum.STRING)
        },
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value", value = "Send text to the same client " +
                             "who sent the message") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "message", value = "message") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "text", value = "Text which should be sent") })
public class SendText extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        Session session = null;
        try {
            if (context.getCarbonMessage().getProperty(Constants.CHANNEL_ID) != null) {
                String sessionId = (String) context.getCarbonMessage().getProperty(Constants.CHANNEL_ID);
                String uri = (String) context.getCarbonMessage().getProperty(Constants.TO);
                WebSocketSessionManager webSocketSessionManager = WebSocketSessionManager.getInstance();
                session = webSocketSessionManager.getSession(uri, sessionId);
                String text = getArgument(context, 1).stringValue();
                session.getBasicRemote().sendText(text);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Cannot send the message. Error occurred.");
        }
        return VOID_RETURN;
    }
}
