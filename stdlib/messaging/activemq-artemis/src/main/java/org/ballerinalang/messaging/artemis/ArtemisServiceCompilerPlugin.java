/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.messaging.artemis;

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.AbstractTransportCompilerPlugin;

import java.util.List;

/**
 * Compiler plugin for validating Artemis Listener service.
 *
 * @since 0.995.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = ArtemisConstants.ARTEMIS,
                name = ArtemisConstants.LISTENER_OBJ
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(
                        packageName = ArtemisConstants.ARTEMIS,
                        name = ArtemisConstants.MESSAGE_OBJ
                )})
public class ArtemisServiceCompilerPlugin extends AbstractTransportCompilerPlugin {

    private static final String INVALID_RESOURCE_SIGNATURE_FOR = "Invalid resource signature for ";
    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(ArtemisConstants.SERVICE_CONFIG)) {
                count++;
            }
        }

        if (count == 0) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "There has to be an artemis:ServiceConfig annotation declared for service");
        }

        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        if (resources.size() > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "Only one resource is allowed in the service");
        }
        validate(resources.get(0));
    }

    private void validate(BLangFunction resource) {
        if (!isResourceReturnsErrorOrNil(resource)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, "Invalid return type: expected error?");
        }
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (paramDetails == null || paramDetails.isEmpty() || paramDetails.size() > 2) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               INVALID_RESOURCE_SIGNATURE_FOR + resource.getName().getValue() +
                                       " resource: Unexpected parameter count(expected parameter count 1 or 2)");
            return;
        }
        if (!ArtemisConstants.MESSAGE_OBJ_FULL_NAME.equals(paramDetails.get(0).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + " resource: The first parameter should be an artemis:Message");
        }
        if (paramDetails.size() == 2) {
            validateSecondParam(resource, paramDetails);
        }
    }

    private void validateSecondParam(BLangFunction resource, List<BLangSimpleVariable> paramDetails) {
        BType secondParamType = paramDetails.get(1).type;
        int secondParamTypeTag = secondParamType.tag;
        if (secondParamTypeTag != TypeTags.STRING && secondParamTypeTag != TypeTags.JSON &&
                secondParamTypeTag != TypeTags.XML && secondParamTypeTag != TypeTags.RECORD &&
                secondParamTypeTag != TypeTags.MAP && checkArrayType(secondParamType)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + " resource in service " +
                    ": The second parameter should be a string, json, xml, byte[], map or a record type");
        }
        if (secondParamTypeTag == TypeTags.MAP && checkMapConstraint(secondParamType)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + " resource in service " +
                    ": The second parameter should be a map of string, int, float, byte, boolean or byte[]");
        }
    }

    private boolean checkArrayType(BType secondParamType) {
        return secondParamType.tag != TypeTags.ARRAY || (secondParamType instanceof BArrayType &&
                ((BArrayType) secondParamType).getElementType().tag != org.ballerinalang.model.types.TypeTags.BYTE_TAG);
    }

    private boolean checkMapConstraint(BType paramType) {
        if (paramType instanceof BMapType) {
            BType constraintType = ((BMapType) paramType).constraint;
            int constraintTypeTag = constraintType.tag;
            return constraintTypeTag != TypeTags.STRING && constraintTypeTag != TypeTags.INT &&
                    constraintTypeTag != TypeTags.FLOAT && constraintTypeTag != TypeTags.BYTE &&
                    constraintTypeTag != TypeTags.BOOLEAN && checkArrayType(constraintType);
        }
        return false;
    }
}


