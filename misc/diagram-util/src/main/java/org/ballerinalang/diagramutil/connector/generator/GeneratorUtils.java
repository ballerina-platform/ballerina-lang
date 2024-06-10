/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.generator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.diagramutil.connector.models.connector.types.InclusionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PathParamType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common utils used in project api generators.
 */
public class GeneratorUtils {

    public static String getDocCodeBlockString(MarkdownCodeBlockNode markdownCodeBlockNode) {
        StringBuilder doc = new StringBuilder();

        doc.append(markdownCodeBlockNode.startBacktick().toString());
        markdownCodeBlockNode.langAttribute().ifPresent(doc::append);

        for (MarkdownCodeLineNode codeLineNode : markdownCodeBlockNode.codeLines()) {
            doc.append(codeLineNode.codeDescription().toString());
        }

        doc.append(markdownCodeBlockNode.endBacktick().toString());
        return doc.toString();
    }

    public static String getDocFromMetadata(Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return "";
        }
        StringBuilder doc = new StringBuilder();
        MarkdownDocumentationNode docLines = optionalMetadataNode.get().documentationString().isPresent() ?
                (MarkdownDocumentationNode) optionalMetadataNode.get().documentationString().get() : null;
        if (docLines != null) {
            for (Node docLine : docLines.documentationLines()) {
                if (docLine instanceof MarkdownDocumentationLineNode markdownDocLine) {
                    doc.append(!markdownDocLine.documentElements().isEmpty() ?
                            getDocLineString(markdownDocLine.documentElements()) : "\n");
                } else if (docLine instanceof MarkdownCodeBlockNode markdownCodeBlock) {
                    doc.append(getDocCodeBlockString(markdownCodeBlock));
                } else {
                    break;
                }
            }
        }

