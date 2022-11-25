/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.modelgenerator.core.generators;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.modelgenerator.core.model.ElementLocation;
import io.ballerina.modelgenerator.core.model.service.ServiceAnnotation;
import io.ballerina.tools.text.LineRange;

import java.util.UUID;

import static io.ballerina.modelgenerator.core.ProjectDesignConstants.DISPLAY_ANNOTATION;
import static io.ballerina.modelgenerator.core.ProjectDesignConstants.ID;
import static io.ballerina.modelgenerator.core.ProjectDesignConstants.LABEL;

/**
 * Provide utils functions for component model generating.
 *
 * @since 2201.3.1
 */
public class GeneratorUtils {

    public static ElementLocation getElementLocation(String filePath, LineRange lineRange) {

        ElementLocation.LinePosition startPosition = ElementLocation.LinePosition.from(
                lineRange.startLine().line(), lineRange.startLine().offset());
        ElementLocation.LinePosition endLinePosition = ElementLocation.LinePosition.from(
                lineRange.endLine().line(), lineRange.endLine().offset()
        );
        return ElementLocation.from(filePath, startPosition, endLinePosition);

    }

    public static ServiceAnnotation getServiceAnnotation(NodeList<AnnotationNode> annotationNodes, String filePath) {

        String id = UUID.randomUUID().toString();
        String label = "";
        ElementLocation elementLocation = null;
        for (AnnotationNode annotationNode : annotationNodes) {
            String annotationName = annotationNode.annotReference().toString().trim();
            if (!(annotationName.equals(DISPLAY_ANNOTATION) && annotationNode.annotValue().isPresent())) {
                continue;
            }
            SeparatedNodeList<MappingFieldNode> fields = annotationNode.annotValue().get().fields();
            elementLocation = getElementLocation(filePath, annotationNode.lineRange());
            for (MappingFieldNode mappingFieldNode : fields) {
                if (mappingFieldNode.kind() != SyntaxKind.SPECIFIC_FIELD) {
                    continue;
                }
                SpecificFieldNode specificFieldNode = (SpecificFieldNode) mappingFieldNode;
                String name = specificFieldNode.fieldName().toString().trim();
                if (specificFieldNode.valueExpr().isEmpty()) {
                    continue;
                }
                ExpressionNode expressionNode = specificFieldNode.valueExpr().get();
                String expressionNodeStr = expressionNode.toString().trim();
                String annotation = expressionNodeStr.replace("\"", "");
                if (name.equals(ID)) {
                    id = annotation;
                } else if (name.equals(LABEL)) {
                    label = annotation;
                }
            }
            break;
        }

        return new ServiceAnnotation(id, label, elementLocation);
    }
}
