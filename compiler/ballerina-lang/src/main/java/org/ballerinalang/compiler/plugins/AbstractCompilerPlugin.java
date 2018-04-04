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

import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ObjectNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.RecordNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.TransformerNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

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
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(ResourceNode resourceNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(ConnectorNode connectorNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(ActionNode actionNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(StructNode serviceNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(ObjectNode objectNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(RecordNode recordNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(EnumNode enumNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(VariableNode variableNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(AnnotationNode annotationNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(TransformerNode transformerNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
    }

    @Override
    public void codeGenerated(Path binaryPath) {
    }
}