        return doc.toString();
    }

    /**
     * Get return type parameters.
     *
     * @param returnTypeDescriptorNode Type descriptor node
     * @param optionalMetadataNode     Metadata node
     * @param semanticModel            Project semantic model
     * @return Type
     */
    public static Type getReturnParameter(ReturnTypeDescriptorNode returnTypeDescriptorNode,
                                          Optional<MetadataNode> optionalMetadataNode,
                                          SemanticModel semanticModel) {
        Optional<Type> optionalParam = Type.fromSyntaxNode(returnTypeDescriptorNode.type(), semanticModel);
        if (optionalParam.isPresent()) {
            Type parameter = optionalParam.get();
            parameter.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                    returnTypeDescriptorNode.annotations());
            parameter.documentation = getParameterDocFromMetadataList("return", optionalMetadataNode);
            return parameter;
        }
        return null;
    }

    public static String getDocLineString(NodeList<Node> documentElements) {
        if (documentElements.isEmpty()) {
            return null;
        }
        StringBuilder doc = new StringBuilder();
        for (Node docNode : documentElements) {
            doc.append(docNode.toString());
        }

        return doc.toString();
    }

    public static String getParameterDocFromMetadataList(String parameterName,
                                                         Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return null;
        }
        MarkdownDocumentationNode docLines = optionalMetadataNode.get().documentationString().isPresent() ?
                (MarkdownDocumentationNode) optionalMetadataNode.get().documentationString().get() : null;
        StringBuilder parameterDoc = new StringBuilder();
        if (docLines != null) {
            boolean lookForMoreLines = false;
            for (Node docLine : docLines.documentationLines()) {
                if (docLine instanceof MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
                    if (markdownParameterDocumentationLineNode.parameterName().text().equals(parameterName)) {
                        parameterDoc.append(getDocLineString(((MarkdownParameterDocumentationLineNode) docLine)
                                .documentElements()));
                        lookForMoreLines = true;
                    } else {
                        lookForMoreLines = false;
                    }
                } else if (lookForMoreLines && docLine instanceof MarkdownDocumentationLineNode markdownDocLine) {
                    parameterDoc.append(getDocLineString(markdownDocLine.documentElements()));
                }
            }
        }

        return parameterDoc.toString();
    }

    public static Map<String, String> getDisplayAnnotationFromAnnotationsList(NodeList<AnnotationNode> annotations) {
        Map<String, String> displayAnnotation = new HashMap<>();
        for (AnnotationNode annotation : annotations) {
            Node annotReference = annotation.annotReference();
            if (annotReference.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode simpleNameRef = (SimpleNameReferenceNode) annotReference;
                if (simpleNameRef.name().text().equals("display") && annotation.annotValue().isPresent()) {
                    for (MappingFieldNode fieldNode : annotation.annotValue().get().fields()) {
                        if (fieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                            SpecificFieldNode specificField = (SpecificFieldNode) fieldNode;
                            if (specificField.fieldName().kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                                String fieldName = ((IdentifierToken) specificField.fieldName()).text();
                                if (fieldName.equals("label") || fieldName.equals("iconPath")) {
                                    if (((SpecificFieldNode) fieldNode).valueExpr().isPresent()) {
                                        ExpressionNode valueNode =
                                                ((SpecificFieldNode) fieldNode).valueExpr().get();
                                        if (valueNode instanceof BasicLiteralNode basicLiteralNode) {
                                            String fieldValue = basicLiteralNode.literalToken().text();
                                            if (fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
                                                fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
                                            }
                                            displayAnnotation.put(fieldName, fieldValue);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return displayAnnotation;
    }

    public static List<PathParamType> getPathParameters(NodeList<Node> pathParamNodes) {
        List<PathParamType> parameters = new ArrayList<>();

        for (Node pathParamNode : pathParamNodes) {
            switch (pathParamNode.kind()) {
                case IDENTIFIER_TOKEN:
                    IdentifierToken token = (IdentifierToken) pathParamNode;
                    PathParamType identifierParam = new PathParamType(token.toString().trim(), "token");
                    parameters.add(identifierParam);
                    break;
                case RESOURCE_PATH_SEGMENT_PARAM:
                    ResourcePathParameterNode pathSegmentParam = (ResourcePathParameterNode) pathParamNode;
                    if (pathSegmentParam.paramName().isPresent()) {
                        PathParamType segmentParam = new PathParamType(pathSegmentParam.paramName().get().text(),
                                pathSegmentParam.typeDescriptor().toString().trim());
                        parameters.add(segmentParam);
                    }
                    break;
                case RESOURCE_PATH_REST_PARAM:
                    ResourcePathParameterNode pathRestParam = (ResourcePathParameterNode) pathParamNode;
                    if (pathRestParam.paramName().isPresent()) {
                        PathParamType restParam = new PathParamType(pathRestParam.paramName().get().text(),
                                pathRestParam.typeDescriptor().toString().trim(), true);
                        parameters.add(restParam);
                    }
                    break;
                default:
                    // Skip tokens
                    break;
            }
        }

        return parameters;
    }

    /**
     * Generate function parameters.
     *
     * @param parameterNodes       Parameter node
     * @param optionalMetadataNode Metadata mode
     * @param semanticModel        Semantic model
     * @return List of types
     */
    public static List<Type> getFunctionParameters(SeparatedNodeList<ParameterNode> parameterNodes,
                                                   Optional<MetadataNode> optionalMetadataNode,
                                                   SemanticModel semanticModel) {
        List<Type> parameters = new ArrayList<>();
        Optional<Type> optionalParam;
        Type param;

        for (ParameterNode parameterNode : parameterNodes) {
            switch (parameterNode.kind()) {
                case REQUIRED_PARAM:
                    RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                    optionalParam = Type.fromSyntaxNode(requiredParameterNode.typeName(), semanticModel);
                    if (optionalParam.isEmpty()) {
                        continue;
                    }
                    param = optionalParam.get();
                    param.name = requiredParameterNode.paramName().isPresent() ?
                            requiredParameterNode.paramName().get().text() : null;
                    param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                            requiredParameterNode.annotations());
                    param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                    parameters.add(param);
                    break;
                case DEFAULTABLE_PARAM:
                    DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameterNode;
                    optionalParam = Type.fromSyntaxNode(defaultableParameter.typeName(), semanticModel);
                    if (optionalParam.isEmpty()) {
                        continue;
                    }
                    param = optionalParam.get();

                    param.name = defaultableParameter.paramName().isPresent() ?
                            defaultableParameter.paramName().get().text() : "";
                    param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                            defaultableParameter.annotations());
                    param.defaultable = true;
                    param.defaultValue = defaultableParameter.expression().toString();
                    param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                    parameters.add(param);

                    break;
                case INCLUDED_RECORD_PARAM:
                    IncludedRecordParameterNode includedRecord = (IncludedRecordParameterNode) parameterNode;
                    optionalParam = Type.fromSyntaxNode(includedRecord.typeName(), semanticModel);
                    if (optionalParam.isEmpty()) {
                        continue;
                    }
                    param = new InclusionType(optionalParam.get());
                    param.name = includedRecord.paramName().isPresent() ?
                            includedRecord.paramName().get().text() : "";
                    param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(includedRecord.annotations());
                    param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                    parameters.add(param);
                    break;
                case REST_PARAM:
                    RestParameterNode restParameterNode = (RestParameterNode) parameterNode;
                    optionalParam = Type.fromSyntaxNode(restParameterNode.typeName(), semanticModel);
                    if (optionalParam.isEmpty()) {
                        continue;
                    }
                    param = optionalParam.get();
                    param.name = restParameterNode.paramName().isPresent() ?
                            restParameterNode.paramName().get().text() : null;
                    param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                            restParameterNode.annotations());
                    param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                    param.setRestType(true);
                    parameters.add(param);
                    break;
                default:
                    // Skip tokens
                    break;
            }
        }

        return parameters;
    }

    public static Map<String, String> getDisplayAnnotationFromMetadataNode(
            Optional<MetadataNode> optionalMetadataNode) {
        Map<String, String> displayAnnotation = new HashMap<>();
        if (optionalMetadataNode.isPresent()) {
            displayAnnotation =
                    GeneratorUtils.getDisplayAnnotationFromAnnotationsList(optionalMetadataNode.get().annotations());
        }
        return displayAnnotation;
    }
}
