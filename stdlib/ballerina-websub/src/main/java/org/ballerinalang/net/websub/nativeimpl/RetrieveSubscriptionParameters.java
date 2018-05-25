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
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.WebSubHttpService;
import org.ballerinalang.net.websub.WebSubServicesRegistry;
import org.wso2.transport.http.netty.contract.ServerConnector;

import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.HTTP_SERVER_CONNECTOR;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_AUTH_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_CALLBACK;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_FOLLOW_REDIRECTS_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_HUB;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_RESOURCE_URL;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SECRET;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SECURE_SOCKET_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENDPOINT_CONFIG_HOST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENDPOINT_CONFIG_PORT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENDPOINT_CONFIG_SECURE_SOCKET_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_ENDPOINT_CONFIG_NAME;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_HTTP_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
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
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Listener", structPackage = WEBSUB_PACKAGE),
        returnType = {@ReturnType(type = TypeKind.ARRAY)}
)
public class RetrieveSubscriptionParameters extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        Struct subscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpoint = subscriberServiceEndpoint.getStructField(WEBSUB_HTTP_ENDPOINT);

        Object[] webSubHttpServices = ((WebSubServicesRegistry) serviceEndpoint.getNativeData(WEBSUB_SERVICE_REGISTRY))
                                        .getServicesInfoByInterface().values().toArray();

        BRefValueArray subscriptionDetailArray = new BRefValueArray();

        for (int index = 0; index < webSubHttpServices.length; index++) {
            WebSubHttpService webSubHttpService = (WebSubHttpService) webSubHttpServices[index];
            BMap<String, BValue> subscriptionDetails = new BMap<>();
            List<Annotation> webSubServiceAnnotations = webSubHttpService.getBalService()
                                                        .getAnnotationList(WEBSUB_PACKAGE,
                                                                           ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG);
            if (webSubServiceAnnotations != null) {
                //Ideally would be caught at compile time/validation and would not throw an exception here
                if (webSubServiceAnnotations.size() > 1) {
                    throw new BallerinaConnectorException("Error identifying "
                                                                  + ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG
                                                                  + ": Expected annotation count [1], found ["
                                                                  + webSubServiceAnnotations.size() + "]");
                }

                subscriptionDetails.put(WEBSUB_SERVICE_NAME,
                                new BString(webSubHttpService.getBalService().getServiceInfo().getType().getName()));

                Struct annotationStruct = webSubServiceAnnotations.get(0).getValue();
                subscriptionDetails.put(ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP,
                                        new BString(Boolean.toString(annotationStruct.getBooleanField(
                                                ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP))));
                subscriptionDetails.put(ANN_WEBSUB_ATTR_RESOURCE_URL,
                                        new BString(annotationStruct.getStringField(ANN_WEBSUB_ATTR_RESOURCE_URL)));
                subscriptionDetails.put(ANN_WEBSUB_ATTR_HUB,
                                        new BString(annotationStruct.getStringField(ANN_WEBSUB_ATTR_HUB)));
                subscriptionDetails.put(ANN_WEBSUB_ATTR_TOPIC,
                                        new BString(annotationStruct.getStringField(ANN_WEBSUB_ATTR_TOPIC)));
                subscriptionDetails.put(ANN_WEBSUB_ATTR_LEASE_SECONDS,
                                        new BString(Long.toString(annotationStruct.getIntField(
                                                ANN_WEBSUB_ATTR_LEASE_SECONDS))));
                subscriptionDetails.put(ANN_WEBSUB_ATTR_SECRET,
                                        new BString(annotationStruct.getStringField(ANN_WEBSUB_ATTR_SECRET)));

                if (annotationStruct.getRefField(ANN_WEBSUB_ATTR_AUTH_CONFIG) != null) {
                    BStruct authConfig = (BStruct) annotationStruct.getRefField(
                                                                    ANN_WEBSUB_ATTR_AUTH_CONFIG).getVMValue();
                    subscriptionDetails.put(ANN_WEBSUB_ATTR_AUTH_CONFIG, authConfig);
                } else {
                    subscriptionDetails.put(ANN_WEBSUB_ATTR_AUTH_CONFIG, null);
                }

                if (annotationStruct.getRefField(ANN_WEBSUB_ATTR_SECURE_SOCKET_CONFIG) != null) {
                    BStruct secureSocket =
                            (BStruct) annotationStruct.getRefField(ANN_WEBSUB_ATTR_SECURE_SOCKET_CONFIG).getVMValue();
                    subscriptionDetails.put(ANN_WEBSUB_ATTR_SECURE_SOCKET_CONFIG, secureSocket);
                } else {
                    subscriptionDetails.put(ANN_WEBSUB_ATTR_SECURE_SOCKET_CONFIG, null);
                }

                if (annotationStruct.getRefField(ANN_WEBSUB_ATTR_FOLLOW_REDIRECTS_CONFIG) != null) {
                    BStruct secureSocket = (BStruct) annotationStruct.getRefField(
                                                                ANN_WEBSUB_ATTR_FOLLOW_REDIRECTS_CONFIG).getVMValue();
                    subscriptionDetails.put(ANN_WEBSUB_ATTR_FOLLOW_REDIRECTS_CONFIG, secureSocket);
                } else {
                    subscriptionDetails.put(ANN_WEBSUB_ATTR_FOLLOW_REDIRECTS_CONFIG, null);
                }

                String callback = annotationStruct.getStringField(ANN_WEBSUB_ATTR_CALLBACK);

                if (callback.isEmpty()) {
                    //TODO: intro methods to return host+port and change instead of using connector ID
                    callback = webSubHttpService.getBasePath();
                    Struct serviceEndpointConfig =
                            serviceEndpoint.getRefField(SERVICE_ENDPOINT_CONFIG_NAME).getStructValue();
                    if (!serviceEndpointConfig.getStringField(ENDPOINT_CONFIG_HOST).isEmpty()
                            && serviceEndpointConfig.getIntField(ENDPOINT_CONFIG_PORT) != 0) {
                        callback = serviceEndpointConfig.getStringField(ENDPOINT_CONFIG_HOST) + ":"
                                + serviceEndpointConfig.getIntField(ENDPOINT_CONFIG_PORT) + callback;
                    } else {
                        callback = ((ServerConnector) serviceEndpoint.getNativeData(HTTP_SERVER_CONNECTOR))
                                .getConnectorID() + callback;
                    }
                    if (!callback.contains("://")) {
                        if (serviceEndpointConfig.getRefField(ENDPOINT_CONFIG_SECURE_SOCKET_CONFIG) != null) {
                            //if secure socket is specified
                            callback = ("https://").concat(callback);
                        } else {
                            callback = ("http://").concat(callback);
                        }
                    }
                }

                subscriptionDetails.put(ANN_WEBSUB_ATTR_CALLBACK, new BString(callback));
                subscriptionDetailArray.add(index, subscriptionDetails);
            }
        }


        context.setReturnValues(subscriptionDetailArray);
    }

}
