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

package org.ballerinalang.net.ws.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.ws.BallerinaWebSocketConnectorListener;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;

import javax.websocket.Session;

/**
 * Connect to the remote endpoint.
 *
 * @since 0.94
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "connectWithDefault",
        connectorName = Constants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR)
        },
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "Connection",
                                  structPackage = "ballerina.net.ws")}
)
public class ConnectWithDefault extends AbstractNativeWsAction {
    @Override
    public BValue execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        WsClientConnectorConfig clientConnectorConfig =
                new WsClientConnectorConfig(getUrlFromConnector(bconnector));
        clientConnectorConfig.setTarget(getClientServiceFromConnector(bconnector));

        HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
        try {
            WebSocketClientConnector clientConnector =
                    connectorFactory.createWsClientConnector(clientConnectorConfig);
            Session session = clientConnector.connect(new BallerinaWebSocketConnectorListener());
            BStruct wsConnection = createWSConnectionStruct(context, session, null);
            context.getControlStackNew().currentFrame.returnValues[0] = wsConnection;
            storeWsConnection(session.getId(), wsConnection);
            return wsConnection;
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Cannot connect to remote server: " + e.getMessage());
        }
    }
}
