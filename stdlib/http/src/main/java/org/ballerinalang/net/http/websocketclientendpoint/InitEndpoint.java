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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "WebSocketClient",
                             structPackage = "ballerina.http"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration")},
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct clientEndpointConfig = clientEndpoint.getStructField(HttpConstants.CLIENT_ENDPOINT_CONFIG);

        String remoteUrl = clientEndpointConfig.getStringField(WebSocketConstants.CLIENT_URL_CONFIG);
        Value clientServiceType = clientEndpointConfig.getTypeField(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        Service service = BLangConnectorSPIUtil.getServiceFromType(context.getProgramFile(), clientServiceType);
        if (service == null) {
            throw new BallerinaConnectorException("Cannot find client service: " + clientServiceType);
        }
        if (WebSocketConstants.WEBSOCKET_CLIENT_ENDPOINT_NAME.equals(service.getEndpointName())) {
            WebSocketService wsService = new WebSocketService(service);
            WsClientConnectorConfig clientConnectorConfig = new WsClientConnectorConfig(remoteUrl);
            clientConnectorConfig.setAutoRead(false); // Frames are read sequentially in ballerina.
            Value[] subProtocolValues = clientEndpointConfig
                    .getArrayField(WebSocketConstants.CLIENT_SUBPROTOCOLS_CONFIG);
            if (subProtocolValues != null) {
                clientConnectorConfig.setSubProtocols(Arrays.stream(subProtocolValues).map(Value::getStringValue)
                                                              .toArray(String[]::new));
            }
            Map<String, Value> headerValues = clientEndpointConfig.getMapField(
                    WebSocketConstants.CLIENT_CUSTOMHEADERS_CONFIG);
            if (headerValues != null) {
                clientConnectorConfig.addHeaders(getCustomHeaders(headerValues));
            }

            long idleTimeoutInSeconds = clientEndpointConfig.getIntField(WebSocketConstants.CLIENT_IDLETIMOUT_CONFIG);
            if (idleTimeoutInSeconds > 0) {
                clientConnectorConfig.setIdleTimeoutInMillis((int) (idleTimeoutInSeconds * 1000));
            }

            clientEndpointConfig.addNativeData(WebSocketConstants.CLIENT_SERVICE_CONFIG, wsService);
            clientEndpointConfig.addNativeData(WebSocketConstants.CLIENT_CONNECTOR_CONFIGS, clientConnectorConfig);
        } else {
            throw new BallerinaConnectorException("Incorrect endpoint: " + service.getEndpointName());
        }
        context.setReturnValues();
    }
    Map<String, String> getCustomHeaders(Map<String, Value> headers) {
        Map<String, String> customHeaders = new HashMap<>();
        headers.keySet().forEach(
                key -> customHeaders.put(key, headers.get(key).getStringValue())
        );
        return customHeaders;
    }
}
