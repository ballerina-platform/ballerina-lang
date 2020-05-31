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
 */
package org.ballerinalang.net.grpc.proto;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.net.grpc.config.ServiceConfiguration;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.grpc.GrpcConstants.CALLER_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ON_COMPLETE_RESOURCE;
import static org.ballerinalang.net.grpc.GrpcConstants.ON_ERROR_RESOURCE;
import static org.ballerinalang.net.grpc.GrpcConstants.ON_MESSAGE_RESOURCE;
import static org.ballerinalang.net.grpc.GrpcConstants.ON_OPEN_RESOURCE;
import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.ANN_SERVICE_CONFIG;
import static org.ballerinalang.net.grpc.proto.ServiceProtoUtils.getServiceConfiguration;

/**
 * A utility class for validating an gRPC service signature at compile time.
 *
 * @since 0.970.0
 */
public class ServiceDefinitionValidator {

    public static final int COMPULSORY_PARAM_COUNT = 1;

    /**
     * Validate gRPC service instance.
     *
     * @param serviceNode gRPC service node.
     * @param dlog Diagnostic Logs
     * @return true, if service definition is valid. false otherwise.
     */
    public static boolean validate (ServiceNode serviceNode, DiagnosticLog dlog) {
        return validateAnnotation(serviceNode, dlog) && validateResource(serviceNode, dlog);
    }

    private static boolean validateAnnotation(ServiceNode serviceNode, DiagnosticLog dlog) {
        List<AnnotationAttachmentNode> annotations =
                (List<AnnotationAttachmentNode>) serviceNode.getAnnotationAttachments();
        List<BLangRecordLiteral.BLangRecordKeyValueField> annVals = new ArrayList<>();
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(ANN_SERVICE_CONFIG)) {
                if (annotation.getExpression() != null) {
                    for (RecordLiteralNode.RecordField field :
                            ((BLangRecordLiteral) annotation.getExpression()).fields) {
                        annVals.add((BLangRecordLiteral.BLangRecordKeyValueField) field);
                    }

                    count++;
                }
            }
        }
        if (count > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                    "There cannot be more than one service annotations");
            return false;
        } else if (count == 1) {
            boolean isNameExists = false;
            boolean clientStreaming = false;
            for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : annVals) {
                switch (((BLangSimpleVarRef) (keyValue.key).expr).variableName.getValue()) {
                    case "name":
                        isNameExists = true;
                        break;
                    case "clientStreaming":
                        clientStreaming = true;
                        break;
                    default:
                        break;
                }
            }
            return isNameExists && clientStreaming;
        }
        return true;
    }

    private static boolean validateResource(ServiceNode serviceNode, DiagnosticLog dlog) {
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        ServiceConfiguration serviceConfig = getServiceConfiguration(serviceNode);
        if (serviceConfig.getRpcEndpoint() != null && (serviceConfig.isClientStreaming())) {
            if (resources.size() != 4) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                        "There should be four resources defined in client/bidirectional streaming services");
                return false;
            }
            boolean onMessageExists = false;
            boolean onOpenExists = false;
            boolean onErrorExists = false;
            boolean onCompleteExists = false;
            for (BLangFunction resourceNode : resources) {
                switch (resourceNode.getName().getValue()) {
                    case ON_OPEN_RESOURCE:
                        onOpenExists = true;
                        break;
                    case ON_MESSAGE_RESOURCE:
                        onMessageExists = true;
                        break;
                    case ON_ERROR_RESOURCE:
                        onErrorExists = true;
                        break;
                    case ON_COMPLETE_RESOURCE:
                        onCompleteExists = true;
                        break;
                    default:
                        break;
                }
                if (!validateResourceSignature(resourceNode, dlog, resourceNode.pos)) {
                    return false;
                }
            }
            if (onMessageExists && onOpenExists && onErrorExists && onCompleteExists) {
                return true;
            } else {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                        "One or more resources(onOpen/onMessage/onError/onComplete) is not implemented in " +
                                "client/bidirectional streaming service");
                return false;
            }
        } else {
            for (BLangFunction resourceNode : resources) {
                if (!validateResourceSignature(resourceNode, dlog, resourceNode.pos)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean validateResourceSignature(BLangFunction resourceNode, DiagnosticLog dlog,
                                                     DiagnosticPos pos) {
        List<BLangSimpleVariable> signatureParams = resourceNode.getParameters();
        final int nParams = signatureParams.size();
        if (nParams < COMPULSORY_PARAM_COUNT) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "resource signature parameter count should be >= 1");
            return false;
        }
        if (!isValidResourceParam(signatureParams.get(0), CALLER_ENDPOINT_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "first parameter should be of type " + CALLER_ENDPOINT_TYPE);
            return false;
        }
        return true;
    }

    private static boolean isValidResourceParam(BLangSimpleVariable param, String expectedType) {
        return expectedType.equals(param.type.toString());
    }
}
