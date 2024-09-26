/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextEdit;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.diagramutil.JSONGenerationException;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.FindNodes;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.UnusedSymbolsVisitor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Represents a request for a Ballerina AST Modify.
 *
 * @since 1.3.0
 */
public final class BallerinaTriggerModifyUtil {

//    private static final Logger logger = LoggerFactory.getLogger(BallerinaDocumentServiceImpl.class);

    private static final String MAIN = "main";
    private static final String SERVICE = "service";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String CURRENT_TRIGGER = "CURRENT_TRIGGER";
    public static final String EMPTY_STRING = "";

    private BallerinaTriggerModifyUtil() {
    }


    public static JsonElement modifyTrigger(String type, JsonObject config, Path compilationPath,
                                            WorkspaceManager workspaceManager)
            throws WorkspaceDocumentException, JSONGenerationException,
            FormatterException {
        Optional<SyntaxTree> oldSyntaxTree = workspaceManager.syntaxTree(compilationPath);
        boolean isFileEmpty = false;

        if (oldSyntaxTree.isEmpty()) {
            oldSyntaxTree.get();
        }

        TextDocument oldTextDocument = oldSyntaxTree.get().textDocument();

        List<TextEdit> edits =
                BallerinaTriggerModifyUtil.createTriggerEdits(oldTextDocument, oldSyntaxTree.get(),
                        type.toUpperCase(), config);

        //perform edits
        TextDocumentChange textDocumentChange = TextDocumentChange.from(edits.toArray(
                new TextEdit[0]));
        TextDocument newTextDoc = oldTextDocument.apply(textDocumentChange);
        SyntaxTree updatedSyntaxTree = SyntaxTree.from(newTextDoc);
        SemanticModel updatedSemanticModel = updateWorkspaceDocument(compilationPath, updatedSyntaxTree.toSourceCode(),
                                                                     workspaceManager);
        Document srcFile = workspaceManager.document(compilationPath).orElseThrow();

        //remove unused imports
        UnusedSymbolsVisitor unusedSymbolsVisitor = new UnusedSymbolsVisitor(srcFile, updatedSemanticModel,
                                                                             new HashMap<>());
        unusedSymbolsVisitor.visit((ModulePartNode) updatedSyntaxTree.rootNode());

        if (!unusedSymbolsVisitor.getUnusedImports().isEmpty()) {
            TextDocument updatedTextDocument = TextDocuments.from(newTextDoc.toString());
            edits = BallerinaTreeModifyUtil.getUnusedImportRanges(
                    unusedSymbolsVisitor.getUnusedImports(), updatedTextDocument);
            textDocumentChange = TextDocumentChange.from(edits.toArray(
                    new TextEdit[0]));
            newTextDoc = newTextDoc.apply(textDocumentChange);
        }

        // Use formatter to format the source document.
        SyntaxTree syntaxTree = SyntaxTree.from(newTextDoc);
        String formattedSource = Formatter.format(syntaxTree).toSourceCode();

        SemanticModel newSemanticModel = updateWorkspaceDocument(compilationPath, formattedSource,
                                                                 workspaceManager);
        Optional<Document> formattedSrcFile = workspaceManager.document(compilationPath);
        if (formattedSrcFile.isEmpty()) {
            throw new JSONGenerationException("Modification error");
        }

        JsonElement syntaxTreeJson = DiagramUtil.getSyntaxTreeJSON(formattedSrcFile.get(), newSemanticModel);
        JsonObject jsonTreeWithSource = new JsonObject();
        jsonTreeWithSource.add("tree", syntaxTreeJson);
        jsonTreeWithSource.addProperty("source", formattedSrcFile.get().syntaxTree().toSourceCode());
        return jsonTreeWithSource;
    }

    private static SemanticModel updateWorkspaceDocument(Path compilationPath, String content,
                                                         WorkspaceManager workspaceManager)
            throws WorkspaceDocumentException {
        // TODO: Find a better way to get the semantic model for new TextDocument.
        Optional<Document> document = workspaceManager.document(compilationPath);
        if (document.isEmpty()) {
            throw new WorkspaceDocumentException("Document does not exist in path: " + compilationPath.toString());
        }


        // Update file
        Document updatedDoc = document.get().modify().withContent(content).apply();
        // Update project instance
        return updatedDoc.module().packageInstance().getCompilation().getSemanticModel(updatedDoc.module().moduleId());
    }

