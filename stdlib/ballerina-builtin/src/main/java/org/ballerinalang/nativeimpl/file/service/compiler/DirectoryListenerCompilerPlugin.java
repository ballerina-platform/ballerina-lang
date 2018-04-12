/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.file.service.compiler;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportEndpointTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.List;

import static org.ballerinalang.nativeimpl.file.service.DirectoryListenerConstants.ANNOTATION_PATH;
import static org.ballerinalang.nativeimpl.file.service.DirectoryListenerConstants.FILE_SYSTEM_EVENT;
import static org.ballerinalang.nativeimpl.file.service.DirectoryListenerConstants.RESOURCE_NAME_ON_CREATE;
import static org.ballerinalang.nativeimpl.file.service.DirectoryListenerConstants.RESOURCE_NAME_ON_DELETE;
import static org.ballerinalang.nativeimpl.file.service.DirectoryListenerConstants.RESOURCE_NAME_ON_MODIFY;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Compiler plugin for validating Directory Listener.
 *
 * @since 0.970.0
 */
@SupportEndpointTypes(
        value = {@SupportEndpointTypes.EndpointType(orgName = "ballerina", packageName = "file", name = "Listener")}
)
public class DirectoryListenerCompilerPlugin extends AbstractCompilerPlugin {

    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
        final ExpressionNode configurationExpression = endpointNode.getConfigurationExpression();
        if (NodeKind.RECORD_LITERAL_EXPR.equals(configurationExpression.getKind())) {
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) configurationExpression;
            boolean valid = false;
            for (BLangRecordLiteral.BLangRecordKeyValue config : recordLiteral.getKeyValuePairs()) {
                final String key = ((BLangSimpleVarRef) config.getKey()).variableName.value;
                if (ANNOTATION_PATH.equals(key)) {
                    final Object value = ((BLangLiteral) config.getValue()).getValue();
                    if (value != null && !value.toString().isEmpty()) {
                        valid = true;
                        break;
                    }
                }
            }
            if (!valid) {
                String msg =
                        "'" + ANNOTATION_PATH + "' field either empty or not available in Directory Listener endpoint.";
                dlog.logDiagnostic(ERROR, endpointNode.getPosition(), msg);
            }
        }
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        List<BLangResource> resources = (List<BLangResource>) serviceNode.getResources();
        resources.forEach(res -> validate(serviceNode.getName().getValue(), res, this.dlog));
    }

    public static void validate(String serviceName, BLangResource resource, DiagnosticLog dlog) {
        switch (resource.getName().getValue()) {
            case RESOURCE_NAME_ON_CREATE:
            case RESOURCE_NAME_ON_DELETE:
            case RESOURCE_NAME_ON_MODIFY:
                final List<BLangVariable> parameters = resource.getParameters();
                String msg =
                        "Invalid resource signature for " + resource.getName().getValue() + " in service " + serviceName
                                + ". The parameter should be a file:FileEvent";
                if (parameters.size() != 1) {
                    dlog.logDiagnostic(ERROR, resource.getPosition(), msg);
                    return;
                }
                BType fileEvent = parameters.get(0).getTypeNode().type;
                if (fileEvent.getKind().equals(TypeKind.STRUCT)) {
                    if (fileEvent instanceof BStructType) {
                        BStructType event = (BStructType) fileEvent;
                        if (!"file".equals(event.tsymbol.pkgID.name.value) || !FILE_SYSTEM_EVENT
                                .equals(event.tsymbol.name.value)) {
                            dlog.logDiagnostic(ERROR, resource.getPosition(), msg);
                        }
                    }
                }
                break;
            default:
                dlog.logDiagnostic(ERROR, resource.getPosition(),
                        "Invalid resource name " + resource.getName().getValue() + " in service " + serviceName);
        }
    }
}
