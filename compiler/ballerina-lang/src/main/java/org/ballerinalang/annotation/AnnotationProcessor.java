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
package org.ballerinalang.annotation;

import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.TransformerNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.util.List;

/**
 * The Ballerina annotation processor interface.
 * <p>
 * The Ballerina compiler invokes annotation processors as soon as it
 * guarantees that AST is free of compilation errors.
 *
 * @since 0.962.0
 */
public interface AnnotationProcessor {

    void init(DiagnosticLog diagnosticLog);

    void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations);

    void process(ResourceNode resourceNode, List<AnnotationAttachmentNode> annotations);

    void process(ConnectorNode connectorNode, List<AnnotationAttachmentNode> annotations);

    void process(ActionNode actionNode, List<AnnotationAttachmentNode> annotations);

    void process(StructNode serviceNode, List<AnnotationAttachmentNode> annotations);

    void process(EnumNode enumNode, List<AnnotationAttachmentNode> annotations);

    void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations);

    void process(VariableNode variableNode, List<AnnotationAttachmentNode> annotations);

    void process(AnnotationNode annotationNode, List<AnnotationAttachmentNode> annotations);

    void process(TransformerNode transformerNode, List<AnnotationAttachmentNode> annotations);
}
