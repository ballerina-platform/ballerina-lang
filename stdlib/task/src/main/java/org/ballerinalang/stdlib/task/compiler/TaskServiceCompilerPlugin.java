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
*/

package org.ballerinalang.stdlib.task.compiler;

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.util.AbstractTransportCompilerPlugin;

import java.util.List;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.OBJECT_NAME_LISTENER;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_NAME;

/*
 * TODO:
 *      Compiler plugin will not hit as we do not have any parameters passed into the resource function.
 *      Until this is fixed, cannot validate the resources at compile time.
 *      Issue: https://github.com/ballerina-platform/ballerina-lang/issues/14148
 */
/**
 * Compiler plugin for validating Ballerina Task Service.
 *
 * @since 0.995.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = PACKAGE_NAME, name = OBJECT_NAME_LISTENER
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(packageName = PACKAGE_NAME, name = OBJECT_NAME_LISTENER)
        }
)
public class TaskServiceCompilerPlugin extends AbstractTransportCompilerPlugin {

    //private DiagnosticLog diagnosticLog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        //this.diagnosticLog = diagnosticLog;
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

    }
}
