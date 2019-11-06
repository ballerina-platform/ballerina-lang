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
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.WebSubHttpService;
import org.ballerinalang.net.websub.WebSubServicesRegistry;
import org.wso2.transport.http.netty.contract.ServerConnector;

import static org.ballerinalang.net.http.HttpConstants.DEFAULT_HOST;
import static org.ballerinalang.net.http.HttpConstants.HTTP_DEFAULT_HOST;
import static org.ballerinalang.net.http.HttpConstants.HTTP_SERVER_CONNECTOR;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_CALLBACK;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_EXPECT_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SECRET;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SUBSCRIPTION_HUB_CLIENT_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants
        .ANN_WEBSUB_ATTR_SUBSCRIPTION_PUBLISHER_CLIENT_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_TARGET;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENDPOINT_CONFIG_HOST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENDPOINT_CONFIG_PORT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENDPOINT_CONFIG_SECURE_SOCKET_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_ENDPOINT_CONFIG_NAME;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_HTTP_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_LISTENER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_NAME;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY;

/**
 * Retrieve annotations specified and the callback URL to which WebSub notification should happen.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "retrieveSubscriptionParameters",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = WEBSUB_SERVICE_LISTENER,
                structPackage = WEBSUB_PACKAGE),
        returnType = {@ReturnType(type = TypeKind.ARRAY)}
)
public class RetrieveSubscriptionParameters {

    private static final String LOCALHOST = "localhost";

    @SuppressWarnings("unchecked")
    public static ArrayValue retrieveSubscriptionParameters(Strand strand, ObjectValue subscriberServiceEndpoint) {
        ArrayValue subscriptionDetailArray = new ArrayValue(new BArrayType(new BMapType(BTypes.typeAny)));
        ObjectValue serviceEndpoint = (ObjectValue) subscriberServiceEndpoint.get(WEBSUB_HTTP_ENDPOINT);
        WebSubServicesRegistry webSubServicesRegistry = ((WebSubServicesRegistry) serviceEndpoint.getNativeData(
                WEBSUB_SERVICE_REGISTRY));
        if (webSubServicesRegistry.getServicesMapHolder(DEFAULT_HOST) == null) {
            return subscriptionDetailArray;
        }
        Object[] webSubHttpServices = webSubServicesRegistry.getServicesByHost(DEFAULT_HOST).values().toArray();

        for (int index = 0; index < webSubHttpServices.length; index++) {
            WebSubHttpService webSubHttpService = (WebSubHttpService) webSubHttpServices[index];
            MapValue<String, Object> subscriptionDetails = new MapValueImpl<>();
            MapValue annotation = (MapValue) webSubHttpService.getBalService().getType()
                    .getAnnotation(WEBSUB_PACKAGE, ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG);

            subscriptionDetails.put(WEBSUB_SERVICE_NAME, webSubHttpService.getBalService().getType().getName());
            subscriptionDetails.put(ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP,
                                    annotation.getBooleanValue(ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP));

            if (annotation.containsKey(ANN_WEBSUB_ATTR_TARGET)) {
                subscriptionDetails.put(ANN_WEBSUB_ATTR_TARGET, annotation.get(ANN_WEBSUB_ATTR_TARGET));
            }

            if (annotation.containsKey(ANN_WEBSUB_ATTR_LEASE_SECONDS)) {
                subscriptionDetails.put(ANN_WEBSUB_ATTR_LEASE_SECONDS,
                                        annotation.getIntValue(ANN_WEBSUB_ATTR_LEASE_SECONDS));
            }

            if (annotation.containsKey(ANN_WEBSUB_ATTR_SECRET)) {
                subscriptionDetails.put(ANN_WEBSUB_ATTR_SECRET, annotation.getStringValue(ANN_WEBSUB_ATTR_SECRET));
            }

            subscriptionDetails.put(ANN_WEBSUB_ATTR_EXPECT_INTENT_VERIFICATION,
                                    annotation.getBooleanValue(ANN_WEBSUB_ATTR_EXPECT_INTENT_VERIFICATION));

            if (annotation.containsKey(ANN_WEBSUB_ATTR_SUBSCRIPTION_PUBLISHER_CLIENT_CONFIG)) {
                MapValue<String, Object> publisherClientConfig =
                        (MapValue<String, Object>) annotation.get(ANN_WEBSUB_ATTR_SUBSCRIPTION_PUBLISHER_CLIENT_CONFIG);
                subscriptionDetails.put(ANN_WEBSUB_ATTR_SUBSCRIPTION_PUBLISHER_CLIENT_CONFIG, publisherClientConfig);
            }

            if (annotation.containsKey(ANN_WEBSUB_ATTR_SUBSCRIPTION_HUB_CLIENT_CONFIG)) {
                MapValue<String, Object> hubClientConfig =
                        (MapValue<String, Object>) annotation.get(ANN_WEBSUB_ATTR_SUBSCRIPTION_HUB_CLIENT_CONFIG);
                subscriptionDetails.put(ANN_WEBSUB_ATTR_SUBSCRIPTION_HUB_CLIENT_CONFIG, hubClientConfig);
            }

            String callback;

            if (annotation.containsKey(ANN_WEBSUB_ATTR_CALLBACK)) {
                callback = annotation.getStringValue(ANN_WEBSUB_ATTR_CALLBACK);
            } else {
                //TODO: intro methods to return host+port and change instead of using connector ID
                callback = webSubHttpService.getBasePath();
                MapValue<String, Object> serviceEndpointConfig = (MapValue<String, Object>) serviceEndpoint.get(
                        SERVICE_ENDPOINT_CONFIG_NAME);
                long port = serviceEndpoint.getIntValue(ENDPOINT_CONFIG_PORT);
                if (!serviceEndpointConfig.getStringValue(ENDPOINT_CONFIG_HOST).isEmpty() && port != 0) {
                    callback = serviceEndpointConfig.getStringValue(ENDPOINT_CONFIG_HOST) + ":" + port + callback;
                } else {
                    callback = ((ServerConnector) serviceEndpoint.getNativeData(HTTP_SERVER_CONNECTOR))
                            .getConnectorID() + callback;
                }
                if (callback.startsWith(HTTP_DEFAULT_HOST)) {
                    callback = callback.replace(HTTP_DEFAULT_HOST, LOCALHOST);
                }
                if (!callback.contains("://")) {
                    if (serviceEndpointConfig.get(ENDPOINT_CONFIG_SECURE_SOCKET_CONFIG) != null) {
                        //if secure socket is specified
                        callback = ("https://").concat(callback);
                    } else {
                        callback = ("http://").concat(callback);
                    }
                }
            }

            subscriptionDetails.put(ANN_WEBSUB_ATTR_CALLBACK, callback);
            subscriptionDetailArray.add(index, subscriptionDetails);
        }
        return subscriptionDetailArray;
    }
}
