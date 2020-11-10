/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.compiler.plugins;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;

import java.nio.file.Path;
import java.util.List;

/**
 * {@code AbstractAnnotationProcessor} provides convenient superclass
 * for {@link CompilerPlugin} implementations.
 *
 * @since 0.962.0
 */
public abstract class AbstractCompilerPlugin implements CompilerPlugin {

    public abstract void init(DiagnosticLog diagnosticLog);

    @Override
    public void process(PackageNode packageNode) {
    }

    @Override
    public void process(BLangTestablePackage packageNode) {
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(TypeDefinition typeDefinition, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(ClassDefinition classDefinition, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(SimpleVariableNode variableNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(AnnotationNode annotationNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void codeGenerated(PackageID packageID, Path binaryPath) {
    }

    @Override
    public void pluginExecutionStarted(PackageID packageID) {

    }

    @Override
    public void pluginExecutionCompleted(PackageID packageID) {

    }
}
