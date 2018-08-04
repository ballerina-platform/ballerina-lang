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
 *
 */

package org.ballerinalang.net.websub;

import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubUtils.retrieveResourceDetails;
import static org.ballerinalang.net.websub.WebSubUtils.validateParamNumber;
import static org.ballerinalang.net.websub.WebSubUtils.validateStructType;

/**
 * Resource validator for WebSub Subscriber Services.
 *
 * @since 0.965.0
 */
class WebSubSubscriberServiceValidator {

    static void validateResources(HttpService service, String topicIdentifier, WebSubServicesRegistry serviceRegistry) {
        if (topicIdentifier != null) {
            validateCustomResources(service.getResources(), serviceRegistry);
        } else {
            validateDefaultResources(service.getResources());
        }
    }

    private static void validateDefaultResources(List<HttpResource> resources) {
        for (HttpResource resource : resources) {
            String resourceName = resource.getName();
            switch (resourceName) {
                case RESOURCE_NAME_ON_INTENT_VERIFICATION:
                    validateOnIntentVerificationResource(resource.getBalResource());
                    break;
                case RESOURCE_NAME_ON_NOTIFICATION:
                    validateOnNotificationResource(resource.getBalResource());
                    break;
                default:
                    throw new BallerinaException(String.format("Invalid resource name %s for WebSubSubscriberService. "
                                                        + "Allowed resource names [%s, %s]", resourceName,
                                                        RESOURCE_NAME_ON_INTENT_VERIFICATION,
                                                        RESOURCE_NAME_ON_NOTIFICATION));
            }
        }
    }

    private static void validateOnIntentVerificationResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamNumber(paramDetails, 2, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), WEBSUB_PACKAGE, SERVICE_ENDPOINT);
        validateStructType(resource.getName(), paramDetails.get(1), WEBSUB_PACKAGE,
                           STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST);
    }

    private static void validateOnNotificationResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamNumber(paramDetails, 1, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST);
    }

    private static void validateCustomNotificationResource(Resource resource, String packageName, String structName) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamNumber(paramDetails, 2, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST);
        validateStructType(resource.getName(), paramDetails.get(1), packageName, structName);
    }

    private static void validateCustomResources(List<HttpResource> resources, WebSubServicesRegistry serviceRegistry) {
        HashMap<String, String[]> resourceDetails = retrieveResourceDetails(serviceRegistry);
        List<String> invalidResourceNames = retrieveInvalidResourceNames(resources, resourceDetails);

        if (!invalidResourceNames.isEmpty()) {
            throw new BallerinaException("Resource name(s) not included in the topic-resource mapping found: "
                                                 + invalidResourceNames);
        }
    }


    private static List<String> retrieveInvalidResourceNames(List<HttpResource> resources,
                                                             HashMap<String, String[]> resourceDetails) {
        Set<String> resourceNames = resourceDetails.keySet();
        List<String> invalidResourceNames = new ArrayList<>();

        for (HttpResource resource : resources) {
            String resourceName = resource.getName();
            if (!resourceNames.contains(resourceName)) {
                invalidResourceNames.add(resourceName);
            } else {
                String[] resourceParamDetails = resourceDetails.get(resourceName);
                validateCustomNotificationResource(resource.getBalResource(), resourceParamDetails[0],
                                                   resourceParamDetails[1]);
                resourceNames.remove(resourceName);
            }
        }

        return invalidResourceNames;
    }

}
