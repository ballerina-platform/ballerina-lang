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
package org.ballerinalang.net.grpc.nativeimpl.clientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ENDPOINT_URL;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.http.HttpUtil.populateSenderConfigurationOptions;

/**
 * Extern function for initializing gRPC client endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {

    private HttpWsConnectorFactory httpConnectorFactory = HttpUtil.createHttpWsConnectionFactory();

    @Override
    public void execute(Context context) {
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        // Creating client endpoint with channel as native data.
        BMap<String, BValue> endpointConfigStruct = (BMap<String, BValue>) context.getRefArgument(1);
        Struct endpointConfig = BLangConnectorSPIUtil.toStruct(endpointConfigStruct);
        String urlString = context.getStringArgument(0);
        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new BallerinaConnectorException("Malformed URL: " + urlString);
        }

        String scheme = url.getProtocol();
        Map<String, Object> properties =
                HttpConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration =
                HttpConnectorUtil.getSenderConfiguration(connectionManager.getTransportConfig(), scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        populateSenderConfigurationOptions(senderConfiguration, endpointConfig);
        senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_2_0));
        senderConfiguration.setForceHttp2(true);
        HttpClientConnector clientConnector = httpConnectorFactory.createHttpClientConnector(properties,
                senderConfiguration);

        clientEndpoint.addNativeData(CLIENT_CONNECTOR, clientConnector);
        clientEndpoint.addNativeData(ENDPOINT_URL, urlString);

    }
}
