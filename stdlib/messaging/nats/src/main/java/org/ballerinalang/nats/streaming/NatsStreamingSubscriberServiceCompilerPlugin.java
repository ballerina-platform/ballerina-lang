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
package org.ballerinalang.nats.streaming;

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

import static org.ballerinalang.nats.Constants.NATS_STREAMING_SUBSCRIPTION_ANNOTATION;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Providing the compiler add-ons for NATS streaming consumer services.
 */
@SupportedResourceParamTypes(expectedListenerType = @SupportedResourceParamTypes.Type(packageName = "nats",
                                                                                      name = "StreamingListener"),
                             paramTypes = {
                                     @SupportedResourceParamTypes.Type(packageName = "nats",
                                                                       name = "StreamingMessage")
                             })
public class NatsStreamingSubscriberServiceCompilerPlugin extends AbstractNatsConsumerServiceCompilerPlugin {
    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    public void validateAnnotationPresence(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        if (annotations.stream().noneMatch(annotation -> annotation.getAnnotationName().getValue()
                .equals(NATS_STREAMING_SUBSCRIPTION_ANNOTATION))) {
            logDiagnostic(ERROR, serviceNode.getPosition(), "nats:" + NATS_STREAMING_SUBSCRIPTION_ANNOTATION
                    + " annotation is required to be declared in the subscription service");
        }
    }

    public void validateMessageParameter(BLangSimpleVariable firstParameter, BLangFunction resourceFunction,
            String errorMessage) {
        BType firstParamType = firstParameter.getTypeNode().type;
        if (firstParamType.tag != TypeTags.OBJECT) {
            logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
        } else {
            BObjectType objectType = (BObjectType) firstParamType;
            if (!objectType.tsymbol.getName().getValue().equals(Constants.NATS_STREAMING_MESSAGE_OBJ_NAME)) {
                logDiagnostic(ERROR, resourceFunction.getPosition(), errorMessage);
            }
        }
    }

    @Override
    public String getInvalidMessageResourceSignatureErrorMessage(ServiceNode serviceNode,
            BLangFunction resourceFunction) {
        String errorMessage =  "Invalid resource signature for the %s resource function in %s service. "
                + "Expected first parameter (required) type is nats:StreamingMessage and the expected "
                + "second paramter (optional) type is "
                + "byte[] | boolean | string | int | float | decimal | xml | json | record {}";
        return String.format(errorMessage, resourceFunction.getName().getValue(), serviceNode.getName().getValue());
    }

    @Override
    public String getInvalidErrorResourceSignatureErrorMessage(ServiceNode serviceNode,
            BLangFunction resourceFunction) {
        String errorMessage = "Invalid resource signature for the %s resource function in %s service. "
                + "Expected first parameter (required) type is nats:StreamingMessage and the expected "
                + "second paramter (required) type is error";
        return String.format(errorMessage, resourceFunction.getName().getValue(), serviceNode.getName().getValue());
    }

    public void logDiagnostic(Diagnostic.Kind diagnosticKind, Diagnostic.DiagnosticPosition diagnosticPosition,
            String errorMessage) {
        dlog.logDiagnostic(ERROR, diagnosticPosition, errorMessage);
    }
}
