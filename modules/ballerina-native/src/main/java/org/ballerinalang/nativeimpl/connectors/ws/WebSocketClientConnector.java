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
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaConnector;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.websocket.WebSocketClientServicesRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.UUID;

/**
 * Client connector for WebSocket Client.
 */
@BallerinaConnector(
        packageName = WebSocketClientConnector.CONNECTOR_PACKAGE,
        connectorName = WebSocketClientConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "serviceUri", type = TypeEnum.STRING),
                @Argument(name = "serviceName", type = TypeEnum.STRING)
        })
@Component(
        name = "ballerina.net.connectors.ws",
        immediate = true,
        service = AbstractNativeConnector.class)
@BallerinaAnnotation(annotationName = "Description", attributes = {
        @Attribute(name = "value", value = "Native HTTP Client Connector")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "serviceUri",
                                                                        value = "Url of the service") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "serviceName",
                                                                        value = "Name of the referred service") })
public class WebSocketClientConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_PACKAGE = "ballerina.net.ws";
    public static final String CONNECTOR_NAME = "ClientConnector";

    private final String connectorID;
    private String serviceUri;
    private String serviceName;

    public WebSocketClientConnector(SymbolScope enclosingScope) {
        super(enclosingScope);
        // Generate a random unique ID since it is needed for WebSocket Client Connector.
        this.connectorID = UUID.randomUUID().toString();
    }

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 2) {
            serviceUri = bValueRefs[0].stringValue();
            serviceName = bValueRefs[1].stringValue();
        }
        try {
            initiateConnection(serviceUri, connectorID);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Could not create a WebSocket Client connector for url: " + serviceUri);
        }
        return true;
    }

    @Override
    public AbstractNativeConnector getInstance() {
        return new WebSocketClientConnector(symbolScope);
    }

    /**
     * Retrieve the ID of the client.
     *
     * @return the client unique id of this connector.
     */
    public String getConnectorID() {
        return connectorID;
    }

    private void initiateConnection(String serviceUri, String clientID) throws ClientConnectorException {
        ClientConnector clientConnector =
                BallerinaConnectorManager.getInstance().getClientConnector(Constants.PROTOCOL_WEBSOCKET);
        if (clientConnector == null) {
            throw new BallerinaException("Cannot initiate the connection since not found the connector support for WS");
        }
        WebSocketClientServicesRegistry.getInstance().mapClientIdToServiceName(clientID, serviceName);
        ControlCarbonMessage controlCarbonMessage = new ControlCarbonMessage(
                org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_OPEN);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientID);
        controlCarbonMessage.setProperty(Constants.TO, serviceUri);
        clientConnector.send(controlCarbonMessage, null);
    }
}
