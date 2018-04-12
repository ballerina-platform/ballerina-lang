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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.http.serviceendpoint.AbstractHttpNativeFunction;
import org.ballerinalang.net.websub.WebSubServicesRegistry;
import org.ballerinalang.net.websub.WebSubSubscriberConstants;

/**
 * Retrieve annotations specified and the callback URL to which WebSub notification should happen.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "retrieveSubscriptionParameters",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Listener",
                structPackage = WebSubSubscriberConstants.WEBSUB_PACKAGE_PATH),
        returnType = {@ReturnType(type = TypeKind.MAP)}
)
public class RetrieveSubscriptionParameters extends AbstractHttpNativeFunction {

    @Override
    public void execute(Context context) {
        Struct subscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpoint = subscriberServiceEndpoint.getStructField("serviceEndpoint");

        HttpService httpService = ((HttpService) ((WebSubServicesRegistry) serviceEndpoint.getNativeData(
                WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY)).getServicesInfoByInterface()
                .values().toArray()[0]);

        BMap<String, BString> subscriptionDetails = new BMap<>();

        Struct annotationStruct =
                httpService.getBalService().getAnnotationList(WebSubSubscriberConstants.WEBSUB_PACKAGE_PATH,
                              WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG).get(0).getValue();
        subscriptionDetails.put(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP,
                                    new BString(Boolean.toString(annotationStruct.getBooleanField(
                                            WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP))));
        subscriptionDetails.put(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_RESOURCE_URL,
                                new BString(annotationStruct.getStringField(
                                                WebSubSubscriberConstants.ANN_WEBSUB_ATTR_RESOURCE_URL)));
        subscriptionDetails.put(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_HUB,
                                    new BString(annotationStruct.getStringField(
                                                    WebSubSubscriberConstants.ANN_WEBSUB_ATTR_HUB)));
        subscriptionDetails.put(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_TOPIC,
                                    new BString(annotationStruct.getStringField(
                                                    WebSubSubscriberConstants.ANN_WEBSUB_ATTR_TOPIC)));
        subscriptionDetails.put(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_LEASE_SECONDS,
                                    new BString(Long.toString(annotationStruct.getIntField(
                                                    WebSubSubscriberConstants.ANN_WEBSUB_ATTR_LEASE_SECONDS))));
        subscriptionDetails.put(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SECRET,
                                    new BString(annotationStruct.getStringField(
                                                    WebSubSubscriberConstants.ANN_WEBSUB_ATTR_SECRET)));

        String callback = annotationStruct.getStringField(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_CALLBACK);

        if (callback.isEmpty()) {
            //TODO: intro methods to return host+port and change instead of using connector ID, and fix http:// hack
            callback = httpService.getBasePath();
            BStruct serviceEndpointConfig = ((BStruct) ((BStruct) serviceEndpoint.getVMValue()).getRefField(3));
            if (!serviceEndpointConfig.getStringField(0).equals("") &&
                    serviceEndpointConfig.getIntField(0) != 0) {
                callback = serviceEndpointConfig.getStringField(0) + ":"
                        + serviceEndpointConfig.getIntField(0) + callback;
            } else {
                callback = getServerConnector(serviceEndpoint).getConnectorID() + callback;
            }
            if (!callback.contains("://")) {
                if (serviceEndpointConfig.getRefField(2) != null) { //if secure socket is specified
                    callback = ("https://").concat(callback);
                } else {
                    callback = ("http://").concat(callback);
                }
            }
        }

        subscriptionDetails.put(WebSubSubscriberConstants.ANN_WEBSUB_ATTR_CALLBACK, new BString(callback));
        context.setReturnValues(subscriptionDetails);
    }

}
