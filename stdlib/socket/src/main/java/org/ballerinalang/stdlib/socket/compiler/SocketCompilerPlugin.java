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
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

import java.util.List;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
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
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(packageName = "socket", name = "Listener"),
        paramTypes = { @SupportedResourceParamTypes.Type(packageName = "socket", name = "Client") }
)
public class SocketCompilerPlugin extends AbstractCompilerPlugin {

    private static final String INVALID_RESOURCE_SIGNATURE = "Invalid resource signature for %s in service %s. ";
    private DiagnosticLog diagnosticLog = null;
    private int resourceCount = 0;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.diagnosticLog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        resources.forEach(res -> validate(serviceNode.getName().getValue(), res, this.diagnosticLog));
        if (resourceCount != 4) {
            String errorMsg = "Service needs to have all 4 resources "
                    + "[(%s (Listener) or %s (CallBackService)), %s, %s, %s].";
            String msg = String.format(errorMsg, RESOURCE_ON_ACCEPT, RESOURCE_ON_CONNECT, RESOURCE_ON_READ_READY,
                    RESOURCE_ON_CLOSE, RESOURCE_ON_ERROR);
            diagnosticLog.logDiagnostic(ERROR, serviceNode.getPosition(), msg);
        }
    }

    private void validate(String serviceName, BLangFunction resource, DiagnosticLog diagnosticLog) {
        switch (resource.getName().getValue()) {
            case RESOURCE_ON_CONNECT:
            case RESOURCE_ON_ACCEPT:
                validateOnAccept(serviceName, resource, diagnosticLog);
                resourceCount++;
                break;
            case RESOURCE_ON_READ_READY:
                validateOnReadReady(serviceName, resource, diagnosticLog);
                resourceCount++;
                break;
            case RESOURCE_ON_CLOSE:
                validateOnClose(serviceName, resource, diagnosticLog);
                resourceCount++;
                break;
            case RESOURCE_ON_ERROR:
                validateOnError(serviceName, resource, diagnosticLog);
                resourceCount++;
                break;
            default:
        }
    }

    private void validateOnError(String serviceName, BLangFunction resource, DiagnosticLog diagnosticLog) {
        final List<BLangSimpleVariable> readReadyParams = resource.getParameters();
        if (readReadyParams.size() != 2) {
            String msg = String
                    .format(INVALID_RESOURCE_SIGNATURE + "Parameters should be a 'socket:Caller' and 'error'",
                            resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = readReadyParams.get(0).type;
        if (OBJECT.equals(caller.getKind()) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
        BType error = readReadyParams.get(1).getTypeNode().type;
        if (RECORD.equals(error.getKind()) && error instanceof BRecordType) {
            if (!"error".equals(error.tsymbol.toString())) {
                String msg = String.format(INVALID_RESOURCE_SIGNATURE + "The second parameter should be an 'error'",
                        resource.getName().getValue(), serviceName);
                diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            }
        }
    }

    private void validateOnReadReady(String serviceName, BLangFunction resource, DiagnosticLog diagnosticLog) {
        final List<BLangSimpleVariable> readReadyParams = resource.getParameters();
        if (readReadyParams.size() != 2) {
            String msg = String
                    .format(INVALID_RESOURCE_SIGNATURE + "Parameters should be a 'socket:Caller' and 'byte[]'",
                            resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = readReadyParams.get(0).type;
        if (OBJECT.equals(caller.getKind()) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
        BType content = readReadyParams.get(1).getTypeNode().type;
        if (ARRAY.equals(content.getKind()) && content instanceof BArrayType) {
            if (!"byte".equals(((BArrayType) content).eType.tsymbol.toString())) {
                String msg = String.format(INVALID_RESOURCE_SIGNATURE + "Second parameter should be a byte[]",
                        resource.getName().getValue(), serviceName);
                diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            }
        }
    }

    private void validateOnClose(String serviceName, BLangFunction resource, DiagnosticLog diagnosticLog) {
        validateOnAccept(serviceName, resource, diagnosticLog);
    }

    private void validateOnAccept(String serviceName, BLangFunction resource, DiagnosticLog diagnosticLog) {
        final List<BLangSimpleVariable> acceptParams = resource.getParameters();
        if (acceptParams.size() != 1) {
            String msg = String.format(INVALID_RESOURCE_SIGNATURE + "The parameter should be a 'socket:Caller'",
                    resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
            return;
        }
        BType caller = acceptParams.get(0).type;
        if (OBJECT.equals(caller.getKind()) && caller instanceof BStructureType) {
            validateEndpointCaller(serviceName, resource, diagnosticLog, (BStructureType) caller);
        }
    }

    private void validateEndpointCaller(String serviceName, BLangFunction resource, DiagnosticLog diagnosticLog,
            BStructureType event) {
        String eventType = event.tsymbol.toString();
        if (!("ballerina/socket:Listener".equals(eventType) || "ballerina/socket:Client".equals(eventType))) {
            String msg = String.format(INVALID_RESOURCE_SIGNATURE + "The parameter should be a 'socket:Caller'",
                    resource.getName().getValue(), serviceName);
            diagnosticLog.logDiagnostic(ERROR, resource.getPosition(), msg);
        }
    }
}
