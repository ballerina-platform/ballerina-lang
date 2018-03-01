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
package org.ballerinalang.net.http.actions;


import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.Http2ClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;

import java.util.Map;

/**
 * {@code Init} is the Init action implementation of the HTTP2 Client Connector.
 *
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "<init>",
        connectorName = HttpConstants.HTTP2_CONNECTOR_NAME,
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR)
        },
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeKind.STRING),
                @Argument(name = "options", type = TypeKind.STRUCT, structType = "Options",
                          structPackage = "ballerina.net.http")
        })
public class Http2Init extends Init {

    private HttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();

    @Override
    public ConnectorFuture execute(Context context) {
        BConnector connector = (BConnector) getRefArgument(context, 0);
        String url = connector.getStringField(0);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
            connector.setStringField(0, url);
        }

        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();

        String scheme;
        if (url.startsWith("http://")) {
            scheme = HttpConstants.PROTOCOL_HTTP;
        } else if (url.startsWith("https://")) {
            scheme = HttpConstants.PROTOCOL_HTTPS;
        } else {
            throw new BallerinaException("malformed URL: " + url);
        }

        Map<String, Object> properties =
                HTTPConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration =
                HTTPConnectorUtil.getSenderConfiguration(connectionManager.getTransportConfig(), scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        BStruct options = (BStruct) connector.getRefField(HttpConstants.OPTIONS_STRUCT_INDEX);
        if (options != null) {
            populateSenderConfigurationOptions(senderConfiguration, options);
        }

        Http2ClientConnector http2ClientConnector =
                httpConnectorFactory.createHttp2ClientConnector(properties, senderConfiguration);
        connector.setNativeData(HttpConstants.HTTP2_CONNECTOR_NAME, http2ClientConnector);

        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        ballerinaFuture.notifySuccess();

        return ballerinaFuture;
    }
}
