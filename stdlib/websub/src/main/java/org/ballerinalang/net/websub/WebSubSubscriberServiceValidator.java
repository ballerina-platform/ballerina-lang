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

import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.GENERIC_SUBSCRIBER_SERVICE_TYPE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_CALLER;

/**
 * Resource validator for WebSub Subscriber Services.
 *
 * @since 0.965.0
 */
public class WebSubSubscriberServiceValidator {

    private static final int CUSTOM_RESOURCE_PARAM_COUNT = 2;

    public static void validateDefaultResources(BLangFunction resource, boolean returnsErrorOrNil, DiagnosticLog dlog) {
        String resourceName = resource.getName().getValue();
        switch (resourceName) {
            case RESOURCE_NAME_ON_INTENT_VERIFICATION:
                validateOnIntentVerificationResource(resource, returnsErrorOrNil, dlog);
                break;
            case RESOURCE_NAME_ON_NOTIFICATION:
                validateOnNotificationResource(resource, returnsErrorOrNil, dlog);
                break;
            default:
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.getPosition(), "invalid resource name '"
                        + resourceName + "' only two resources allowed with " + WEBSUB_PACKAGE + ":"
                        + GENERIC_SUBSCRIBER_SERVICE_TYPE + ", '" + RESOURCE_NAME_ON_INTENT_VERIFICATION + "' and '"
                        + RESOURCE_NAME_ON_NOTIFICATION + "'");
        }
    }

    private static void validateOnIntentVerificationResource(BLangFunction resource, boolean returnsErrorOrNil,
                                                             DiagnosticLog dlog) {
        validateResourceReturnType(returnsErrorOrNil, dlog, resource.pos);
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (isValidParamNumber(resource, paramDetails, 2, resource.getName().getValue(), dlog)) {
            validateStructType(resource.getName().getValue(), paramDetails.get(0), WEBSUB_PACKAGE,
                               WEBSUB_SERVICE_CALLER, "first", dlog);
            validateStructType(resource.getName().getValue(), paramDetails.get(1), WEBSUB_PACKAGE,
                               WEBSUB_INTENT_VERIFICATION_REQUEST, "second", dlog);
        }
    }

    private static void validateOnNotificationResource(BLangFunction resource, boolean returnsErrorOrNil,
                                                       DiagnosticLog dlog) {
        validateResourceReturnType(returnsErrorOrNil, dlog, resource.pos);
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (isValidParamNumber(resource, paramDetails, 1, resource.getName().getValue(), dlog)) {
            validateStructType(resource.getName().getValue(), paramDetails.get(0), WEBSUB_PACKAGE,
                               WEBSUB_NOTIFICATION_REQUEST, "first", dlog);
        }
    }

    private static void validateCustomNotificationResource(HttpResource resource, String packageName,
                                                           String structName) {
        List<BType> paramTypes = resource.getParamTypes();
        validateCustomParamNumber(paramTypes, resource.getName());
        validateStructType(resource.getName(), paramTypes.get(0), WEBSUB_PACKAGE, WEBSUB_NOTIFICATION_REQUEST);
        validateStructType(resource.getName(), paramTypes.get(1), packageName, structName);
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
                validateCustomNotificationResource(resource, resourceParamDetails[0],
                                                   resourceParamDetails[1]);
            }
        }
        return invalidResourceNames;
    }

    private static boolean isValidParamNumber(BLangFunction resource, List<BLangSimpleVariable> paramDetails,
                                              int expectedSize, String resourceName, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() != expectedSize) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, "invalid param count for WebSub Resource '"
                    + resourceName + "', expected: " + expectedSize + " found: "
                    + (paramDetails == null ? 0 : paramDetails.size()));
            return false;
        }
        return true;
    }

    private static void validateCustomParamNumber(List<BType> paramTypes, String resourceName) {
        if (paramTypes == null || paramTypes.size() < CUSTOM_RESOURCE_PARAM_COUNT) {
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

    private static void validateStructType(String resourceName, BType paramVarType, String packageName,
                                           String structName) {
        if (!(packageName.concat(":").concat(structName)).equals((paramVarType.getQualifiedName()))) {
            throw new BallerinaException(
                    String.format("Invalid parameter type %s in resource %s. Requires %s:%s",
                                  paramVarType.getQualifiedName(), resourceName, packageName, structName));
        }
    }

    private static void validateResourceReturnType(boolean resourceReturnsErrorOrNil, DiagnosticLog dlog,
                                                   DiagnosticPos pos) {
        if (!resourceReturnsErrorOrNil) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "invalid return type: expected error?");
        }
    }
}
