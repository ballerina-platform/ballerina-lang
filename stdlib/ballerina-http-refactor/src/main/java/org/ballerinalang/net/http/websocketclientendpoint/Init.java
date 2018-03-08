/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.websocketclientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.BallerinaHttpServerConnector;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ServiceEndpoint",
                             structPackage = "ballerina.net.http"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration")},
        isPublic = true
)
public class Init extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        Struct clientEndpointConfig = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        String remoteUrl = clientEndpointConfig.getStringField(WebSocketConstants.CLIENT_URL_CONFIG);
        String clientServiceName = clientEndpointConfig.getStringField(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        BallerinaHttpServerConnector httpServerConnector = (BallerinaHttpServerConnector) ConnectorUtils.
                getBallerinaServerConnector(context, HttpConstants.HTTP_PACKAGE_PATH);
        final WebSocketService wsService =
                httpServerConnector.getWebSocketServicesRegistry().getClientService(clientServiceName);
        if (wsService == null) {
            throw new BallerinaConnectorException("Cannot find client service: " + clientServiceName);
        }

        BRefType bSubProtocolsBRefType = clientConfig.getRefField(0);
        String wsParentConnectionID = clientConfig.getStringField(0);
        BRefType<BMap<BString, BString>> bCustomHeaders = clientConfig.getRefField(1);
        int idleTimeoutInSeconds = (int) clientConfig.getIntField(0);
        WsClientConnectorConfig clientConnectorConfig = new WsClientConnectorConfig(remoteUrl);
        clientConnectorConfig.setTarget(clientServiceName);
        if (bSubProtocolsBRefType != null) {
            clientConnectorConfig.setSubProtocols(getSubProtocols(bSubProtocolsBRefType));
        }
        if (bCustomHeaders != null) {
            clientConnectorConfig.addHeaders(getCustomHeaders(bCustomHeaders));
        }
        if (idleTimeoutInSeconds > 0) {
            clientConnectorConfig.setIdleTimeoutInMillis(idleTimeoutInSeconds * 1000);
        }

        ClientConnectorFuture connectorFuture = new ClientConnectorFuture();
        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
        WebSocketClientConnector clientConnector =
                connectorFactory.createWsClientConnector(clientConnectorConfig);
    }
}
