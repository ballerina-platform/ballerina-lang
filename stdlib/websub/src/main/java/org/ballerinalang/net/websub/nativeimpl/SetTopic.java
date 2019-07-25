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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.http.serviceendpoint.AbstractHttpNativeFunction;
import org.ballerinalang.net.websub.WebSubHttpService;
import org.ballerinalang.net.websub.WebSubServicesRegistry;

import java.util.Optional;

import static org.ballerinalang.net.http.HttpConstants.DEFAULT_HOST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_HTTP_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_LISTENER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY;

/**
 * Set the hub and topic the service is subscribing, if the resource URL was specified as an annotation.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "setTopic",
        args = {@Argument(name = "webSubServiceName", type = TypeKind.STRING),
                @Argument(name = "topic", type = TypeKind.STRING)},
        receiver = @Receiver(type = TypeKind.OBJECT, structType = WEBSUB_SERVICE_LISTENER,
                structPackage = WEBSUB_PACKAGE),
        isPublic = true
)
public class SetTopic extends AbstractHttpNativeFunction {

    public static void setTopic(Strand strand, ObjectValue subscriberServiceEndpoint, String webSubServiceName,
                                String topic) {
        ObjectValue serviceEndpoint = (ObjectValue) subscriberServiceEndpoint.get(WEBSUB_HTTP_ENDPOINT);
        Optional<HttpService> webSubHttpService =
                ((WebSubServicesRegistry) serviceEndpoint.getNativeData(WEBSUB_SERVICE_REGISTRY))
                        .getServicesByHost(DEFAULT_HOST).values().stream().filter(
                        httpService -> webSubServiceName.equals(httpService.getBalService().getType().getName()))
                        .findFirst();

        HttpService httpService = webSubHttpService.get();
        if (httpService instanceof WebSubHttpService) {
            ((WebSubHttpService) httpService).setTopic(topic);
        }
    }
}
