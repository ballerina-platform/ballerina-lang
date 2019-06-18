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

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.AbstractTransportCompilerPlugin;

import java.util.List;

/**
 * Compiler plugin for validating RabbitMQ Listener service.
 *
 * @since 0.995.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = RabbitMQConstants.RABBITMQ,
                name = RabbitMQConstants.CHANNEL_LISTENER_OBJECT
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(
                        packageName = RabbitMQConstants.RABBITMQ,
                        name = RabbitMQConstants.MESSAGE_OBJECT
                )})
public class RabbitMQServiceCompilerPlugin extends AbstractTransportCompilerPlugin {

    // TODO: make a checklist for checks needed and check for them
    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(RabbitMQConstants.SERVICE_CONFIG)) {
                count++;
            }
        }
        if (count > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                    "There cannot be more than one rabbitmq:ServiceConfig annotations");
        } else if (count == 0) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                    "There has to be a rabbitmq:ServiceConfig annotation declared for service");
        }

        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        if (resources.size() > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                    "Only one resource is allowed in the service");
        }
        validate(resources.get(0));
    }

    private void validate(BLangFunction resource) {
        if (!isResourceReturnsErrorOrNil(resource)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                    "Invalid return type: expected error?");
        }
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        if (paramDetails == null || paramDetails.isEmpty() || paramDetails.size() > 2) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                    "Invalid resource function signature for" + resource.getName().getValue() +
                            " resource: Unexpected parameter count(expected parameter count is 1 or 2)");
            return;
        }
        if (!RabbitMQConstants.MESSAGE_OBJ_FULL_NAME.equals(paramDetails.get(0).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, "Invalid resource signature for "
                    + resource.getName().getValue() + " resource: The first parameter should be an rabbitmq:Message");
        }
        if (paramDetails.size() == 2) {
            validateDataBindingParam(resource, resource.getParameters());
        }
    }

    private void validateDataBindingParam(BLangFunction resource, List<BLangSimpleVariable> paramDetails) {
        BType paramType = paramDetails.get(1).type;
        int paramTypeTag = paramType.tag;
        if (paramTypeTag != TypeTags.STRING && paramTypeTag != TypeTags.JSON &&
                paramTypeTag != TypeTags.XML && paramTypeTag != TypeTags.RECORD &&
                paramTypeTag != TypeTags.FLOAT && paramTypeTag != TypeTags.INT
                && validateArrayType(paramType)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, "Invalid resource signature for"
                    + resource.getName().getValue() + " resource in service " +
                    ": The second parameter can only be an int, float, string, json, xml, byte[] or a record type");
        }
    }

    private boolean validateArrayType(BType paramType) {
        return paramType.tag != TypeTags.ARRAY || (paramType instanceof BArrayType &&
                ((BArrayType) paramType).getElementType().tag != org.ballerinalang.model.types.TypeTags.BYTE_TAG);
    }
}
