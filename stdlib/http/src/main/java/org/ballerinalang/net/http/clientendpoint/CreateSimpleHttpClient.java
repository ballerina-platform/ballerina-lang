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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.http.BHttpUtil;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.CLIENT;
import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_SERVICE_URI;
import static org.ballerinalang.net.http.HttpConstants.HTTP2_PRIOR_KNOWLEDGE;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;
import static org.ballerinalang.net.http.HttpUtil.getConnectionManager;
import static org.ballerinalang.net.http.HttpUtil.populateSenderConfigurations;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_2_0_VERSION;

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

    private HttpWsConnectorFactory httpConnectorFactory = HttpUtil.createHttpWsConnectionFactory();

    @Override
    public void execute(Context context) {
        BMap<String, BValue> configBStruct =
                (BMap<String, BValue>) context.getRefArgument(HttpConstants.CLIENT_ENDPOINT_CONFIG_INDEX);
        BMap<String, BValue> globalPoolConfig = (BMap<String, BValue>) context
                .getRefArgument(HttpConstants.CLIENT_GLOBAL_POOL_INDEX);
        Struct clientEndpointConfig = BLangConnectorSPIUtil.toStruct(configBStruct);
        String urlString = context.getStringArgument(HttpConstants.CLIENT_ENDPOINT_URL_INDEX);
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

        String httpVersion = clientEndpointConfig.getRefField(HttpConstants.CLIENT_EP_HTTP_VERSION).getStringValue();
        if (HTTP_2_0_VERSION.equals(httpVersion)) {
            BMap<String, BValue> http2Settings = (BMap<String, BValue>) configBStruct.get(HttpConstants.HTTP2_SETTINGS);
            boolean http2PriorKnowledge = ((BBoolean) http2Settings.get(HTTP2_PRIOR_KNOWLEDGE)).booleanValue();
            senderConfiguration.setForceHttp2(http2PriorKnowledge);
        }

        BHttpUtil.populateSenderConfigurations(senderConfiguration, clientEndpointConfig);
        ConnectionManager poolManager;
        BMap<String, BValue> userDefinedPoolConfig = (BMap<String, BValue>) configBStruct.get(
                HttpConstants.USER_DEFINED_POOL_CONFIG);

        if (userDefinedPoolConfig == null) {
            poolManager = BHttpUtil.getConnectionManager(globalPoolConfig);
        } else {
            poolManager = BHttpUtil.getConnectionManager(userDefinedPoolConfig);
        }

        HttpClientConnector httpClientConnector = httpConnectorFactory
                .createHttpClientConnector(properties, senderConfiguration, poolManager);
        BMap<String, BValue> httpClient = BLangConnectorSPIUtil.createBStruct(context.getProgramFile(),
                                                                              HTTP_PACKAGE_PATH,
                                                                              CLIENT, urlString,
                                                                              clientEndpointConfig);
        httpClient.addNativeData(HttpConstants.CLIENT, httpClientConnector);
        httpClient.addNativeData(HttpConstants.CLIENT_ENDPOINT_CONFIG, clientEndpointConfig);
        configBStruct.addNativeData(HttpConstants.CLIENT, httpClientConnector);
        context.setReturnValues((httpClient));
    }

    @SuppressWarnings("unchecked")
    public static void createSimpleHttpClient(Strand strand, ObjectValue httpClient,
                                              MapValue<String, Long> globalPoolConfig) {
        String urlString = httpClient.getStringValue(CLIENT_ENDPOINT_SERVICE_URI);
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) httpClient.get(
                CLIENT_ENDPOINT_CONFIG);
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
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setScheme(scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        String httpVersion = clientEndpointConfig.getStringValue(HttpConstants.CLIENT_EP_HTTP_VERSION);
        if (HTTP_2_0_VERSION.equals(httpVersion)) {
            MapValue<String, Object> http2Settings = (MapValue<String, Object>) clientEndpointConfig.
                    get(HttpConstants.HTTP2_SETTINGS);
            boolean http2PriorKnowledge = ((BBoolean) http2Settings.get(HTTP2_PRIOR_KNOWLEDGE)).booleanValue();
            senderConfiguration.setForceHttp2(http2PriorKnowledge);
        }

        populateSenderConfigurations(senderConfiguration, clientEndpointConfig);
        ConnectionManager poolManager;
        MapValue<String, Long> userDefinedPoolConfig = (MapValue<String, Long>) clientEndpointConfig.get(
                HttpConstants.USER_DEFINED_POOL_CONFIG);

        if (userDefinedPoolConfig == null) {
            poolManager = getConnectionManager(globalPoolConfig);
        } else {
            poolManager = getConnectionManager(userDefinedPoolConfig);
        }

        HttpClientConnector httpClientConnector = HttpUtil.createHttpWsConnectionFactory()
                .createHttpClientConnector(properties, senderConfiguration, poolManager);
        httpClient.addNativeData(HttpConstants.CLIENT, httpClientConnector);
    }
}
