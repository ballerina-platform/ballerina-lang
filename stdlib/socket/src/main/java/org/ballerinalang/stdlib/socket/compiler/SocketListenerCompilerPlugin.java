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

package org.ballerinalang.stdlib.socket.compiler;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportEndpointTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.List;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.stdlib.socket.SocketConstants.CONFIG_FIELD_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_READ_READY;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Compiler plugin for validating Socket listener.
 *
 * @since 0.983.0
 */
@SupportEndpointTypes(
        value = {@SupportEndpointTypes.EndpointType(orgName = "ballerina", packageName = "socket", name = "Listener")}
)
public class SocketListenerCompilerPlugin extends AbstractCompilerPlugin {

    private DiagnosticLog diagnosticLog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.diagnosticLog = diagnosticLog;
    }

    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
        final ExpressionNode configurationExpression = endpointNode.getConfigurationExpression();
        if (NodeKind.RECORD_LITERAL_EXPR.equals(configurationExpression.getKind())) {
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) configurationExpression;
            boolean isPortPresent = false;
            for (BLangRecordLiteral.BLangRecordKeyValue config : recordLiteral.getKeyValuePairs()) {
                final String key = ((BLangSimpleVarRef) config.getKey()).variableName.value;
                if (CONFIG_FIELD_PORT.equals(key)) {
                    isPortPresent = true;
                    final Object value = ((BLangLiteral) config.getValue()).getValue();
                    Long port = (Long) value;
                    if (port <= 0) {
                        String msg = "'" + CONFIG_FIELD_PORT + "' must be a positive integer value";
                        diagnosticLog.logDiagnostic(ERROR, endpointNode.getPosition(), msg);
                        break;
                    }
                }
            }
            if (!isPortPresent) {
                String msg = "'" + CONFIG_FIELD_PORT + "' is a mandatory configuration field";
                diagnosticLog.logDiagnostic(ERROR, endpointNode.getPosition(), msg);
            }
        }
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        List<BLangResource> resources = (List<BLangResource>) serviceNode.getResources();
        resources.forEach(res -> validate(serviceNode.getName().getValue(), res, this.diagnosticLog));
    }

    public void validate(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        switch (resource.getName().getValue()) {
            case LISTENER_RESOURCE_ON_ACCEPT:
                validateOnAccept(serviceName, resource, diagnosticLog);
                break;
            case LISTENER_RESOURCE_ON_READ_READY:
                validateOnReadReady(serviceName, resource, diagnosticLog);
                break;
            case LISTENER_RESOURCE_ON_CLOSE:
            case LISTENER_RESOURCE_ON_ERROR:
                validateOnErrorOrClose(serviceName, resource, diagnosticLog);
                break;
            default:
        }
    }

    private void validateOnReadReady(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        final List<BLangVariable> readReadyParams = resource.getParameters();
        if (readReadyParams.size() != 3) {
            String msg =
                    "Invalid resource signature for " + resource.getName().getValue() + " in service " + serviceName
                            + ". Parameters should be an 'endpoint', 'socket:TCPSocketMeta' and 'byte[]'";
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = readReadyParams.get(0).type;
        if (caller.getKind().equals(OBJECT) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
        BType metaInfo = readReadyParams.get(1).getTypeNode().type;
        if (OBJECT.equals(metaInfo.getKind()) && metaInfo instanceof BStructureType) {
            validateMetaInfo(serviceName, resource, diagnosticLog, (BStructureType) metaInfo, "Second");
        }
        BType content = readReadyParams.get(2).getTypeNode().type;
        if (ARRAY.equals(content.getKind()) && content instanceof BArrayType) {
            if (!"byte".equals(((BArrayType) content).eType.tsymbol.toString())) {
                String msg =
                        "Invalid resource signature for " + resource.getName().getValue() + " in service " + serviceName
                                + ". Third parameter should be a byte[]";
                diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            }
        }
    }

    private void validateOnErrorOrClose(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        final List<BLangVariable> parameters = resource.getParameters();
        if (parameters.size() != 1) {
            String msg =
                    "Invalid resource signature for " + resource.getName().getValue() + " in service " + serviceName
                            + ". The parameter should be a 'socket:TCPSocketMeta'";
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType metaInfo = parameters.get(0).getTypeNode().type;
        if (OBJECT.equals(metaInfo.getKind()) && metaInfo instanceof BStructureType) {
            validateMetaInfo(serviceName, resource, diagnosticLog, (BStructureType) metaInfo, "First");
        }
    }

    private void validateOnAccept(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        final List<BLangVariable> acceptParams = resource.getParameters();
        if (acceptParams.size() != 2) {
            String msg =
                    "Invalid resource signature for " + resource.getName().getValue() + " in service " + serviceName
                            + ". Parameters should be an 'endpoint'  and 'socket:TCPSocketMeta'";
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = acceptParams.get(0).type;
        if (caller.getKind().equals(OBJECT) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
        BType metaInfo = acceptParams.get(1).getTypeNode().type;
        if (OBJECT.equals(metaInfo.getKind()) && metaInfo instanceof BStructureType) {
            validateMetaInfo(serviceName, resource, diagnosticLog, (BStructureType) metaInfo, "Second");
        }
    }

    private void validateMetaInfo(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog,
            BStructureType event, String position) {
        if (!"socket".equals(event.tsymbol.pkgID.name.value) || !"TCPSocketMeta".equals(event.tsymbol.name.value)) {
            String msg =
                    "Invalid resource signature for " + resource.getName().getValue() + " in service " + serviceName
                            + ". " + position + " parameter should be a 'socket:TCPSocketMeta'";
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
        }
    }

    private void validateEndpointCaller(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog,
            BStructureType event) {
        if (!"ballerina/socket:Listener".equals(event.tsymbol.toString())) {
            String msg =
                    "Invalid resource signature for " + resource.getName().getValue() + " in service " + serviceName
                            + ". First parameter should be an 'endpoint'";
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
        }
    }
}
