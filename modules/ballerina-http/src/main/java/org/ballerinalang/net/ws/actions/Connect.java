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
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.net.ws.BallerinaWebSocketConnectorListener;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
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
        actionName = "connect",
        connectorName = Constants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR),
        }
)
public class Connect extends AbstractNativeAction {
    @Override
    public ConnectorFuture execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        WsClientConnectorConfig senderConfiguration =
                (WsClientConnectorConfig) bconnector.getnativeData(Constants.NATIVE_DATA_SENDER_CONFIG);
        BStruct wsParentConnection = (BStruct) bconnector.getnativeData(Constants.NATIVE_DATA_PARENT_CONNECTION);
        HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();

        try {
            WebSocketClientConnector clientConnector =
                    connectorFactory.createWsClientConnector(senderConfiguration);
            Session session = clientConnector.connect(new BallerinaWebSocketConnectorListener());

            ClientConnectorFuture future = new ClientConnectorFuture();
            BStruct response = createWSConnectionStruct(context, session, wsParentConnection);
            future.notifyReply(response);
            return future;
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Cannot connect to remote server: " + e.getMessage());
        }
    }

    private BStruct createWSConnectionStruct(Context context, Session session, BStruct wsParentConnection) {

        //gather package details from natives
        PackageInfo wsConnectionPackageInfo = context.getProgramFile().getPackageInfo(Constants.WEBSOCKET_PACKAGE_NAME);
        StructInfo wsConnectionStructInfo =
                wsConnectionPackageInfo.getStructInfo(Constants.STRUCT_WEBSOCKET_CONNECTION);

        //create session struct
        BStructType structType = wsConnectionStructInfo.getType();
        BStruct wsConnection = new BStruct(structType);

        wsConnection.addNativeData(Constants.NATIVE_DATA_WEBSOCKET_SESSION, session);
        wsConnection.addNativeData(Constants.NATIVE_DATA_PARENT_CONNECTION, wsParentConnection);
        return wsConnection;
    }
}
