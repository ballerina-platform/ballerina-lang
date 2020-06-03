/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.email.service.compiler;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.stdlib.email.util.EmailConstants;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

import java.util.List;

import static org.ballerinalang.stdlib.email.util.EmailConstants.ON_ERROR;
import static org.ballerinalang.stdlib.email.util.EmailConstants.ON_MESSAGE;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Compiler plugin for validating Email Listener.
 *
 * @since 1.3.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(packageName = EmailConstants.MODULE_NAME,
                name = EmailConstants.LISTENER),
        paramTypes = {
                @SupportedResourceParamTypes.Type(packageName = EmailConstants.MODULE_NAME,
                        name = EmailConstants.EMAIL),
                @SupportedResourceParamTypes.Type(packageName = EmailConstants.MODULE_NAME,
                        name = EmailConstants.ERROR)
        }
)
public class EmailListenerCompilerPlugin extends AbstractCompilerPlugin {

    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceData, List<AnnotationAttachmentNode> annotations) {
        List<BLangFunction> resources = (List<BLangFunction>) serviceData.getResources();
        resources.forEach(res -> validate(serviceData.getName().getValue(), res, this.dlog));
    }

    public void validate(String serviceName, BLangFunction resource, DiagnosticLog dlog) {
        final List<BLangSimpleVariable> parameters = resource.getParameters();
        switch (resource.getName().getValue()) {
            case ON_MESSAGE:
                String onMessageErrorMessage = "Invalid resource signature for %s in service %s. "
                        + "The parameter should be a " + EmailConstants.MODULE_NAME + ":" + EmailConstants.EMAIL +
                        " with no returns.";
                onMessageErrorMessage = String.format(onMessageErrorMessage, resource.getName().getValue(),
                        serviceName);
                if (parameters.size() != 1) {
                    dlog.logDiagnostic(ERROR, resource.getPosition(), onMessageErrorMessage);
                    return;
                }
                BType emailEvent = parameters.get(0).getTypeNode().type;
                if (emailEvent.getKind().equals(TypeKind.OBJECT)) {
                    if (emailEvent instanceof BStructureType) {
                        BStructureType event = (BStructureType) emailEvent;
                        if (!EmailConstants.MODULE_NAME.equals(event.tsymbol.pkgID.name.value) ||
                                !EmailConstants.EMAIL.equals(event.tsymbol.name.value)) {
                            dlog.logDiagnostic(ERROR, resource.getPosition(), onMessageErrorMessage);
                            return;
                        }
                    }
                }
                break;
            case ON_ERROR:
                String onErrorErrorMessage = "Invalid resource signature for %s in service %s. " +
                        "The parameter should be a " + EmailConstants.MODULE_NAME + ":" + EmailConstants.ERROR +
                        " with no returns.";
                onErrorErrorMessage = String.format(onErrorErrorMessage, resource.getName().getValue(), serviceName);
                if (parameters.size() != 1) {
                    dlog.logDiagnostic(ERROR, resource.getPosition(), onErrorErrorMessage);
                    return;
                }
                BType errorEvent = parameters.get(0).getTypeNode().type;
                if (errorEvent.getKind().equals(TypeKind.OBJECT)) {
                    if (errorEvent instanceof BStructureType) {
                        BStructureType event = (BStructureType) errorEvent;
                        if (!EmailConstants.MODULE_NAME.equals(event.tsymbol.pkgID.name.value) ||
                                !EmailConstants.EMAIL.equals(event.tsymbol.name.value)) {
                            dlog.logDiagnostic(ERROR, resource.getPosition(), onErrorErrorMessage);
                            return;
                        }
                    }
                }
                break;
            default:
                dlog.logDiagnostic(ERROR, resource.getPosition(), "Invalid resource name " +
                        resource.getName().getValue() + " in service " + serviceName);
        }
    }
}