    private static List<FunctionDefinitionNode> getResourceFunctions(ServiceDeclarationNode serviceDeclarationNode) {
        List<FunctionDefinitionNode> resources = new ArrayList<>();
        for (Node node : serviceDeclarationNode.members()) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
                List<String> qualifiers = functionDefinitionNode.qualifierList().stream().map(Token::text)
                        .toList();
                if (qualifiers.contains(SyntaxKind.RESOURCE_KEYWORD.stringValue())) {
                    resources.add(functionDefinitionNode);
                }
            }
        }
        return resources;
    }

    private static List<TextEdit> createTriggerEdits(TextDocument oldTextDocument,
                                                     SyntaxTree oldSyntaxTree,
                                                     String type, JsonObject config) {
        List<TextEdit> edits = new ArrayList<>();
        Gson gson = new Gson();
        String currentTrigger = EMPTY_STRING;
        if (config != null && config.has(CURRENT_TRIGGER)) {
            currentTrigger = config.get(CURRENT_TRIGGER).getAsString();
        }
        FindNodes findNodes = new FindNodes();
        findNodes.visit((ModulePartNode) oldSyntaxTree.rootNode());

        if (findNodes.getFunctionDefinitionNodes().isEmpty() && findNodes.getServiceDeclarationNodes().isEmpty()) {
            //insert new
            if (MAIN.equalsIgnoreCase(type)) {
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_START",
                        1, 1, 1, 1));
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_END",
                        1, 1, 1, 1));
            } else if (SERVICE.equalsIgnoreCase(type)) {
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument,
                        gson.fromJson("{\"TYPE\":\"ballerina/http\"}",
                                JsonObject.class), "IMPORT",
                        1, 1, 1, 1));
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_START",
                        1, 1, 1, 1));
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_END",
                        1, 1, 1, 1));
            }
        } else {
            Optional<FunctionDefinitionNode> mainFunction = findNodes.getFunctionDefinitionNodes().stream().
                    filter(function -> function.functionName().text().equals("main")).findFirst();
            if (mainFunction.isPresent()) {
                //replace main
                if (MAIN.equalsIgnoreCase(type)) {
                    Optional<ImportDeclarationNode> httpImport = findNodes.getImportDeclarationNodesList().stream().
                            filter(aImport -> (aImport.orgName().isPresent()
                                    ? aImport.orgName().get().orgName().text() : "")
                                    .equalsIgnoreCase("ballerina")
                                    && aImport.moduleName().size() == 1 &&
                                    aImport.moduleName().get(0).text().equalsIgnoreCase("http")).
                            findFirst();
                    if (httpImport.isEmpty()) {
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument,
                                gson.fromJson("{\"TYPE\":\"ballerina/http\"}",
                                        JsonObject.class), "IMPORT",
                                1, 1, 1, 1));
                    }
                    int startLine = mainFunction.get().lineRange().startLine().line();
                    if (SCHEDULE.equalsIgnoreCase(currentTrigger)) {
                        --startLine;
                    }
                    edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_START_MODIFY",
                            startLine,
                            mainFunction.get().lineRange().startLine().offset(),
                            mainFunction.get().functionBody().lineRange().startLine().line(),
                            mainFunction.get().functionBody().lineRange().startLine().offset() + 1));
                } else if (SERVICE.equalsIgnoreCase(type)) {
                    Optional<ImportDeclarationNode> httpImport = findNodes.getImportDeclarationNodesList().stream().
                            filter(aImport -> (aImport.orgName().isPresent()
                                    ? aImport.orgName().get().orgName().text() : "")
                                    .equalsIgnoreCase("ballerina")
                                    && aImport.moduleName().size() == 1 &&
                                    aImport.moduleName().get(0).text().equalsIgnoreCase("http")).
                            findFirst();
                    if (httpImport.isEmpty()) {
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument,
                                gson.fromJson("{\"TYPE\":\"ballerina/http\"}",
                                        JsonObject.class), "IMPORT",
                                1, 1, 1, 1));
                    }
                    int startLine = mainFunction.get().lineRange().startLine().line();
                    if (SCHEDULE.equalsIgnoreCase(currentTrigger)) {
                        --startLine;
                    }
                    edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_START",
                            startLine,
                            mainFunction.get().lineRange().startLine().offset(),
                            mainFunction.get().functionBody().lineRange().startLine().line(),
                            mainFunction.get().functionBody().lineRange().startLine().offset() + 1));
                    edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_END",
                            mainFunction.get().functionBody().lineRange().endLine().line(),
                            mainFunction.get().functionBody().lineRange().endLine().offset() - 1,
                            mainFunction.get().lineRange().endLine().line(),
                            mainFunction.get().lineRange().endLine().offset()));
                }
            } else {
                Optional<ServiceDeclarationNode> service = findNodes.getServiceDeclarationNodes().stream().findFirst();
                if (service.isPresent()) {
                    List<FunctionDefinitionNode> resourceFunctions = getResourceFunctions(service.get());

                    //replace service
                    if (MAIN.equalsIgnoreCase(type)) {
                        if (service.get().metadata().isPresent()
                                && service.get().metadata().get().annotations() != null &&
                                !service.get().metadata().get().annotations().isEmpty()) {
                            edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config,
                                    "MAIN_START", service.get().metadata().get().annotations().get(0)
                                            .lineRange().startLine().line(),
                                    service.get().metadata().get().annotations().get(0).lineRange()
                                            .startLine().offset(),
                                    resourceFunctions.get(0).functionBody().lineRange()
                                            .startLine().line(),
                                    resourceFunctions.get(0).functionBody().
                                            lineRange().startLine().offset() + 1));
                        } else {
                            edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config,
                                    "MAIN_START", service.get().lineRange().startLine().line(),
                                    service.get().lineRange().startLine().offset(),
                                    resourceFunctions.get(0).functionBody().lineRange()
                                            .startLine().line(),
                                    resourceFunctions.get(0).functionBody().lineRange()
                                            .startLine().offset() + 1));
                        }
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_END",
                                resourceFunctions.get(0).functionBody()
                                        .lineRange().endLine().line(),
                                resourceFunctions.get(0).functionBody()
                                        .lineRange().endLine().offset() - 1,
                                service.get().lineRange().endLine().line(),
                                service.get().lineRange().endLine().offset()));
                    } else if (SERVICE.equalsIgnoreCase(type)) {
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(
                                oldTextDocument,
                                config,
                                "SERVICE_START_MODIFY",
                                service.get().metadata().get().annotations().get(0).lineRange().startLine().line(),
                                service.get().metadata().get().annotations().get(0).lineRange().startLine().offset(),
                                resourceFunctions.get(0).functionBody().lineRange()
                                        .startLine().line(),
                                resourceFunctions.get(0).functionBody()
                                        .lineRange().startLine().offset() + 1));
                    }
                } else {
                    throw new RuntimeException("Trigger function not found for replacement!");
                }
            }
        }
        return edits;
    }

}
