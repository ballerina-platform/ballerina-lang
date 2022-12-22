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

package io.ballerina.architecturemodelgenerator.generators;

import io.ballerina.architecturemodelgenerator.model.ElementLocation;
import io.ballerina.architecturemodelgenerator.model.service.ServiceAnnotation;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.values.ConstantValue;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.CLIENT;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.DISPLAY_ANNOTATION;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.ID;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.LABEL;

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

    public static ServiceAnnotation getServiceAnnotation(Annotatable annotableSymbol, String filePath) {

        String id = null;
        String label = "";
        ElementLocation elementLocation = null;

        List<AnnotationSymbol> annotSymbols = annotableSymbol.annotations();
        List<AnnotationAttachmentSymbol> annotAttachmentSymbols = annotableSymbol.annotAttachments();
        if (annotSymbols.size() == annotAttachmentSymbols.size()) {
            for (int i = 0; i < annotSymbols.size(); i++) {
                AnnotationSymbol annotSymbol = annotSymbols.get(i);
                AnnotationAttachmentSymbol annotAttachmentSymbol = annotAttachmentSymbols.get(i);
                String annotName = annotSymbol.getName().orElse("");
                elementLocation = annotSymbol.getLocation().isPresent() ?
                        getElementLocation(filePath, annotSymbol.getLocation().get().lineRange()) : null;
                if (!annotName.equals(DISPLAY_ANNOTATION) || annotAttachmentSymbol.attachmentValue().isEmpty() ||
                        !(annotAttachmentSymbol.attachmentValue().get().value() instanceof LinkedHashMap) ||
                        !annotAttachmentSymbol.isConstAnnotation()) {
                    continue;
                }
                LinkedHashMap attachmentValue = (LinkedHashMap) annotAttachmentSymbol.attachmentValue().get().value();
                if (attachmentValue.containsKey(ID)) {
                    id = ((ConstantValue) attachmentValue.get(ID)).value().toString();
                }
                if (attachmentValue.containsKey(LABEL)) {
                    label = ((ConstantValue) attachmentValue.get(LABEL)).value().toString();
                }
                break;
            }
        }

        return new ServiceAnnotation(id, label, elementLocation);
    }

    public static String getClientModuleName(Node clientNode, SemanticModel semanticModel) {

        String clientModuleName = null;
        Optional<TypeSymbol> clientTypeSymbol = semanticModel.typeOf(clientNode);
        if (clientTypeSymbol.isPresent()) {
            clientModuleName = clientTypeSymbol.get().signature().trim().replace(CLIENT, "");
        }

        return clientModuleName;
    }

    public static String getClientModuleName(TypeSymbol typeSymbol) {
        String clientModuleName = typeSymbol.signature().trim().replace(CLIENT, "");
        if (typeSymbol.getModule().isPresent()) {
            clientModuleName = typeSymbol.getModule().get().id().toString();;
        }
        return clientModuleName;
    }

    public static NonTerminalNode findNode(SyntaxTree syntaxTree, LineRange lineRange) {
        if (lineRange == null) {
            return null;
        }

        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int end = textDocument.textPositionFrom(lineRange.endLine());
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, end - start), true);
    }
}
