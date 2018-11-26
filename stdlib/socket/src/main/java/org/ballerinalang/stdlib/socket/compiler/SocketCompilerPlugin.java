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
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.List;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.stdlib.socket.SocketConstants.CONFIG_FIELD_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_CONNECT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_READ_READY;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Compiler plugin for validating Socket services.
 *
 * @since 0.985.0
 */
@SupportEndpointTypes(
        value = {@SupportEndpointTypes.EndpointType(orgName = "ballerina", packageName = "socket", name = "Listener"),
                 @SupportEndpointTypes.EndpointType(orgName = "ballerina", packageName = "socket", name = "Client")}
)
public class SocketCompilerPlugin extends AbstractCompilerPlugin {

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
                        String msg = String.format("'%s' must be a positive integer value", CONFIG_FIELD_PORT);
                        diagnosticLog.logDiagnostic(ERROR, endpointNode.getPosition(), msg);
                        break;
                    }
                }
            }
            if (!isPortPresent) {
                String msg = String.format("'%s' is a mandatory configuration field", CONFIG_FIELD_PORT);
                diagnosticLog.logDiagnostic(ERROR, endpointNode.getPosition(), msg);
            }
        }
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        List<BLangResource> resources = (List<BLangResource>) serviceNode.getResources();
        resources.forEach(res -> validate(serviceNode.getName().getValue(), res, this.diagnosticLog));
    }

    private void validate(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        switch (resource.getName().getValue()) {
            case RESOURCE_ON_CONNECT:
            case RESOURCE_ON_ACCEPT:
                validateOnAccept(serviceName, resource, diagnosticLog);
                break;
            case RESOURCE_ON_READ_READY:
                validateOnReadReady(serviceName, resource, diagnosticLog);
                break;
            case RESOURCE_ON_CLOSE:
                validateOnClose(serviceName, resource, diagnosticLog);
                break;
            case RESOURCE_ON_ERROR:
                validateOnError(serviceName, resource, diagnosticLog);
                break;
            default:
        }
    }

    private void validateOnError(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        final List<BLangSimpleVariable> readReadyParams = resource.getParameters();
        if (readReadyParams.size() != 2) {
            String msg = String.format("Invalid resource signature for %s in service %s. "
                            + "Parameters should be an 'endpoint' and 'error'",
                    resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = readReadyParams.get(0).type;
        if (caller.getKind().equals(OBJECT) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
        BType error = readReadyParams.get(1).getTypeNode().type;
        if (RECORD.equals(error.getKind()) && error instanceof BRecordType) {
            if (!"error".equals(error.tsymbol.toString())) {
                String msg = String.format("Invalid resource signature for %s in service %s. "
                        + "The second parameter should be an 'error'", resource.getName().getValue(), serviceName);
                diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            }
        }
    }

    private void validateOnReadReady(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        final List<BLangSimpleVariable> readReadyParams = resource.getParameters();
        if (readReadyParams.size() != 2) {
            String msg = String.format("Invalid resource signature for %s in service %s. "
                            + "Parameters should be an 'endpoint' and 'byte[]'",
                    resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = readReadyParams.get(0).type;
        if (caller.getKind().equals(OBJECT) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
        BType content = readReadyParams.get(1).getTypeNode().type;
        if (ARRAY.equals(content.getKind()) && content instanceof BArrayType) {
            if (!"byte".equals(((BArrayType) content).eType.tsymbol.toString())) {
                String msg = String
                        .format("Invalid resource signature for %s in service %s. Second parameter should be a byte[]",
                                resource.getName().getValue(), serviceName);
                diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            }
        }
    }

    private void validateOnClose(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        validateOnAccept(serviceName, resource, diagnosticLog);
    }

    private void validateOnAccept(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog) {
        final List<BLangSimpleVariable> acceptParams = resource.getParameters();
        if (acceptParams.size() != 1) {
            String msg = String.format("Invalid resource signature for %s in service %s. "
                            + "The parameter should be an 'endpoint'",
                    resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = acceptParams.get(0).type;
        if (caller.getKind().equals(OBJECT) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
    }

    private void validateEndpointCaller(String serviceName, BLangResource resource, DiagnosticLog diagnosticLog,
            BStructureType event) {
        String eventType = event.tsymbol.toString();
        if (!("ballerina/socket:Server".equals(eventType) || "ballerina/socket:Client".equals(eventType))) {
            String msg = String.format("Invalid resource signature for %s in service %s. "
                    + "The parameter should be an 'endpoint'", resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
        }
    }
}
