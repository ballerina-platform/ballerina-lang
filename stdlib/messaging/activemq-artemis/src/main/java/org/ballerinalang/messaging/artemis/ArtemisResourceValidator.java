/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.artemis;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * Validator for Artemis resource.
 *
 * @since 0.995.0
 */
public class ArtemisResourceValidator {

    private static final String INVALID_RESOURCE_SIGNATURE_FOR = "Invalid resource signature for ";
    private static final String RESOURCE_IN_SERVICE = " resource in service ";

    public static void validate(BLangFunction resource, DiagnosticLog dlog, boolean resourceReturnsErrorOrNil,
                                boolean onErrorAvailable) {
        if (!resourceReturnsErrorOrNil) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, "Invalid return type: expected error?");
        }
        switch (resource.getName().getValue()) {
            case ArtemisConstants.ON_MESSAGE:
                validateOnMessageResource(resource, dlog, onErrorAvailable);
                break;
            case ArtemisConstants.ON_ERROR:
                validateOnErrorResource(resource, dlog);
                break;
            default:
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                        "Invalid resource name " + resource.getName().getValue() + " in service, only " +
                                ArtemisConstants.ON_MESSAGE + " and " + ArtemisConstants.ON_ERROR + " are allowed");
        }
    }

    private static void validateOnErrorResource(BLangFunction resource, DiagnosticLog dlog) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (validateParamDetailsSize(paramDetails, 2, resource, dlog)) {
            if (!ArtemisConstants.MESSAGE_OBJ_FULL_NAME.equals(paramDetails.get(0).type.toString())) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                        + resource.getName().getValue() +
                        " resource: The first parameter should be an artemis:Message");
            }
            if (!"error".equals(paramDetails.get(1).type.toString())) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, String.format(
                        "Invalid resource signature for %s resource in service : The second parameter should be " +
                                "artemis:ArtemisError",
                        resource.getName().getValue()));
            }
        }
    }

    private static void validateOnMessageResource(BLangFunction resource, DiagnosticLog dlog,
                                                  boolean onErrorAvailable) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (validateParamDetailsSize(paramDetails, 1, 2, resource, dlog)) {
            if (!ArtemisConstants.MESSAGE_OBJ_FULL_NAME.equals(paramDetails.get(0).type.toString())) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                        + resource.getName().getValue() +
                        " resource: The first parameter should be an artemis:Message");
            }
            if (paramDetails.size() == 2) {
                validateSecondParam(resource, paramDetails, dlog, onErrorAvailable);
            }
        }
    }

    private static void validateSecondParam(BLangFunction resource, List<BLangSimpleVariable> paramDetails,
                                            DiagnosticLog dlog, boolean onErrorAvailable) {
        if (!onErrorAvailable) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, "onError resource " +
                    "function is not found");
        }
        BType secondParamType = paramDetails.get(1).type;
        int secondParamTypeTag = secondParamType.tag;
        if (secondParamTypeTag != TypeTags.STRING && secondParamTypeTag != TypeTags.JSON &&
                secondParamTypeTag != TypeTags.XML && secondParamTypeTag != TypeTags.RECORD &&
                secondParamTypeTag != TypeTags.MAP && checkArrayType(secondParamType)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The second parameter should be a string, json, xml, byte[], map or a record type");
        }
        if (secondParamTypeTag == TypeTags.MAP && checkMapConstraint(secondParamType)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The second parameter should be a map of string, int, float, byte, boolean or byte[]");
        }

    }

    private static boolean checkArrayType(BType secondParamType) {
        return secondParamType.tag != TypeTags.ARRAY || (secondParamType instanceof BArrayType &&
                ((BArrayType) secondParamType).getElementType().tag != org.ballerinalang.model.types.TypeTags.BYTE_TAG);
    }

    private static boolean checkMapConstraint(BType paramType) {
        if (paramType instanceof BMapType) {
            BType constraintType = ((BMapType) paramType).constraint;
            int constraintTypeTag = constraintType.tag;
            return constraintTypeTag != TypeTags.STRING && constraintTypeTag != TypeTags.INT &&
                    constraintTypeTag != TypeTags.FLOAT && constraintTypeTag != TypeTags.BYTE &&
                    constraintTypeTag != TypeTags.BOOLEAN && checkArrayType(constraintType);
        }
        return false;
    }

    private static boolean validateParamDetailsSize(List<BLangSimpleVariable> paramDetails, int expectedSize,
                                                    BLangFunction resource, DiagnosticLog dlog) {
        boolean flag = true;
        if (paramDetails == null || paramDetails.size() != expectedSize) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": Expected parameter count = " + expectedSize);
            flag = false;
        }
        return flag;
    }

    private static boolean validateParamDetailsSize(List<BLangSimpleVariable> paramDetails, int min, int max,
                                                    BLangFunction resource, DiagnosticLog dlog) {
        boolean flag = true;
        if (paramDetails == null || paramDetails.size() < min || paramDetails.size() > max) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + ": Unexpected parameter count " +
                    "(expected parameter count between " + min + " and " + max + ")");
            flag = false;
        }
        return flag;
    }

    private ArtemisResourceValidator() {
    }
}
