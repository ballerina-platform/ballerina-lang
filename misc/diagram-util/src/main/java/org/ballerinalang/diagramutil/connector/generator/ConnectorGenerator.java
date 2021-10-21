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
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.diagramutil.connector.models.connector.Function;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.diagramutil.connector.models.connector.types.InclusionType;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.ModuleDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generate connector metadata using project syntax tree and semantic model.
 */
public class ConnectorGenerator {

    /**
     * Find connector implementations inside the project and generate metadata.
     *
     * @param project  Balerina Project
     * @param detailed Get connectors with metadata
     * @return
     * @throws IOException
     */
    public static List<Connector> getProjectConnectors(Project project, boolean detailed) throws IOException {
        List<Connector> connectorsList = new ArrayList<>();
        Package currentPackage = project.currentPackage();
        String packageName = currentPackage.packageName().toString();
        String version = currentPackage.packageVersion().toString();
        String orgName = currentPackage.packageOrg().toString();

        currentPackage.modules().forEach(module -> {
            module.documentIds().forEach(documentId -> {
                SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
                SemanticModel semanticModel = module.getCompilation().getSemanticModel();
                String moduleName = module.moduleName().toString();
                if (!syntaxTree.containsModulePart()) {
                    return;
                }

                ModulePartNode modulePartNode = syntaxTree.rootNode();
                for (Node node : modulePartNode.members()) {
                    if (node.kind() != SyntaxKind.CLASS_DEFINITION) {
                        return;
                    }

                    ClassDefinitionNode classDefinition = (ClassDefinitionNode) node;
                    if (classDefinition.visibilityQualifier().isPresent()
                            && classDefinition.visibilityQualifier().get()
                            .kind().equals(SyntaxKind.PUBLIC_KEYWORD) && Generator
                            .containsToken(classDefinition.classTypeQualifiers(), SyntaxKind.CLIENT_KEYWORD)) {
                        String connectorName = classDefinition.className().text();
                        String description = getDocFromMetadata(classDefinition.metadata());
                        Map<String, String> connectorAnnotation =
                                getDisplayAnnotationFromMetadataNode(classDefinition.metadata());
                        if (!connectorAnnotation.get("label").isEmpty()) {
                            connectorName = connectorAnnotation.get("label");
                        }

                        if (detailed) {
                            List<Function> functions = getConnectorFunctions(semanticModel, classDefinition);
                            connectorsList.add(new Connector(orgName, moduleName, packageName, version,
                                    connectorName, description, connectorAnnotation, functions));
                        } else {
                            connectorsList.add(new Connector(orgName, moduleName, packageName, version,
                                    connectorName, description, connectorAnnotation));
                        }
                    }
                }

            });
        });
        return connectorsList;
    }

    /**
     * Generate list of connector metadata inside a project.
     *
     * @param project Ballerina project
     * @return
     * @throws IOException
     */
    public static List<Connector> generateConnectorModel(Project project) throws IOException {
        List<Connector> connectors = new ArrayList<>();
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        for (Map.Entry<String, ModuleDoc> moduleDoc : moduleDocMap.entrySet()) {
            SemanticModel model = moduleDoc.getValue().semanticModel;
            for (Map.Entry<String, SyntaxTree> syntaxTreeMapEntry : moduleDoc.getValue().syntaxTreeMap.entrySet()) {
                connectors.addAll(getConnectorModelFromSyntaxTree(syntaxTreeMapEntry.getValue(), model,
                        project.currentPackage().packageOrg().value(),
                        project.currentPackage().packageName().value(),
                        project.currentPackage().packageVersion().toString(), moduleDoc.getKey()));
            }
        }
        return connectors;
    }

