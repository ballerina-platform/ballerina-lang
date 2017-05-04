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

package org.ballerinalang.nativeimpl.connectors.ws;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.services.dispatchers.http.Constants;
import org.wso2.carbon.messaging.TextCarbonMessage;

/**
 * Push Text to the server.
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "pushText",
        connectorName = WebSocketClientConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR),
                @Argument(name = "text", type = TypeEnum.STRING),
        }
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = {@Attribute(name = "value",
                                              value = "Push text to the server.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "c",
                                                                        value = "A WebSocket Client Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "text",
                                                                        value = "text which should be sent") })
public class PushText extends AbstractWebSocketAction {
    @Override
    public BValue execute(Context context) {
        BConnector bconnector = (BConnector) getArgument(context, 0);
        String text = getArgument(context, 1).stringValue();
        TextCarbonMessage textCarbonMessage = new TextCarbonMessage(text);
        textCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, getClientID(bconnector));
        pushMessage(textCarbonMessage);
        return null;
    }
}
