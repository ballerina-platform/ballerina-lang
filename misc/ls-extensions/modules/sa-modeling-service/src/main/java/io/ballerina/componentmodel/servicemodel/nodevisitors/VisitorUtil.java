/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.concurrent.atomic.AtomicReference;

import static io.ballerina.componentmodel.ComponentModelingConstants.DISPLAY_ANNOTATION;
import static io.ballerina.componentmodel.ComponentModelingConstants.ID;

/**
 * Includes util functions to generate.
 */
public class VisitorUtil {
    public static String getId(NodeList<AnnotationNode> annotationNodes) {
        AtomicReference<String> id = new AtomicReference<>("");
        for (AnnotationNode annotationNode : annotationNodes)  {
            String annotationName = annotationNode.annotReference().toString().trim();
            if (annotationName.equals(DISPLAY_ANNOTATION)) {
                if (annotationNode.annotValue().isPresent()) {
                    SeparatedNodeList<MappingFieldNode> fields = annotationNode.annotValue().get().fields();
                    fields.forEach(mappingFieldNode -> {
                        if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                            SpecificFieldNode specificFieldNode = (SpecificFieldNode) mappingFieldNode;
                            String name = specificFieldNode.fieldName().toString().trim();
                            if (specificFieldNode.valueExpr().isPresent()) {
                                if (name.equals(ID)) {
                                    ExpressionNode expressionNode = specificFieldNode.valueExpr().get();
                                    String expressionNodeStr = expressionNode.toString().trim();
                                    String annotationId = expressionNodeStr.replace("\"", "");
                                    id.set(annotationId);
                                }
                            }
                        }
                    });
                }
            }
        }
        return id.get();
    }
}
