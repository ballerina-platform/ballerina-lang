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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.websub.WebSubServicesRegistry;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.LISTENER_SERVICE_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY;

/**
 * Register a WebSub Subscriber service.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "registerWebSubSubscriberServiceEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = WEBSUB_PACKAGE),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPEDESC)},
        isPublic = true
)
public class RegisterWebSubSubscriberServiceEndpoint extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        Struct subscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        BMap<String, BValue> vmValue = (BMap<String, BValue>) subscriberServiceEndpoint.getVMValue();
        Struct serviceEndpoint = ConnectorSPIModelHelper
                .createStruct((BMap<String, BValue>) vmValue.get(LISTENER_SERVICE_ENDPOINT));
        WebSubServicesRegistry webSubServicesRegistry =
                (WebSubServicesRegistry) serviceEndpoint.getNativeData(WEBSUB_SERVICE_REGISTRY);
        webSubServicesRegistry.registerWebSubSubscriberService(service);
        context.setReturnValues();

    }
}

