/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.stdlib.task.listener.compiler;

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.util.AbstractTransportCompilerPlugin;

import java.util.List;
import java.util.Objects;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.RESOURCE_ON_TRIGGER;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.STRUCT_NAME_LISTENER;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/*
 * TODO:
 * Compiler plugin will not hit as we do not have any parameters passed into the resource function.
 * Until this is fixed, cannot validate the resources at compile time.
 */
/**
 * Compiler plugin for validating Ballerina Task Service.
 *
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = PACKAGE_NAME, name = STRUCT_NAME_LISTENER
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(packageName = PACKAGE_NAME, name = STRUCT_NAME_LISTENER)
        }
)
public class TaskServiceCompilerPlugin extends AbstractTransportCompilerPlugin {

    private DiagnosticLog diagnosticLog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.diagnosticLog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        validateService(serviceNode);
    }

    private void validateService(ServiceNode serviceNode) {
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        for (FunctionNode resource : resources) {
            validateResource(resource);
        }
    }

    public void validateResource(FunctionNode resource) {
        String resourceName = resource.getName().getValue();
        Diagnostic.DiagnosticPosition position = resource.getPosition();

        if (RESOURCE_ON_TRIGGER.equals(resourceName) || RESOURCE_ON_ERROR.equals(resourceName)) {
            checkReturnType(resource, resourceName, position);
        } else {
            String message = "Invalid resource name: " + resourceName + " found. Expected: "
                    + RESOURCE_ON_TRIGGER + " or " + RESOURCE_ON_ERROR + ".";
            logError(message, position);
        }
    }

    private void checkReturnType(FunctionNode resource, String resourceName, Diagnostic.DiagnosticPosition position) {
        BLangFunction function = (BLangFunction) resource;
        if (Objects.nonNull(function.symbol.getReturnType())) {
            String message = "Invalid return type for the resource: " + resourceName
                    + ". Task resources do not return values.";
            logError(message, position);
        }
    }

    private void logError(String message, Diagnostic.DiagnosticPosition position) {
        diagnosticLog.logDiagnostic(ERROR, position, message);
    }
}
