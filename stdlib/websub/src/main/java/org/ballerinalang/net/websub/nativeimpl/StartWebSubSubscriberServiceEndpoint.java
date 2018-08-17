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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConnectorPortBindingListener;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.serviceendpoint.AbstractHttpNativeFunction;
import org.ballerinalang.net.websub.BallerinaWebSubConnectionListener;
import org.ballerinalang.net.websub.WebSubServicesRegistry;
import org.ballerinalang.net.websub.WebSubSubscriberConstants;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_HTTP_ENDPOINT;

/**
 * Set WebSub connection listener on startup.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "startWebSubSubscriberServiceEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener",
                structPackage = WebSubSubscriberConstants.WEBSUB_PACKAGE),
        isPublic = true
)
public class StartWebSubSubscriberServiceEndpoint extends AbstractHttpNativeFunction {

    @Override
    public void execute(Context context) {
        Struct subscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpoint = ((subscriberServiceEndpoint).getRefField(WEBSUB_HTTP_ENDPOINT).getStructValue());

        ServerConnector serverConnector = getServerConnector(serviceEndpoint);
        //TODO: check if isStarted check is required
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        WebSubServicesRegistry webSubServicesRegistry = (WebSubServicesRegistry) serviceEndpoint.getNativeData(
                                                                WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY);
        serverConnectorFuture.setHttpConnectorListener(
                new BallerinaWebSubConnectionListener(webSubServicesRegistry, serviceEndpoint
                        .getStructField(HttpConstants.SERVICE_ENDPOINT_CONFIG), context));
        serverConnectorFuture.setPortBindingEventListener(new HttpConnectorPortBindingListener());

        context.setReturnValues();
    }
}
