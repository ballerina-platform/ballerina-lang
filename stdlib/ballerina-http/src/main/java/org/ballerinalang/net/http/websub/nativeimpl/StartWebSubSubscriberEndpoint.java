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

package org.ballerinalang.net.http.websub.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConnectorPortBindingListener;
import org.ballerinalang.net.http.serviceendpoint.AbstractHttpNativeFunction;
import org.ballerinalang.net.http.util.ConnectorStartupSynchronizer;
import org.ballerinalang.net.http.websub.BallerinaWebSubConnectionListener;
import org.ballerinalang.net.http.websub.WebSubServicesRegistry;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

import java.util.logging.LogManager;

/**
 * Set WebSub connection listener on startup.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "startWebSubSubscriberEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "WebSubSubscriberService",
                structPackage = "ballerina.net.http"),
        isPublic = true
)
public class StartWebSubSubscriberEndpoint extends AbstractHttpNativeFunction {

    @Override
    public void execute(Context context) {
        Struct webSubSubscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpoint = ConnectorSPIModelHelper.createStruct(
                (BStruct) ((BStruct) (webSubSubscriberServiceEndpoint.getVMValue())).getRefField(1));

        ServerConnector serverConnector = getServerConnector(serviceEndpoint);
        if (isHTTPTraceLoggerEnabled()) {
            ((BLogManager) BLogManager.getLogManager()).setHttpTraceLogHandler();
        }
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        WebSubServicesRegistry webSubServicesRegistry = getWebSubServicesRegistry(serviceEndpoint);
        serverConnectorFuture.setHttpConnectorListener(new BallerinaWebSubConnectionListener(webSubServicesRegistry));
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
