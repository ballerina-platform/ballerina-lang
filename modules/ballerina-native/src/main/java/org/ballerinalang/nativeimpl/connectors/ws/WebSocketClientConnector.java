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

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.ws.ConnectorController;
import org.ballerinalang.nativeimpl.actions.ws.ConnectorControllerRegistry;
import org.ballerinalang.nativeimpl.actions.ws.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaConnector;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ws.WebSocketClientServicesRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.UUID;
import javax.websocket.Session;

/**
 * Client connector for WebSocket Client.
 */
@BallerinaConnector(
        packageName = WebSocketClientConnector.CONNECTOR_PACKAGE,
        connectorName = WebSocketClientConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "remoteServerUrl", type = TypeEnum.STRING),
                @Argument(name = "serviceName", type = TypeEnum.STRING)
        })
@Component(
        name = "ballerina.net.connectors.ws",
        immediate = true,
        service = AbstractNativeConnector.class)
@BallerinaAnnotation(annotationName = "Description", attributes = {
        @Attribute(name = "value", value = "Native HTTP Client Connector")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "remoteServerUrl",
                                                                        value = "Url of the service") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "serviceName",
                                                                        value = "Name of the referred service") })
public class WebSocketClientConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_PACKAGE = "ballerina.net.ws";
    public static final String CONNECTOR_NAME = "ClientConnector";

    private String remoteServerUrl;
    private String serviceName;
    private final String connectorID;

    public WebSocketClientConnector(SymbolScope enclosingScope) {
        super(enclosingScope);
        connectorID = UUID.randomUUID().toString();
        ConnectorControllerRegistry.getInstance().addConnectorController(connectorID);
    }

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 2) {
            remoteServerUrl = bValueRefs[0].stringValue();
            serviceName = bValueRefs[1].stringValue();
        }
        return true;
    }

    @Override
    public AbstractNativeConnector getInstance() {
        return new WebSocketClientConnector(symbolScope);
    }

    /**
     * Retrieve the ID of the client if it has or else create a new connection and return the ID.
     *
     * @return the client unique id of this connector.
     */
    public String getClientID(Session session) {
        ConnectorController connectorController = ConnectorControllerRegistry
                .getInstance().getConnectorController(connectorID);
        if (connectorController.clientExists(session)) {
            return connectorController.getClientID(session);
        } else {
            try {
                String clientID = connectorController.addConnection(session);
                initiateConnection(remoteServerUrl, clientID);
                return clientID;
            } catch (ClientConnectorException e) {
                connectorController.removeClient(session);
                throw new BallerinaException("Error occurred in managing connection.");
            }
        }
    }

    /**
     * Retrieve connector ID.
     *
     * @return connector ID of the connector.
     */
    public String getConnectorID() {
        return connectorID;
    }

    /**
     * Initiate the connection with the remote server.
     *
     * @param remoteUrl remote url which the connector need to be connected.
     * @param clientID ID of the WebSocket client for the communication.
     * @throws ClientConnectorException if the client service cannot be found.
     */
    private void initiateConnection(String remoteUrl, String clientID) throws ClientConnectorException {
        ClientConnector clientConnector =
                BallerinaConnectorManager.getInstance().getClientConnector(Constants.PROTOCOL_WEBSOCKET);
        if (clientConnector == null) {
            throw new BallerinaException("Cannot initiate the connection since not found the connector support for WS");
        }
        WebSocketClientServicesRegistry.getInstance().mapClientIdToServiceName(clientID, serviceName);
        ControlCarbonMessage controlCarbonMessage = new ControlCarbonMessage(
                org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_OPEN);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientID);
        controlCarbonMessage.setProperty(Constants.TO, remoteUrl);
        clientConnector.send(controlCarbonMessage, null);
    }
}
