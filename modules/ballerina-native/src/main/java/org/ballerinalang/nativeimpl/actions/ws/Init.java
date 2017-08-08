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

package org.ballerinalang.nativeimpl.actions.ws;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ws.Constants;
import org.ballerinalang.services.dispatchers.ws.WebSocketConnectionManager;
import org.ballerinalang.services.dispatchers.ws.WebSocketServicesRegistry;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import javax.websocket.Session;

/**
 * Initialize the WebSocket client connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "<init>",
        connectorName = Constants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR),
        },
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeEnum.STRING),
                @Argument(name = "callbackService", type = TypeEnum.STRING)
        })
@BallerinaAnnotation(annotationName = "Description",
                     attributes = {@Attribute(name = "value",
                                              value = "Initialize the connection") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "c",
                                                                        value = "WebSocket Client Connector") })
@Component(
        name = "action.net.ws.init",
        immediate = true,
        service = AbstractNativeAction.class)
public class Init extends AbstractWebSocketAction {
    @Override
    public BValue execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        String remoteUrl = bconnector.getStringField(0);
        String clientServiceName = bconnector.getStringField(1);
        ServiceInfo parentService = context.getServiceInfo();

        // Initializing a client connection.
        ClientConnector clientConnector =
                BallerinaConnectorManager.getInstance().getClientConnector(Constants.PROTOCOL_WEBSOCKET);
        CarbonMessage carbonMessage = new ControlCarbonMessage(org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_OPEN);
        carbonMessage.setProperty(Constants.REMOTE_ADDRESS, remoteUrl);
        carbonMessage.setProperty(Constants.TO, clientServiceName);
        Session clientSession;
        try {
            clientSession = (Session) clientConnector.init(carbonMessage, null, null);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Error occurred during initializing the connection to " + remoteUrl);
        }

        // Setting parent service to client service if parent service is a WS service.
        WebSocketConnectionManager connectionManager = WebSocketConnectionManager.getInstance();
        if (parentService != null && parentService.getProtocolPkgPath().equals(Constants.WEBSOCKET_PACKAGE_PATH)) {
            WebSocketServicesRegistry.getInstance().
                    setParentServiceToClientService(clientServiceName, parentService);

            // Adding client connector to parent service in connection manager.
            connectionManager.addClientConnector(parentService, bconnector, clientSession);
        } else {
            connectionManager.addClientConnectorWithoutParentService(bconnector, clientSession);
        }

        return null;
    }
}
