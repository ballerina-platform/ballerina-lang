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
package io.ballerina.test.compiler.plugins;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.ConnectorNode;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A test implementation of the {@link org.ballerinalang.compiler.plugins.CompilerPlugin} interface.
 */
@SupportedAnnotationPackages(
        value = {"images.jpeg", "images.gif", "images.png"}
)
public class ABCCompilerPlugin extends AbstractCompilerPlugin {

    static Map<TestEvent.Kind, Set<TestEvent>> testEventMap = new HashMap<>();

    @Override
    public void init(DiagnosticLog diagnosticLog) {
    }

    @Override
    public void process(PackageNode packageNode) {
        addEvent(TestEvent.Kind.PKG_NODE, packageNode.toString(), 1);
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.SERVICE_ANN, serviceNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(ResourceNode resourceNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.RESOURCE_ANN, resourceNode.getName().getValue(), annotations.size());

    }

    @Override
    public void process(ConnectorNode connectorNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.CONNECTOR_ANN, connectorNode.getName().getValue(), annotations.size());

    }

    @Override
    public void process(ActionNode actionNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.ACTION_ANN, actionNode.getName().getValue(), annotations.size());

    }

    @Override
    public void process(StructNode structNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.STRUCT_ANN, structNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(ObjectNode objectNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.STRUCT_ANN, objectNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(RecordNode recordNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.STRUCT_ANN, recordNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(EnumNode enumNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.ENUM_ANN, enumNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.FUNC_ANN, functionNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(VariableNode variableNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.VARIAVLE_ANN, variableNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(AnnotationNode annotationNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.ANNOTATION_ANN, annotationNode.getName().getValue(), annotations.size());
    }

    @Override
    public void process(TransformerNode transformerNode, List<AnnotationAttachmentNode> annotations) {
        addEvent(TestEvent.Kind.TRANSFORM_ANN, transformerNode.getName().getValue(), annotations.size());

    }

    @Override
    public void codeGenerated(Path binaryPath) {
        addEvent(TestEvent.Kind.CODE_GEN, binaryPath.toString(), 1);

    }

    private void addEvent(TestEvent.Kind kind, String nodeName, int noOfAnnotations) {
        addEvent(new TestEvent(kind, nodeName, noOfAnnotations));
    }

    private void addEvent(TestEvent event) {
        Set<TestEvent> eventSet = testEventMap.computeIfAbsent(event.kind, kind -> new HashSet<>());
        eventSet.add(event);
    }
}
