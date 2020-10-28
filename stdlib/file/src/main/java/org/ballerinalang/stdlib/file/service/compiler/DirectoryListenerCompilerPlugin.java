/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.file.service.compiler;

import io.ballerina.runtime.api.types.StructureType;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

import java.util.List;

import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.FILE_SYSTEM_EVENT;
import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.RESOURCE_NAME_ON_CREATE;
import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.RESOURCE_NAME_ON_DELETE;
import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.RESOURCE_NAME_ON_MODIFY;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Compiler plugin for validating Directory Listener.
 *
 * @since 0.970.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(packageName = "file", name = "Listener"),
        paramTypes = { @SupportedResourceParamTypes.Type(packageName = "file", name = "FileEvent") }
)
public class DirectoryListenerCompilerPlugin extends AbstractCompilerPlugin {

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
        switch (resource.getName().getValue()) {
            case RESOURCE_NAME_ON_CREATE:
            case RESOURCE_NAME_ON_DELETE:
            case RESOURCE_NAME_ON_MODIFY:
                final List<BLangSimpleVariable> parameters = resource.getParameters();
                String msg = "Invalid resource signature for %s in service %s. "
                        + "The parameter should be a file:FileEvent with no returns.";
                msg = String.format(msg, resource.getName().getValue(), serviceName);
                if (parameters.size() != 1) {
                    dlog.logDiagnostic(ERROR, resource.getPosition(), msg);
                    return;
                }
                BType fileEvent = parameters.get(0).getTypeNode().type;
                if (fileEvent.getKind().equals(TypeKind.OBJECT)) {
                    if (fileEvent instanceof StructureType) {
                        StructureType event = (StructureType) fileEvent;
                        if (!"file".equals(event.getPkg().getName()) || !FILE_SYSTEM_EVENT
                                .equals(event.getName())) {
                            dlog.logDiagnostic(ERROR, resource.getPosition(), msg);
                            return;
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
