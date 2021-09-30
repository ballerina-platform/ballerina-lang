/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Project;
import org.ballerinalang.central.client.model.connector.BalFunction;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.diagramutil.connector.models.connector.Function;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.diagramutil.connector.models.connector.types.InclusionType;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.ModuleDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generator used to generate the Connector model.
 */
public class ConnectorGenerator {

    public static List<Connector> generateConnectorModel(Project project) throws IOException {
        List<Connector> connectors = new ArrayList<>();
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        for (Map.Entry<String, ModuleDoc> moduleDoc : moduleDocMap.entrySet()) {
            SemanticModel model = moduleDoc.getValue().semanticModel;
            for (Map.Entry<String, SyntaxTree> syntaxTreeMapEntry : moduleDoc.getValue().syntaxTreeMap.entrySet()) {
                connectors.addAll(getConnectorModelFromSyntaxTree(syntaxTreeMapEntry.getValue(), model,
                        project.currentPackage().packageOrg().toString(),
                        project.currentPackage().packageName().toString(),
                        project.currentPackage().packageVersion().toString(), moduleDoc.getKey()));
            }
        }
        return connectors;
    }

    public static List<Connector> getConnectorModelFromSyntaxTree(SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                                  String orgName, String packageName, String version,
                                                                  String moduleName) {
        List<Connector> connectorsList = new ArrayList<>();
        if (syntaxTree.containsModulePart()) {
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            for (Node node : modulePartNode.members()) {
                if (node.kind() == SyntaxKind.CLASS_DEFINITION) {
                    ClassDefinitionNode classDefinition = (ClassDefinitionNode) node;
                    if (classDefinition.visibilityQualifier().isPresent() && classDefinition.visibilityQualifier().get()
                            .kind().equals(SyntaxKind.PUBLIC_KEYWORD) && Generator
                            .containsToken(classDefinition.classTypeQualifiers(), SyntaxKind.CLIENT_KEYWORD)) {
                        String connectorName = classDefinition.className().text();
                        String description = getDocFromMetadata(classDefinition.metadata());
                        Map<String, String> connectorAnnotation =
                                getDisplayAnnotationFromMetadataNode(classDefinition.metadata());
                        List<BalFunction> functions = new ArrayList<>();
                        for (Node member : classDefinition.members()) {
                            if (member instanceof FunctionDefinitionNode &&
                                    (Generator.containsToken(((FunctionDefinitionNode) member).qualifierList(),
                                            SyntaxKind.PUBLIC_KEYWORD) ||
                                            Generator.containsToken(((FunctionDefinitionNode) member).qualifierList(),
                                                    SyntaxKind.REMOTE_KEYWORD))) {
                                FunctionDefinitionNode functionDefinition = (FunctionDefinitionNode) member;
                                List<org.ballerinalang.central.client.model.connector.BalType> parameters =
                                        new ArrayList<>();

                                String functionName = functionDefinition.functionName().text();
                                Map<String, String> funcAnnotation =
                                        getDisplayAnnotationFromMetadataNode(functionDefinition.metadata());

                                FunctionSignatureNode functionSignature = functionDefinition.functionSignature();
                                parameters.addAll(getFunctionParameters(functionSignature.parameters(),
                                        functionDefinition.metadata(), semanticModel));

                                org.ballerinalang.central.client.model.connector.BalType returnParam = null;
                                if (functionSignature.returnTypeDesc().isPresent()) {
                                    returnParam = getReturnParameter(functionSignature.returnTypeDesc().get(),
                                            functionDefinition.metadata(), semanticModel);
                                }
                                functions.add(new Function(functionName, parameters, returnParam, funcAnnotation,
                                        Generator.containsToken(((FunctionDefinitionNode) member).qualifierList(),
                                                SyntaxKind.REMOTE_KEYWORD),
                                        getDocFromMetadata(functionDefinition.metadata())));
                            }
                        }
                        connectorsList.add(new Connector(orgName, moduleName, packageName, version, connectorName,
                                description, connectorAnnotation, functions));
                    }
                }
            }
        }
        return connectorsList;
    }

