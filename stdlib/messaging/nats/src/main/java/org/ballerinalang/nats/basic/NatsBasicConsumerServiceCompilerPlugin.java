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
package org.ballerinalang.nats.basic;

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.nats.AbstractNatsConsumerServiceCompilerPlugin;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

import static org.ballerinalang.nats.Constants.NATS_BASIC_CONSUMER_ANNOTATION;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Providing the compiler add-ons for NATS basic consumer services.
 */
@SupportedResourceParamTypes(expectedListenerType = @SupportedResourceParamTypes.Type(packageName = "nats",
                                                                                      name = "Listener"),
                             paramTypes = {
                                     @SupportedResourceParamTypes.Type(packageName = "nats",
                                                                       name = "Message")
                             })
public class NatsBasicConsumerServiceCompilerPlugin extends AbstractNatsConsumerServiceCompilerPlugin {
    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void validateAnnotationPresence(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        if (annotations.stream().noneMatch(annotation -> annotation.getAnnotationName().getValue()
                .equals(NATS_BASIC_CONSUMER_ANNOTATION))) {
            logDiagnostic(ERROR, serviceNode.getPosition(), "nats:" + NATS_BASIC_CONSUMER_ANNOTATION
                    + " annotation is required to be declared in the consumer service");
        }
    }

    @Override
    public void logDiagnostic(Diagnostic.Kind diagnosticKind, Diagnostic.DiagnosticPosition diagnosticPosition,
            String errorMessage) {
        dlog.logDiagnostic(ERROR, diagnosticPosition, errorMessage);
    }

    @Override
    public void validateMessageParameter(BLangSimpleVariable firstParameter, BLangFunction resourceFunction,
            String errorMessage) {
        BType firstParamType = firstParameter.getTypeNode().type;
        if (firstParamType.tag != TypeTags.OBJECT) {
            logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
        } else {
            BObjectType objectType = (BObjectType) firstParamType;
            if (!objectType.tsymbol.getName().getValue().equals(Constants.NATS_MESSAGE_OBJ_NAME)) {
                logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
            }
        }
    }

    @Override
    public String getInvalidMessageResourceSignatureErrorMessage(ServiceNode serviceNode,
            BLangFunction resourceFunction) {
        String errorMessage =  "Invalid resource signature for the %s resource function in %s service. "
                + "Expected first parameter (required) type is nats:Message and the expected "
                + "second parameter (optional) type is "
                + "byte[] | boolean | string | int | float | decimal | xml | json | record {}";
        return String.format(errorMessage, resourceFunction.getName().getValue(), serviceNode.getName().getValue());
    }

    @Override
    public String getInvalidErrorResourceSignatureErrorMessage(ServiceNode serviceNode,
            BLangFunction resourceFunction) {
        String errorMessage = "Invalid resource signature for the %s resource function in %s service. "
                + "Expected first parameter (required) type is nats:Message and the expected "
                + "second parameter (required) type is error";
        return String.format(errorMessage, resourceFunction.getName().getValue(), serviceNode.getName().getValue());
    }
}
