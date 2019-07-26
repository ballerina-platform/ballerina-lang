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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.websub.WebSubServicesRegistry;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.LISTENER_SERVICE_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_LISTENER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY;

/**
 * Register a WebSub Subscriber service.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "registerWebSubSubscriberServiceEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = WEBSUB_SERVICE_LISTENER,
                structPackage = WEBSUB_PACKAGE),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPEDESC)},
        isPublic = true
)
public class RegisterWebSubSubscriberServiceEndpoint extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static void registerWebSubSubscriberServiceEndpoint(Strand strand, ObjectValue subscriberServiceEndpoint,
                                                               ObjectValue service) {
        ObjectValue serviceEndpoint = (ObjectValue) subscriberServiceEndpoint.get(LISTENER_SERVICE_ENDPOINT);
        WebSubServicesRegistry webSubServicesRegistry =
                (WebSubServicesRegistry) serviceEndpoint.getNativeData(WEBSUB_SERVICE_REGISTRY);
        webSubServicesRegistry.registerWebSubSubscriberService(service);
    }
}
