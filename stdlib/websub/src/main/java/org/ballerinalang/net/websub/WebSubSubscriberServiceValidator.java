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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.BALLERINA;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.GENERIC_SUBSCRIBER_SERVICE_TYPE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB;
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

    private static void validateStructType(String resourceName, BLangSimpleVariable paramDetail, String packageName,
                                           String structuralTypeName, String paramPosition, DiagnosticLog dlog) {
        if (!(packageName.concat(":").concat(structuralTypeName)).equals((paramDetail.type).toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, paramDetail.pos, "invalid resource signature for '" + resourceName
                    + "', expected '" + packageName.concat(":").concat(structuralTypeName) + "' as " + paramPosition
                    + " parameter");
        }
    }

    private static void validateResourceReturnType(boolean resourceReturnsErrorOrNil, DiagnosticLog dlog,
                                                   DiagnosticPos pos) {
        if (!resourceReturnsErrorOrNil) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "invalid return type: expected error?");
        }
    }

    // Runtime Validation for Specific Subscriber Services.

    private static void validateOnIntentVerificationResource(HttpResource resource) {
        List<BType> paramTypes = resource.getParamTypes();
        validateParamCount(paramTypes, 2, resource.getName());
        validateCallerParam(paramTypes.get(0));
        validateIntentVerificationParam(paramTypes.get(1));
    }

    private static void validateCustomNotificationResource(HttpResource resource, BRecordType recordType) {
        List<BType> paramTypes = resource.getParamTypes();
        validateParamCount(paramTypes, CUSTOM_RESOURCE_PARAM_COUNT, resource.getName());
        validateNotificationParam(resource.getName(), paramTypes.get(0));
        validateRecordType(resource.getName(), paramTypes.get(1), recordType);
    }

    static void validateCustomResources(List<HttpResource> resources, WebSubServicesRegistry serviceRegistry) {
        List<String> invalidResourceNames = retrieveInvalidResourceNames(resources,
                                                                         serviceRegistry.getResourceDetails());

        if (!invalidResourceNames.isEmpty()) {
            throw BallerinaErrors.createError("Resource name(s) not included in the topic-resource mapping " +
                                                      "found: " + invalidResourceNames);
        }
    }

    private static List<String> retrieveInvalidResourceNames(List<HttpResource> resources,
                                                             HashMap<String, BRecordType> resourceDetails) {
        Set<String> resourceNames = resourceDetails.keySet();
        List<String> invalidResourceNames = new ArrayList<>();

        for (HttpResource resource : resources) {
            String resourceName = resource.getName();

            if (RESOURCE_NAME_ON_INTENT_VERIFICATION.equals(resourceName)) {
                validateOnIntentVerificationResource(resource);
            } else if (!resourceNames.contains(resourceName)) {
                invalidResourceNames.add(resourceName);
            } else {
                validateCustomNotificationResource(resource, resourceDetails.get(resourceName));
            }
        }
        return invalidResourceNames;
    }

    private static void validateParamCount(List<BType> paramTypes, int expectedCount, String resourceName) {
        int paramCount = paramTypes.size();
        if (paramCount < expectedCount) {
            throw BallerinaErrors.createError(String.format("Invalid param count for WebSub Resource '%s': expected " +
                                                                    "'%d', found '%d'",
                                                            resourceName, expectedCount, paramCount));
        }
    }

    private static void validateCallerParam(BType paramVarType) {
        if (!isExpectedObjectParam(paramVarType, WEBSUB_SERVICE_CALLER)) {
            throw BallerinaErrors.createError(
                    String.format("Invalid parameter type '%s' in resource '%s'. Requires '%s:%s'",
                                  paramVarType.getQualifiedName(), RESOURCE_NAME_ON_INTENT_VERIFICATION, WEBSUB_PACKAGE,
                                  WEBSUB_SERVICE_CALLER));
        }
    }

    private static void validateIntentVerificationParam(BType paramVarType) {
        if (!isExpectedObjectParam(paramVarType, WEBSUB_INTENT_VERIFICATION_REQUEST)) {
            throw BallerinaErrors.createError(
                    String.format("Invalid parameter type '%s' in resource '%s'. Requires '%s:%s'",
                                  paramVarType.getQualifiedName(), RESOURCE_NAME_ON_INTENT_VERIFICATION, WEBSUB_PACKAGE,
                                  WEBSUB_INTENT_VERIFICATION_REQUEST));
        }
    }

    private static void validateNotificationParam(String resourceName, BType paramVarType) {
        if (!isExpectedObjectParam(paramVarType, WEBSUB_NOTIFICATION_REQUEST)) {
            throw BallerinaErrors.createError(
                    String.format("Invalid parameter type '%s' in resource '%s'. Requires '%s:%s'",
                                  paramVarType.getQualifiedName(), resourceName, WEBSUB_PACKAGE,
                                  WEBSUB_NOTIFICATION_REQUEST));
        }
    }

    private static void validateRecordType(String resourceName, BType paramVarType, BRecordType recordType) {
        if (!TypeChecker.isSameType(paramVarType, recordType)) {
            throw BallerinaErrors.createError(
                    String.format("Invalid parameter type '%s' in resource '%s'. Requires '%s'",
                                  paramVarType.getQualifiedName(), resourceName, recordType.getQualifiedName()));
        }
    }

    private static boolean isExpectedObjectParam(BType specifiedType, String expectedWebSubRecordName) {
        if (specifiedType.getTag() != TypeTags.OBJECT_TYPE_TAG) {
            return false;
        }
        BObjectType objectType = (BObjectType) specifiedType;
        return objectType.getPackage().org.equals(BALLERINA) &&
                objectType.getPackage().name.equals(WEBSUB) &&
                objectType.getName().equals(expectedWebSubRecordName);
    }
}