    /**
     * Generate connector models from syntax tree.
     *
     * @param syntaxTree    File syntax tree
     * @param semanticModel Project semantic model
     * @param orgName       Organization name
     * @param packageName   Package name
     * @param version       Version
     * @param moduleName    Module name
     * @return
     */
    public static List<Connector> getConnectorModelFromSyntaxTree(SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                                  String orgName, String packageName, String version,
                                                                  String moduleName) {
        List<Connector> connectorsList = new ArrayList<>();
        if (!syntaxTree.containsModulePart()) {
            return connectorsList;
        }
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
                    if (connectorAnnotation != null && !connectorAnnotation.get("label").isEmpty()) {
                        connectorName = connectorAnnotation.get("label");
                    }
                    List<Function> functions = getConnectorFunctions(semanticModel, classDefinition);
                    connectorsList.add(new Connector(orgName, moduleName, packageName, version,
                            connectorName, description, connectorAnnotation, functions));
                }
            }
        }
        return connectorsList;
    }

    private static List<Function> getConnectorFunctions(SemanticModel semanticModel,
                                                        ClassDefinitionNode classDefinition) {
        List<Function> functions = new ArrayList<>();
        for (Node member : classDefinition.members()) {

            boolean isRemoteFunction = member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION &&
                    (Generator.containsToken(((FunctionDefinitionNode) member).qualifierList(),
                            SyntaxKind.REMOTE_KEYWORD));
            boolean isPublicFunction = member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION &&
                    (Generator.containsToken(((FunctionDefinitionNode) member).qualifierList(),
                            SyntaxKind.PUBLIC_KEYWORD));

            if (isRemoteFunction || isPublicFunction) {
                FunctionDefinitionNode functionDefinition = (FunctionDefinitionNode) member;
                List<Type> parameters =
                        new ArrayList<>();

                String functionName = functionDefinition.functionName().text();
                Map<String, String> funcAnnotation =
                        getDisplayAnnotationFromMetadataNode(functionDefinition.metadata());

                FunctionSignatureNode functionSignature = functionDefinition.functionSignature();
                parameters.addAll(getFunctionParameters(functionSignature.parameters(),
                        functionDefinition.metadata(), semanticModel));

                Type returnParam = null;
                if (functionSignature.returnTypeDesc().isPresent()) {
                    returnParam = getReturnParameter(functionSignature.returnTypeDesc().get(),
                            functionDefinition.metadata(), semanticModel);
                }
                functions.add(new Function(functionName, parameters, returnParam, funcAnnotation,
                        isRemoteFunction, getDocFromMetadata(functionDefinition.metadata())));
            }
        }
        return functions;
    }

    /**
     * Generate function parameters.
     *
     * @param parameterNodes       Parameter node
     * @param optionalMetadataNode Metadata mode
     * @param semanticModel
     * @return
     */
    public static List<Type> getFunctionParameters(SeparatedNodeList<ParameterNode> parameterNodes,
                                                   Optional<MetadataNode> optionalMetadataNode,
                                                   SemanticModel semanticModel) {
        List<Type> parameters = new ArrayList<>();
        Type param;

        for (ParameterNode parameterNode : parameterNodes) {
            switch (parameterNode.kind()) {
                case REQUIRED_PARAM:
                    RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                    param = Type.fromSyntaxNode(requiredParameterNode.typeName(), semanticModel);
                    param.name = requiredParameterNode.paramName().isPresent() ?
                            requiredParameterNode.paramName().get().text() : null;
                    param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                            requiredParameterNode.annotations());
                    param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                    parameters.add(param);
                    break;
                case DEFAULTABLE_PARAM:
                    DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameterNode;
                    param = Type.fromSyntaxNode(defaultableParameter.typeName(), semanticModel);
                    if (param != null) {
                        param.name = defaultableParameter.paramName().isPresent() ?
                                defaultableParameter.paramName().get().text() : "";
                        param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(
                                defaultableParameter.annotations());
                        param.defaultValue = defaultableParameter.expression().toString();
                        param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                        parameters.add(param);
                    }
                    break;
                case INCLUDED_RECORD_PARAM:
                    IncludedRecordParameterNode includedRecord = (IncludedRecordParameterNode) parameterNode;
                    param = new InclusionType(Type.fromSyntaxNode(includedRecord.typeName(), semanticModel));
                    param.name = includedRecord.paramName().isPresent() ?
                            includedRecord.paramName().get().text() : "";
                    param.displayAnnotation = getDisplayAnnotationFromAnnotationsList(includedRecord.annotations());
                    param.documentation = getParameterDocFromMetadataList(param.name, optionalMetadataNode);
                    parameters.add(param);
                    break;
                default:
                    break;
            }
        }

        return parameters;
    }

    /**
     * Get return type parameters.
     *
     * @param returnTypeDescriptorNode Type descriptor node
     * @param optionalMetadataNode     Metadata node
     * @param semanticModel            Project semantic model
     * @return
     */
    public static Type getReturnParameter(ReturnTypeDescriptorNode returnTypeDescriptorNode,
                                          Optional<MetadataNode> optionalMetadataNode,
                                          SemanticModel semanticModel) {
        Type parameter = Type.fromSyntaxNode(returnTypeDescriptorNode.type(), semanticModel);
        parameter.displayAnnotation = getDisplayAnnotationFromAnnotationsList(returnTypeDescriptorNode.annotations());
        parameter.documentation = getParameterDocFromMetadataList("return", optionalMetadataNode);
        return parameter;
    }

    /**
     * Get display annotation.
     *
     * @param annotations Annotation node
     * @return
     */
    public static Map<String, String> getDisplayAnnotationFromAnnotationsList(NodeList<AnnotationNode> annotations) {
        Map<String, String> displayAnnotation = new HashMap<>();
        Optional<AnnotationNode> optDisplayAnnotation = annotations.stream()
                .filter(annotationNode -> annotationNode.annotReference().toString().equals("display ")).findAny();
        if (optDisplayAnnotation.isPresent() && optDisplayAnnotation.get().annotValue().isPresent()) {
            Map<String, String> finalDisplayAnnotation = displayAnnotation;
            optDisplayAnnotation.get().annotValue().get().fields().forEach(mappingFieldNode -> {
                if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                    SpecificFieldNode fieldNode = (SpecificFieldNode) mappingFieldNode;
                    if (fieldNode.fieldName().toString().equals("label") ||
                            fieldNode.fieldName().toString().equals("iconPath")) {
                        finalDisplayAnnotation.put(fieldNode.fieldName().toString(),
                                fieldNode.valueExpr().isPresent() ?
                                        fieldNode.valueExpr().get().toSourceCode().replace("\"", "") : "");
                    } else {
                        finalDisplayAnnotation.put(fieldNode.fieldName().toString(),
                                fieldNode.valueExpr().isPresent() ?
                                        fieldNode.valueExpr().get().toSourceCode() : "");
                    }
                }
            });
            displayAnnotation = finalDisplayAnnotation;
        }
        return displayAnnotation;
    }

    /**
     * Get display annotation form metadata node.
     *
     * @param optionalMetadataNode Metadata node
     * @return
     */
    public static Map<String, String> getDisplayAnnotationFromMetadataNode(
            Optional<MetadataNode> optionalMetadataNode) {
        Map<String, String> displayAnnotation = Collections.emptyMap();
        if (optionalMetadataNode.isPresent()) {
            displayAnnotation = getDisplayAnnotationFromAnnotationsList(optionalMetadataNode.get().annotations());
        }
        return displayAnnotation;
    }

    /**
     * Get doc info from metadata node.
     *
     * @param optionalMetadataNode Metadata node
     * @return
     */
    private static String getDocFromMetadata(Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return "";
        }

        StringBuilder doc = new StringBuilder();
        Optional<Node> docLines = optionalMetadataNode.get().documentationString();
        if (docLines.isEmpty()) {
            return doc.toString();
        }

        for (Node docLine : ((MarkdownDocumentationNode) docLines.get()).documentationLines()) {
            if (docLine.kind() == SyntaxKind.MARKDOWN_DOCUMENTATION_LINE) {
                String mdLine = !((MarkdownDocumentationLineNode) docLine).documentElements().isEmpty() ?
                        getDocLineString(((MarkdownDocumentationLineNode) docLine).documentElements()) : "\n";
                doc.append(mdLine);
            } else if (docLine.kind() == SyntaxKind.MARKDOWN_CODE_BLOCK) {
                doc.append(getDocCodeBlockString((MarkdownCodeBlockNode) docLine));
            } else {
                break;
            }
        }

        return doc.toString();
    }

    private static String getParameterDocFromMetadataList(String parameterName,
                                                          Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return "";
        }

        Optional<Node> docLines = optionalMetadataNode.get().documentationString();
        StringBuilder parameterDoc = new StringBuilder();
        if (docLines.isEmpty()) {
            return parameterDoc.toString();
        }

        boolean lookForMoreLines = false;
        for (Node docLine : ((MarkdownDocumentationNode) docLines.get()).documentationLines()) {
            if (docLine.kind() == SyntaxKind.MARKDOWN_PARAMETER_DOCUMENTATION_LINE) {
                if (((MarkdownParameterDocumentationLineNode) docLine).parameterName().text()
                        .equals(parameterName)) {
                    parameterDoc.append(getDocLineString(((MarkdownParameterDocumentationLineNode) docLine)
                            .documentElements()));
                    lookForMoreLines = true;
                } else {
                    lookForMoreLines = false;
                }
            } else if (lookForMoreLines && docLine.kind() == SyntaxKind.MARKDOWN_DOCUMENTATION_LINE) {
                parameterDoc.append(getDocLineString(((MarkdownDocumentationLineNode) docLine).documentElements()));
            }
        }

        return parameterDoc.toString();
    }

    private static String getDocLineString(NodeList<Node> documentElements) {
        if (documentElements.isEmpty()) {
            return "";
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
