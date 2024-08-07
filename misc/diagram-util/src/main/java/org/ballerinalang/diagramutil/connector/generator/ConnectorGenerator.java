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
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.diagramutil.connector.models.connector.Function;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.diagramutil.connector.models.connector.types.PathParamType;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.ModuleDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Generate connector metadata using project syntax tree and semantic model.
 */
public class ConnectorGenerator {

    private ConnectorGenerator() {
    }

    /**
     * Find connector implementations inside the project and generate metadata.
     *
     * @param project  Balerina Project
     * @param detailed Get connectors with metadata
     * @param query    Filter connector list
     * @return List of connectors
     */
    public static List<Connector> getProjectConnectors(Project project, boolean detailed, String query) {
        List<Connector> connectorsList = new ArrayList<>();
        Package currentPackage = project.currentPackage();
        String packageName = currentPackage.packageName().toString();
        String version = currentPackage.packageVersion().toString();
        String orgName = currentPackage.packageOrg().toString();

        currentPackage.modules().forEach(module -> module.documentIds().forEach(documentId -> {
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

                    if (detailed) {
                        List<Function> functions = getConnectorFunctions(semanticModel, classDefinition);
                        connectorsList.add(new Connector(orgName, moduleName, packageName, version,
                                connectorName, description, connectorAnnotation, functions));
                        continue;
                    }
                    // Filter connectors with search query
                    if (!query.isEmpty() && !(orgName.toLowerCase(Locale.ROOT).contains(query)
                            || moduleName.toLowerCase(Locale.ROOT).contains(query)
                            || connectorName.toLowerCase(Locale.ROOT).contains(query)
                            || (connectorAnnotation.get("label") != null
                            && connectorAnnotation.get("label").toLowerCase(Locale.ROOT).contains(query)))) {
                        continue;
                    }

                    connectorsList.add(new Connector(orgName, moduleName, packageName, version,
                            connectorName, description, connectorAnnotation));
                }
            }

        }));
        return connectorsList;
    }

    /**
     * Generate list of connector metadata inside a project.
     *
     * @param project Ballerina project
     * @return Connector list
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
     * @return List of connectors
     */
    public static List<Connector> getConnectorModelFromSyntaxTree(SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                                  String orgName, String packageName, String version,
                                                                  String moduleName) {
        Type.clearVisitedTypeMap();
        List<Connector> connectorsList = new ArrayList<>();
        if (!syntaxTree.containsModulePart()) {
            return connectorsList;
        }
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (Node node : modulePartNode.members()) {
            if (node.kind() != SyntaxKind.CLASS_DEFINITION) {
                continue;
            }
            ClassDefinitionNode classDefinition = (ClassDefinitionNode) node;
            if (classDefinition.visibilityQualifier().isEmpty() || !classDefinition.visibilityQualifier().get()
                    .kind().equals(SyntaxKind.PUBLIC_KEYWORD) || !Generator
                    .containsToken(classDefinition.classTypeQualifiers(), SyntaxKind.CLIENT_KEYWORD)) {
                continue;
            }
            String connectorName = classDefinition.className().text();
            String description = GeneratorUtils.getDocFromMetadata(classDefinition.metadata());
            Map<String, String> connectorAnnotation =
                    GeneratorUtils.getDisplayAnnotationFromMetadataNode(classDefinition.metadata());
            if (!connectorAnnotation.isEmpty() && connectorAnnotation.containsKey("label") &&
                    !connectorAnnotation.get("label").isEmpty()) {
                connectorName = connectorAnnotation.get("label");
            }

            List<Function> functions = getConnectorFunctions(semanticModel, classDefinition);
            connectorsList.add(new Connector(orgName, moduleName, packageName, version, connectorName,
                    description, connectorAnnotation, functions));
        }
        return connectorsList;
    }

    private static List<Function> getConnectorFunctions(SemanticModel semanticModel,
                                                        ClassDefinitionNode classDefinition) {
        List<Function> functions = new ArrayList<>();
        for (Node member : classDefinition.members()) {
            if (!(member instanceof FunctionDefinitionNode functionDefinition)) {
                continue;
            }
            NodeList<Token> qualifierList = functionDefinition.qualifierList();
            if ((Generator.containsToken(qualifierList, SyntaxKind.PUBLIC_KEYWORD) ||
                    Generator.containsToken(qualifierList, SyntaxKind.REMOTE_KEYWORD) ||
                    Generator.containsToken(qualifierList, SyntaxKind.RESOURCE_KEYWORD))) {

                String functionName = functionDefinition.functionName().text();
                Map<String, String> funcAnnotation =
                        GeneratorUtils.getDisplayAnnotationFromMetadataNode(functionDefinition.metadata());

                FunctionSignatureNode functionSignature = functionDefinition.functionSignature();
                List<PathParamType> pathParams = new ArrayList<>(GeneratorUtils.getPathParameters(
                        functionDefinition.relativeResourcePath()));
                List<Type> queryParams = new ArrayList<>(GeneratorUtils.getFunctionParameters(
                        functionSignature.parameters(),
                        functionDefinition.metadata(), semanticModel));

                Type returnParam = null;
                if (functionSignature.returnTypeDesc().isPresent()) {
                    returnParam = GeneratorUtils.getReturnParameter(functionSignature.returnTypeDesc().get(),
                            functionDefinition.metadata(), semanticModel);
                }

                String[] qualifierArr = new String[qualifierList.size()];
                for (int i = 0; i < qualifierList.size(); i++) {
                    qualifierArr[i] = qualifierList.get(i).toString().trim();
                }

                functions.add(new Function(functionName, pathParams, queryParams, returnParam, funcAnnotation,
                        qualifierArr, GeneratorUtils.getDocFromMetadata(functionDefinition.metadata())));
            }
        }
        return functions;
    }


    /**
     * Get display annotation.
     *
     * @param annotations Annotation node
     * @return Display annotation list
     */
    public static Map<String, String> getDisplayAnnotationFromAnnotationsList(NodeList<AnnotationNode> annotations) {
        Map<String, String> displayAnnotation = new HashMap<>();
        Optional<AnnotationNode> optDisplayAnnotation = annotations.stream()
                .filter(annotationNode -> annotationNode.annotReference().toString().equals("display ")).findAny();
        if (!(optDisplayAnnotation.isPresent() && optDisplayAnnotation.get().annotValue().isPresent())) {
            return displayAnnotation;
        }

        optDisplayAnnotation.get().annotValue().get().fields().forEach(mappingFieldNode -> {
            if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                SpecificFieldNode fieldNode = (SpecificFieldNode) mappingFieldNode;
                if (fieldNode.fieldName().toString().equals("label") ||
                        fieldNode.fieldName().toString().equals("iconPath")) {
                    displayAnnotation.put(fieldNode.fieldName().toString(),
                            fieldNode.valueExpr().isPresent() ?
                                    fieldNode.valueExpr().get().toSourceCode().replace("\"", "") : "");
                } else {
                    displayAnnotation.put(fieldNode.fieldName().toString(),
                            fieldNode.valueExpr().isPresent() ?
                                    fieldNode.valueExpr().get().toSourceCode() : "");
                }
            }
        });

        return displayAnnotation;
    }

    /**
     * Get display annotation form metadata node.
     *
     * @param optionalMetadataNode Metadata node
     * @return Display annotation map
     */
    public static Map<String, String> getDisplayAnnotationFromMetadataNode(
            Optional<MetadataNode> optionalMetadataNode) {
        Map<String, String> result = Collections.emptyMap();
        if (optionalMetadataNode.isPresent()) {
            result = getDisplayAnnotationFromAnnotationsList(optionalMetadataNode.get().annotations());
        }
        return result;
    }

    /**
     * Get doc info from metadata node.
     *
     * @param optionalMetadataNode Metadata node
     * @return Document text
     */
    private static String getDocFromMetadata(Optional<MetadataNode> optionalMetadataNode) {
        String result;
        if (optionalMetadataNode.isEmpty()) {
            result = "";
        } else {
            StringBuilder doc = new StringBuilder();
            Optional<Node> docLines = optionalMetadataNode.get().documentationString();
            if (docLines.isEmpty()) {
                result = doc.toString();
            } else {
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
                result = doc.toString();
            }
        }

        return result;
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
        markdownCodeBlockNode.langAttribute().ifPresent(doc::append);

        for (MarkdownCodeLineNode codeLineNode : markdownCodeBlockNode.codeLines()) {
            doc.append(codeLineNode.codeDescription().toString());
        }

        doc.append(markdownCodeBlockNode.endBacktick().toString());
        return doc.toString();
    }

}
