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

package org.ballerinalang.net.http.clientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.HTTP_CLIENT;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;
import static org.ballerinalang.net.http.HttpUtil.getConnectionManager;
import static org.ballerinalang.net.http.HttpUtil.populateSenderConfigurations;

/**
 * Initialization of client endpoint.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "createSimpleHttpClient",
        args = {@Argument(name = "uri", type = TypeKind.STRING),
                @Argument(name = "client", type = TypeKind.OBJECT, structType = "Client")},
        isPublic = true
)
public class CreateSimpleHttpClient extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

    }

    @SuppressWarnings("unchecked")
    public static ObjectValue createSimpleHttpClient(Strand strand, String urlString,
                                                     MapValue<String, Object> clientEndpointConfig,
                                                     MapValue<String, Integer> globalPoolConfig) {
        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();
        String scheme;
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new BallerinaException("Malformed URL: " + urlString);
        }
        scheme = url.getProtocol();
        Map<String, Object> properties =
                HttpConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration =
                HttpConnectorUtil.getSenderConfiguration(connectionManager.getTransportConfig(), scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        populateSenderConfigurations(senderConfiguration, clientEndpointConfig);
        ConnectionManager poolManager;
        MapValue<String, Integer> userDefinedPoolConfig = (MapValue<String, Integer>) clientEndpointConfig.get(
                HttpConstants.USER_DEFINED_POOL_CONFIG);

        if (userDefinedPoolConfig == null) {
            poolManager = getConnectionManager(globalPoolConfig);
        } else {
            poolManager = getConnectionManager(userDefinedPoolConfig);
        }

        HttpClientConnector httpClientConnector = HttpUtil.createHttpWsConnectionFactory()
                .createHttpClientConnector(properties, senderConfiguration, poolManager);
        ObjectValue httpClient = BallerinaValues.createObjectValue(HTTP_PACKAGE_PATH, HTTP_CLIENT);
        httpClient.set(HttpConstants.CLIENT_ENDPOINT_SERVICE_URI, urlString);
        httpClient.set(HttpConstants.CLIENT_ENDPOINT_CONFIG, clientEndpointConfig);
        httpClient.addNativeData(HttpConstants.HTTP_CLIENT, httpClientConnector);
        httpClient.addNativeData(HttpConstants.CLIENT_ENDPOINT_CONFIG, clientEndpointConfig);
        clientEndpointConfig.addNativeData(HttpConstants.HTTP_CLIENT, httpClientConnector);
        return httpClient;
    }
}
