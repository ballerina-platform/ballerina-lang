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
import org.wso2.carbon.messaging.ControlCarbonMessage;

/**
 * Close the connection of WebSocket Client connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "close",
        connectorName = WebSocketClientConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR)
        }
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = {@Attribute(name = "value",
                                              value = "Closing the connection with server.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "c",
                                                                        value = "A WebSocket Client Connector") })
public class Close extends AbstractWebSocketAction {
    @Override
    public BValue execute(Context context) {
        BConnector bconnector = (BConnector) getArgument(context, 0);
        ControlCarbonMessage controlCarbonMessage = new ControlCarbonMessage(
                org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_CLOSE);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLOSE_CODE, 1000);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLOSE_REASON, "Normal closure");
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID,
                getClientID(context, bconnector));
        pushMessage(controlCarbonMessage);
        return null;
    }
}
