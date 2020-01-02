/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nats;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

import static org.ballerinalang.nats.Constants.ON_ERROR_RESOURCE;
import static org.ballerinalang.nats.Constants.ON_MESSAGE_RESOURCE;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Abstract class providing/defining the functionality for compiler add-ons for NATS consumer services.
 */
public abstract class AbstractNatsConsumerServiceCompilerPlugin extends AbstractCompilerPlugin {
    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        validateAnnotationPresence(serviceNode, annotations);
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        if (resources.size() != 2) {
            logDiagnostic(ERROR, serviceNode.getPosition(),
                    "Exactly two resource functions should be present in the service");
        }
        resources.forEach(resource -> validateResourceFunctionSyntax(resource, serviceNode));
    }

    public abstract void validateAnnotationPresence(ServiceNode serviceNode,
            List<AnnotationAttachmentNode> annotations);

    public abstract void logDiagnostic(Diagnostic.Kind diagnosticKind, Diagnostic.DiagnosticPosition diagnosticPosition,
            String errorMessage);

    public abstract void validateMessageParameter(BLangSimpleVariable firstParameter, BLangFunction resourceFunction,
            String errorMessage);

    public abstract String getInvalidMessageResourceSignatureErrorMessage(ServiceNode serviceNode,
            BLangFunction resourceFunction);

    public abstract String getInvalidErrorResourceSignatureErrorMessage(ServiceNode serviceNode,
            BLangFunction resourceFunction);

    private void validateResourceFunctionSyntax(BLangFunction resourceFunction, ServiceNode serviceNode) {
        String resourceFunctionName = resourceFunction.getName().getValue();
        switch (resourceFunctionName) {
        case ON_MESSAGE_RESOURCE:
            validateOnMessageResource(serviceNode, resourceFunction);
            break;
        case ON_ERROR_RESOURCE:
            validateOnErrorResource(serviceNode, resourceFunction);
            break;
        default:
            String errorMessage = "Invalid resource function name %s in service %s";
            logDiagnostic(ERROR, resourceFunction.getPosition(),
                    String.format(errorMessage, resourceFunctionName, serviceNode.getName().getValue()));
        }
    }

    private void validateOnMessageResource(ServiceNode serviceNode, BLangFunction resourceFunction) {
        String errorMessage = getInvalidMessageResourceSignatureErrorMessage(serviceNode, resourceFunction);
        List<BLangSimpleVariable> functionParameters = resourceFunction.getParameters();
        if (functionParameters.size() == 1) {
            BLangSimpleVariable firstParameter = functionParameters.get(0);
            validateMessageParameter(firstParameter, resourceFunction, errorMessage);
        } else if (functionParameters.size() == 2) {
            BLangSimpleVariable firstParameter = functionParameters.get(0);
            validateMessageParameter(firstParameter, resourceFunction, errorMessage);
            BLangSimpleVariable secondParamter = functionParameters.get(1);
            validateDataTypeParameter(secondParamter, resourceFunction, errorMessage);
        } else {
            logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
        }
    }

    private void validateDataTypeParameter(BLangSimpleVariable secondParameter, BLangFunction resourceFunction,
            String errorMessage) {
        BType secondParamType = secondParameter.getTypeNode().type;
        int secondParamTypeTag = secondParamType.tag;
        if ((secondParamTypeTag == TypeTags.ARRAY)) {
            BType elementType = ((BArrayType) secondParamType).getElementType();
            if (elementType.tag != TypeTags.BYTE) {
                logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
            }
        } else if (secondParamTypeTag != TypeTags.BOOLEAN && secondParamTypeTag != TypeTags.STRING
                && secondParamTypeTag != TypeTags.INT && secondParamTypeTag != TypeTags.FLOAT
                && secondParamTypeTag != TypeTags.DECIMAL && secondParamTypeTag != TypeTags.XML
                && secondParamTypeTag != TypeTags.JSON && secondParamTypeTag != TypeTags.RECORD) {
            logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
        }
    }

    private void validateOnErrorResource(ServiceNode serviceNode, BLangFunction resourceFunction) {
        String errorMessage = getInvalidErrorResourceSignatureErrorMessage(serviceNode, resourceFunction);
        List<BLangSimpleVariable> functionParameters = resourceFunction.getParameters();

        if (functionParameters.size() == 2) {
            BLangSimpleVariable firstParameter = functionParameters.get(0);
            validateMessageParameter(firstParameter, resourceFunction, errorMessage);
            BLangSimpleVariable secondParameter = functionParameters.get(1);
            validateErrorParameter(secondParameter, resourceFunction, errorMessage);
        } else {
            logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
        }
    }

    private void validateErrorParameter(BLangSimpleVariable parameterUnderValidation, BLangFunction resourceFunction,
            String errorMessage) {
        BType parameterType = parameterUnderValidation.getTypeNode().type;
        int parameterTypeTag = parameterType.tag;
        if (parameterTypeTag != TypeTags.ERROR) {
            logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
        }
    }
}
