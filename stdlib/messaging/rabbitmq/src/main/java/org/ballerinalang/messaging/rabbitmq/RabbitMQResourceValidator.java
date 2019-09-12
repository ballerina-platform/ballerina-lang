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

package org.ballerinalang.messaging.rabbitmq;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * Validator for RabbitMQ resource.
 *
 * @since 0.995.0
 */
public class RabbitMQResourceValidator {

    private static final String INVALID_RESOURCE_SIGNATURE_FOR = "Invalid resource signature for ";
    private static final String RESOURCE_IN_SERVICE = " resource in service ";

    public static void validate(BLangFunction resource, DiagnosticLog dlog) {
        switch (resource.getName().getValue()) {
            case RabbitMQConstants.FUNC_ON_MESSAGE:
                validateOnMessageResource(resource, dlog);
                break;
            case RabbitMQConstants.FUNC_ON_ERROR:
                validateOnErrorResource(resource, dlog);
                break;
            default:
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                        "Invalid resource name " + resource.getName().getValue() + " in service, only " +
                RabbitMQConstants.FUNC_ON_MESSAGE + " and " + RabbitMQConstants.FUNC_ON_ERROR + " are allowed");
        }

    }

    private static void validateOnMessageResource(BLangFunction resource, DiagnosticLog dlog) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 1, 2, resource, dlog);
        if (!RabbitMQConstants.MESSAGE_OBJ_FULL_NAME.equals(paramDetails.get(0).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + " resource: The first parameter should be an rabbitmq:Message");
        }
        if (paramDetails.size() == 2) {
            validateDataBindingParam(resource, resource.getParameters(), dlog);
        }
    }

    private static void validateOnErrorResource(BLangFunction resource, DiagnosticLog dlog) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, resource, dlog);
        if (!RabbitMQConstants.MESSAGE_OBJ_FULL_NAME.equals(paramDetails.get(0).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + " resource: The first parameter should be an rabbitmq:Message");
        }
        if (paramDetails.size() < 2 || !"error".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, String.format(
                    "Invalid resource signature for %s resource in service : The second parameter should be an error",
                    resource.getName().getValue()));
        }
    }

    private static void validateParamDetailsSize(List<BLangSimpleVariable> paramDetails, int expectedSize,
                                                 BLangFunction resource, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() != expectedSize) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": Expected parameter count = " + expectedSize);
        }
    }

    private static void validateParamDetailsSize(List<BLangSimpleVariable> paramDetails, int min, int max,
                                                 BLangFunction resource, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() < min || paramDetails.size() > max) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + ": Unexpected parameter count");
        }
    }

    private static void validateDataBindingParam(BLangFunction resource, List<BLangSimpleVariable> paramDetails,
                                                 DiagnosticLog dlog) {
        BType paramType = paramDetails.get(1).type;
        int paramTypeTag = paramType.tag;
        if (paramTypeTag != TypeTags.STRING && paramTypeTag != TypeTags.JSON &&
                paramTypeTag != TypeTags.XML && paramTypeTag != TypeTags.RECORD &&
                paramTypeTag != TypeTags.FLOAT && paramTypeTag != TypeTags.INT
                && validateArrayType(paramType)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The second parameter can only be an int, float, string, json, xml, byte[] or a record type");
        }
    }

    private static boolean validateArrayType(BType paramType) {
        return paramType.tag != TypeTags.ARRAY || (paramType instanceof BArrayType &&
                ((BArrayType) paramType).getElementType().tag != org.ballerinalang.jvm.types.TypeTags.BYTE_TAG);
    }

    private RabbitMQResourceValidator() {
    }
}