    public static List<Type> getFunctionParameters(SeparatedNodeList<ParameterNode> parameterNodes,
                                                   Optional<MetadataNode> optionalMetadataNode,
                                                   SemanticModel semanticModel) {
        List<Type> parameters = new ArrayList<>();

        for (ParameterNode parameterNode : parameterNodes) {
            if (parameterNode instanceof RequiredParameterNode) {
                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                Type param = Type.fromSyntaxNode(requiredParameterNode.typeName(), semanticModel);
                param.name = requiredParameterNode.paramName().isPresent() ?
                        requiredParameterNode.paramName().get().text() : null;
                param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                        requiredParameterNode.annotations());
                param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                parameters.add(param);
            } else if (parameterNode instanceof DefaultableParameterNode) {
                DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameterNode;
                Type param = Type.fromSyntaxNode(defaultableParameter.typeName(), semanticModel);
                param.name = defaultableParameter.paramName().isPresent() ?
                        defaultableParameter.paramName().get().text() : "";
                param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                        defaultableParameter.annotations());
                param.defaultValue = defaultableParameter.expression().toString();
                param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                parameters.add(param);
            } else if (parameterNode instanceof IncludedRecordParameterNode) {
                IncludedRecordParameterNode includedRecord = (IncludedRecordParameterNode) parameterNode;
                InclusionType param = new InclusionType(Type.fromSyntaxNode(includedRecord.typeName(), semanticModel));
                param.name = includedRecord.paramName().isPresent() ? includedRecord.paramName().get().text() : "";
                param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(includedRecord.annotations());
                param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                parameters.add(param);
            }
        }

        return parameters;
    }

    public static Type getReturnParameter(ReturnTypeDescriptorNode returnTypeDescriptorNode,
                                          Optional<MetadataNode> optionalMetadataNode,
                                          SemanticModel semanticModel) {
        Type parameter = Type.fromSyntaxNode(returnTypeDescriptorNode.type(), semanticModel);
        parameter.displayAnnotation = getDisplayAnnotationFromAnnotationsList(returnTypeDescriptorNode.annotations());
        parameter.documentation = getParameterDocFromMetadataList("return", optionalMetadataNode);
        return parameter;
    }

    public static Map<String, String> getDisplayAnnotationFromAnnotationsList(NodeList<AnnotationNode> annotations) {
        Map<String, String> displayAnnotation = new HashMap<>();
        Optional<AnnotationNode> optDisplayAnnotation = annotations.stream()
                .filter(annotationNode -> annotationNode.annotReference().toString().equals("display ")).findAny();
        if (optDisplayAnnotation.isPresent()) {
            if (optDisplayAnnotation.get().annotValue().isPresent()) {
                Map<String, String> finalDisplayAnnotation = displayAnnotation;
                optDisplayAnnotation.get().annotValue().get().fields().forEach(mappingFieldNode -> {
                    if (mappingFieldNode instanceof SpecificFieldNode) {
                        SpecificFieldNode fieldNode = (SpecificFieldNode) mappingFieldNode;
                        finalDisplayAnnotation.put(fieldNode.fieldName().toString(),
                                fieldNode.valueExpr().isPresent() ? fieldNode.valueExpr().get()
                                        .toSourceCode().replace("\"", "") : "");
                    }
                });
                displayAnnotation = finalDisplayAnnotation;
            }
        }
        return displayAnnotation;
    }

    public static Map<String, String> getDisplayAnnotationFromMetadataNode(
            Optional<MetadataNode> optionalMetadataNode) {
        Map<String, String> displayAnnotation = null;
        if (optionalMetadataNode.isPresent()) {
            displayAnnotation = getDisplayAnnotationFromAnnotationsList(optionalMetadataNode.get().annotations());
        }
        return displayAnnotation;
    }

    private static String getDocFromMetadata(Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return "";
        }
        StringBuilder doc = new StringBuilder();
        MarkdownDocumentationNode docLines = optionalMetadataNode.get().documentationString().isPresent() ?
                (MarkdownDocumentationNode) optionalMetadataNode.get().documentationString().get() : null;
        if (docLines != null) {
            for (Node docLine : docLines.documentationLines()) {
                if (docLine instanceof MarkdownDocumentationLineNode) {
                    doc.append(!((MarkdownDocumentationLineNode) docLine).documentElements().isEmpty() ?
                            getDocLineString(((MarkdownDocumentationLineNode) docLine).documentElements()) : "\n");
                } else if (docLine instanceof MarkdownCodeBlockNode) {
                    doc.append(getDocCodeBlockString((MarkdownCodeBlockNode) docLine));
                } else {
                    break;
                }
            }
        }

        return doc.toString();
    }

    private static String getParameterDocFromMetadataList(String parameterName,
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
                if (docLine instanceof MarkdownParameterDocumentationLineNode) {
                    if (((MarkdownParameterDocumentationLineNode) docLine).parameterName().text()
                            .equals(parameterName)) {
                        parameterDoc.append(getDocLineString(((MarkdownParameterDocumentationLineNode) docLine)
                                .documentElements()));
                        lookForMoreLines = true;
                    } else {
                        lookForMoreLines = false;
                    }
                } else if (lookForMoreLines && docLine instanceof MarkdownDocumentationLineNode) {
                    parameterDoc.append(getDocLineString(((MarkdownDocumentationLineNode) docLine).documentElements()));
                }
            }
        }

        return parameterDoc.toString();
    }

    private static String getDocLineString(NodeList<Node> documentElements) {
        if (documentElements.isEmpty()) {
            return null;
        }
        StringBuilder doc = new StringBuilder();
        for (Node docNode : documentElements) {
            doc.append(docNode.toString());
        }

        return doc.toString();
    }

    private static String getDocCodeBlockString(MarkdownCodeBlockNode markdownCodeBlockNode) {
        StringBuilder doc = new StringBuilder();

        doc.append(markdownCodeBlockNode.startBacktick().toString());
        markdownCodeBlockNode.langAttribute().ifPresent(langAttribute -> doc.append(langAttribute.toString()));

        for (MarkdownCodeLineNode codeLineNode : markdownCodeBlockNode.codeLines()) {
            doc.append(codeLineNode.codeDescription().toString());
        }

        doc.append(markdownCodeBlockNode.endBacktick().toString());
        return doc.toString();
    }

}
