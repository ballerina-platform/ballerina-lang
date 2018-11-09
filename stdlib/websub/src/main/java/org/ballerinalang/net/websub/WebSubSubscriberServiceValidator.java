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
import org.ballerinalang.model.types.BType;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.GENERIC_SUBSCRIBER_SERVICE_TYPE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;

/**
 * Resource validator for WebSub Subscriber Services.
 *
 * @since 0.965.0
 */
public class WebSubSubscriberServiceValidator {

    private static final int CUSTOM_RESOURCE_PARAM_COUNT = 2;

    public static void validateDefaultResources(BLangResource resource, DiagnosticLog dlog) {
        String resourceName = resource.getName().getValue();
        switch (resourceName) {
            case RESOURCE_NAME_ON_INTENT_VERIFICATION:
                validateOnIntentVerificationResource(resource, dlog);
                break;
            case RESOURCE_NAME_ON_NOTIFICATION:
                validateOnNotificationResource(resource, dlog);
                break;
            default:
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.getPosition(), "invalid resource name '"
                        + resourceName + "' only two resources allowed with " + WEBSUB_PACKAGE + ":"
                        + GENERIC_SUBSCRIBER_SERVICE_TYPE + ", '" + RESOURCE_NAME_ON_INTENT_VERIFICATION + "' and '"
                        + RESOURCE_NAME_ON_NOTIFICATION + "'");
        }
    }

    private static void validateOnIntentVerificationResource(BLangResource resource, DiagnosticLog dlog) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (isValidParamNumber(resource, paramDetails, 2, resource.getName().getValue(), dlog)) {
            validateStructType(resource.getName().getValue(), paramDetails.get(0), WEBSUB_PACKAGE, SERVICE_ENDPOINT,
                               "first", dlog);
            validateStructType(resource.getName().getValue(), paramDetails.get(1), WEBSUB_PACKAGE,
                               STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST, "second", dlog);
        }
    }

    private static void validateOnNotificationResource(BLangResource resource, DiagnosticLog dlog) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (isValidParamNumber(resource, paramDetails, 1, resource.getName().getValue(), dlog)) {
            validateStructType(resource.getName().getValue(), paramDetails.get(0), WEBSUB_PACKAGE,
                               STRUCT_WEBSUB_NOTIFICATION_REQUEST, "first", dlog);
        }
    }

    private static void validateCustomNotificationResource(Resource resource, String packageName, String structName) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateCustomParamNumber(paramDetails, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST);
        validateStructType(resource.getName(), paramDetails.get(1), packageName, structName);
    }

    static void validateCustomResources(List<HttpResource> resources, WebSubServicesRegistry serviceRegistry) {
        List<String> invalidResourceNames = retrieveInvalidResourceNames(resources,
                                                                         serviceRegistry.getResourceDetails());

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
            }
        }
        return invalidResourceNames;
    }

    private static boolean isValidParamNumber(BLangResource resource, List<BLangSimpleVariable> paramDetails,
                                              int expectedSize, String resourceName, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() != expectedSize) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, "invalid param count for WebSub Resource '"
                    + resourceName + "', expected: " + expectedSize + " found: "
                    + (paramDetails == null ? 0 : paramDetails.size()));
            return false;
        }
        return true;
    }

    private static void validateCustomParamNumber(List<ParamDetail> paramDetails, String resourceName) {
        if (paramDetails == null || paramDetails.size() < CUSTOM_RESOURCE_PARAM_COUNT) {
            throw new BallerinaException(String.format("Invalid param count for WebSub Resource \"%s\"",
                                                       resourceName));
        }
    }

    private static void validateStructType(String resourceName, BLangSimpleVariable paramDetail, String packageName,
                                           String structuralTypeName, String paramPosition, DiagnosticLog dlog) {
        if (!(packageName.concat(":").concat(structuralTypeName)).equals((paramDetail.type).toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, paramDetail.pos, "invalid resource signature for '" + resourceName
                    + "', expected '" + packageName.concat(":").concat(structuralTypeName) + "' as " + paramPosition
                    + " parameter");
        }
    }

    private static void validateStructType(String resourceName, ParamDetail paramDetail, String packageName,
                                           String structName) {
        BType paramVarType = paramDetail.getVarType();
        if (!paramVarType.getPackagePath().equals(packageName)) {
            throw new BallerinaException(
                    String.format("Invalid parameter type %s:%s %s in resource %s. Requires %s:%s",
                                  paramVarType.getPackagePath(), paramVarType.getName(),
                                  paramDetail.getVarName(), resourceName, packageName, structName));
        }

        if (!paramVarType.getName().equals(structName)) {
            throw new BallerinaException(
                    String.format("Invalid parameter type %s:%s %s in resource %s. Requires %s:%s",
                                  paramVarType.getPackagePath(), paramVarType.getName(),
                                  paramDetail.getVarName(), resourceName, packageName, structName));
        }
    }
}
