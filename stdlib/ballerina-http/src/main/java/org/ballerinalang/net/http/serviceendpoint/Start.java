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

package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.net.http.BallerinaWebSocketServerConnectorListener;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConnectorPortBindingListener;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.ballerinalang.net.http.util.ConnectorStartupSynchronizer;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

import java.util.HashSet;
import java.util.logging.LogManager;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ServiceEndpoint",
                             structPackage = "ballerina.net.http"),
        isPublic = true
)
public class Start extends AbstractHttpNativeFunction {

    @Override
    public void execute(Context context) {
        Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        ServerConnector serverConnector = getServerConnector(serviceEndpoint);
        if (isHTTPTraceLoggerEnabled()) {
            ((BLogManager) BLogManager.getLogManager()).setHttpTraceLogHandler();
        }
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        HTTPServicesRegistry httpServicesRegistry = getHttpServicesRegistry(serviceEndpoint);
        HashSet<FilterHolder> filterHolder = getFilters(serviceEndpoint);
        WebSocketServicesRegistry webSocketServicesRegistry = getWebSocketServicesRegistry(serviceEndpoint);
        serverConnectorFuture.setHttpConnectorListener(new BallerinaHTTPConnectorListener(httpServicesRegistry,
                filterHolder));
        serverConnectorFuture
                .setWSConnectorListener(new BallerinaWebSocketServerConnectorListener(webSocketServicesRegistry));
        // TODO: set startup server port binder. Do we really need it with new design?
        ConnectorStartupSynchronizer startupSynchronizer = new ConnectorStartupSynchronizer(1);
        serverConnectorFuture.setPortBindingEventListener(
                new HttpConnectorPortBindingListener(startupSynchronizer, serverConnector.getConnectorID()));

        context.setReturnValues();
    }

    private boolean isHTTPTraceLoggerEnabled() {
        // TODO: Take a closer look at this since looking up from the Config Registry here caused test failures
        return ((BLogManager) LogManager.getLogManager()).getPackageLogLevel(
                org.ballerinalang.logging.util.Constants.HTTP_TRACE_LOG) == BLogLevel.TRACE;
    }
}
