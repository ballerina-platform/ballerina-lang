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
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.net.ws.Constants;
import org.wso2.carbon.transport.http.netty.contract.websocket.WsClientConnectorConfig;

/**
 * Initialize the WebSocket client connector.
 *
 * @since 0.94
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
        }
)
public class Init extends AbstractNativeAction {
    @Override
    public ConnectorFuture execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        String remoteUrl = bconnector.getStringField(0);
        String clientServiceName = bconnector.getStringField(1);

        WsClientConnectorConfig senderConfiguration = new WsClientConnectorConfig(remoteUrl);
        senderConfiguration.setTarget(clientServiceName);
        bconnector.setNativeData(Constants.NATIVE_DATA_SENDER_CONFIG, senderConfiguration);
        ClientConnectorFuture future = new ClientConnectorFuture();
        future.notifySuccess();
        return future;
    }
}
