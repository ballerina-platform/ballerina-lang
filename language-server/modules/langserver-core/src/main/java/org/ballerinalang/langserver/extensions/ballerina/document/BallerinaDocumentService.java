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

import com.google.gson.JsonElement;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.util.DependencyUtils;
import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGen;
import io.ballerina.syntaxapicallsgen.config.SyntaxApiCallsGenConfig;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManagerProxy;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.FindNodes;
import org.ballerinalang.langserver.extensions.ballerina.packages.BallerinaPackageService;
import org.ballerinalang.langserver.extensions.ballerina.packages.PackageMetadataResponse;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of Ballerina Document extension for Language Server.
 *
 * @since 0.981.2
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("ballerinaDocument")
public class BallerinaDocumentService implements ExtendedLanguageServerService {

    protected static final String MINUTIAE = "WHITESPACE_MINUTIAE";

    private WorkspaceManagerProxy workspaceManagerProxy;
    private LSClientLogger clientLogger;
    private LanguageServerContext serverContext;

    @Override
    public void init(LanguageServer langServer,
                     WorkspaceManagerProxy workspaceManagerProxy,
                     LanguageServerContext serverContext) {
        this.workspaceManagerProxy = workspaceManagerProxy;
        this.serverContext = serverContext;
        this.clientLogger = LSClientLogger.getInstance(serverContext);
    }

    @JsonRequest
    public CompletableFuture<SyntaxApiCallsResponse> syntaxApiCalls(SyntaxApiCallsRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            SyntaxApiCallsResponse reply = new SyntaxApiCallsResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
            if (filePath.isEmpty()) {
                return reply;
            }

            try {
                Optional<Document> srcFile = this.workspaceManagerProxy.get().document(filePath.get());
                if (srcFile.isEmpty()) {
                    return reply;
                }
                // Create the config object
                SyntaxApiCallsGenConfig syntaxApiCallsGenConfig = new SyntaxApiCallsGenConfig.Builder()
                        .ignoreMinutiae(request.getIgnoreMinutiae()).build();

                // Get the source file content.
                String srcContent = srcFile.get().textDocument().toString();

                // Get the generated syntax API quote.
                String javaCode = SyntaxApiCallsGen.generate(srcContent, syntaxApiCallsGenConfig);

                // Preparing the response.
                reply.setSource(srcFile.get().syntaxTree().toSourceCode());
                reply.setCode(javaCode);
                reply.setParseSuccess(reply.getCode() != null);
            } catch (Throwable e) {
                reply.setParseSuccess(false);
                String msg = "Operation 'ballerinaDocument/syntaxApiCalls' failed!";
                this.clientLogger.logError(DocumentContext.DC_SYNTAX_API_CALLS, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
            }
            return reply;
        });
    }

    @JsonRequest
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTree(BallerinaSyntaxTreeRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
        if (filePath.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }

        try {
            Optional<Document> srcFile = this.workspaceManagerProxy.get().document(filePath.get());
            if (srcFile.isEmpty()) {
                return CompletableFuture.supplyAsync(() -> reply);
            }

            // Get the semantic model.
            Optional<SemanticModel> semanticModel = this.workspaceManagerProxy.get().semanticModel(filePath.get());

            // Get the generated syntax tree JSON with type info.
            JsonElement jsonSyntaxTree = DiagramUtil.getSyntaxTreeJSON(srcFile.get(), semanticModel.get());

            // Preparing the response.
            reply.setSource(srcFile.get().syntaxTree().toSourceCode());
            reply.setSyntaxTree(jsonSyntaxTree);
            reply.setParseSuccess(reply.getSyntaxTree() != null);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/syntaxTree' failed!";
            this.clientLogger.logError(DocumentContext.DC_SYNTAX_TREE, msg, e, request.getDocumentIdentifier(),
                    (Position) null);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @JsonRequest
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTreeByRange(BallerinaSyntaxTreeByRangeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
            if (filePath.isEmpty()) {
                return reply;
            }

            try {
                Optional<Document> srcFile = this.workspaceManagerProxy.get().document(filePath.get());
                if (srcFile.isEmpty()) {
                    return reply;
                }

                // Get the semantic model.
                Optional<SemanticModel> semanticModel = this.workspaceManagerProxy.get().semanticModel(filePath.get());

                //Find the ST Nodes of the selected range
                SyntaxTree syntaxTree = srcFile.get().syntaxTree();
                NonTerminalNode node = CommonUtil.findNode(request.getLineRange(), syntaxTree);

                // Get the generated syntax tree JSON with type info.
                JsonElement subSyntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(node, semanticModel.get());

                // Preparing the response.
                reply.setSource(node.toSourceCode());
                reply.setSyntaxTree(subSyntaxTreeJSON);
                reply.setParseSuccess(reply.getSyntaxTree() != null);
                return reply;
            } catch (Throwable e) {
                reply.setParseSuccess(false);
                String msg = "Operation 'ballerinaDocument/syntaxTreeByRange' failed!";
                this.clientLogger.logError(DocumentContext.DC_SYNTAX_TREE_BY_RANGE, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return reply;
            }
        });
    }

    @JsonRequest
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTreeLocate(BallerinaSyntaxTreeByRangeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
            if (filePath.isEmpty()) {
                return reply;
            }

            try {
                Optional<Document> srcFile = this.workspaceManagerProxy.get().document(filePath.get());
                if (srcFile.isEmpty()) {
                    return reply;
                }

                // Get the semantic model.
                Optional<SemanticModel> semanticModel = this.workspaceManagerProxy.get().semanticModel(filePath.get());

                //Find the ST Nodes of the selected range
                SyntaxTree syntaxTree = srcFile.get().syntaxTree();

                // Get the generated syntax tree JSON with type info.
                JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(srcFile.get(), semanticModel.get());

                //Map the path on the JSON syntax tree object
                syntaxTreeJSON = BallerinaLocateSyntaxTreeUtil.mapNodePath(request.getLineRange(), syntaxTree,
                        syntaxTreeJSON);

                // Preparing the response.
                reply.setSyntaxTree(syntaxTreeJSON);
                reply.setSource(srcFile.get().syntaxTree().toSourceCode());
                reply.setParseSuccess(reply.getSyntaxTree() != null);
                return reply;
            } catch (Throwable e) {
                reply.setParseSuccess(false);
                String msg = "Operation 'ballerinaDocument/syntaxTreeLocate' failed!";
                this.clientLogger.logError(DocumentContext.DC_SYNTAX_TREE_LOCATE, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return reply;
            }
        });
    }

    @JsonRequest
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTreeModify(BallerinaSyntaxTreeModifyRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
        if (filePath.isEmpty()) {
            reply.setParseSuccess(false);
            return CompletableFuture.supplyAsync(() -> reply);
        }

        try {
            // Apply modifications.
            JsonElement syntaxTreeWithSource = BallerinaTreeModifyUtil.modifyTree(request.getAstModifications(),
                    filePath.get(), this.workspaceManagerProxy.get());

            // Preparing the response.
            reply.setSource(syntaxTreeWithSource.getAsJsonObject().get("source").getAsString());
            reply.setSyntaxTree(syntaxTreeWithSource.getAsJsonObject().get("tree"));
            reply.setParseSuccess(reply.getSyntaxTree() != null);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/syntaxTreeModify' failed!";
            this.clientLogger.logError(DocumentContext.DC_SYNTAX_TREE_MODIFY, msg, e, request.getDocumentIdentifier(),
                    (Position) null);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @JsonRequest
    public CompletableFuture<BallerinaSyntaxTreeResponse> triggerModify(BallerinaTriggerModifyRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
        if (filePath.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        try {
            // Apply modifications to the trigger
            JsonElement syntaxTreeWithSource = BallerinaTriggerModifyUtil.modifyTrigger(request.getType(),
                    request.getConfig(), filePath.get(), this.workspaceManagerProxy.get());

            // Preparing the response.
            reply.setSource(syntaxTreeWithSource.getAsJsonObject().get("source").getAsString());
            reply.setSyntaxTree(syntaxTreeWithSource.getAsJsonObject().get("tree"));
            reply.setParseSuccess(reply.getSyntaxTree() != null);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/ast' failed!";
            this.clientLogger.logError(DocumentContext.DC_AST, msg, e, request.getDocumentIdentifier(),
                    (Position) null);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @JsonRequest
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTreeByName(BallerinaSyntaxTreeByNameRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
            WorkspaceManager workspaceManager = this.workspaceManagerProxy.get(fileUri);

            try {
                Optional<Document> srcFile = workspaceManager.document(filePath.get());

                // Get the semantic model.
                Optional<SemanticModel> semanticModel = workspaceManager.semanticModel(filePath.get());

                // Get the start line range of function invoke.
                int lineValue = request.getLineRange().getStart().getLine();
                int charValue = request.getLineRange().getStart().getCharacter();

                // Get the symbol of function
                Optional<Symbol> functionSymbol = semanticModel.get().symbol(srcFile.get(),
                        LinePosition.from(lineValue, charValue));
                if (functionSymbol.isEmpty()) {
                    return reply;
                }

                // Get the file path of the function symbol
                String functionPath = functionSymbol.get().getLocation().get().lineRange().filePath();

                // Get the project of current file
                Optional<Project> project = workspaceManager.project(filePath.get());

                // Loop through project modules to find the document of the function declaration
                project.get().currentPackage().modules().forEach(module -> {
                    module.documentIds().forEach(id -> {
                        Document document = module.document(id);
                        if (functionPath.equals(document.name())) {
                            // Get the nodes from the found document
                            SyntaxTree st = document.syntaxTree();
                            FindNodes findNodes = new FindNodes();
                            findNodes.visit((ModulePartNode) st.rootNode());

                            // Get only the function nodes
                            List<FunctionDefinitionNode> functionNodes = findNodes.getFunctionDefinitionNodes();

                            // Find the function node equals to the function name and within line range
                            functionNodes.forEach(node -> {
                                int nodeStartLine = node.lineRange().startLine().line();
                                int nodeEndLine = node.lineRange().endLine().line();
                                int symbolLine = functionSymbol.get().getLocation().get()
                                        .lineRange().startLine().line();
                                boolean withinRange = nodeStartLine <= symbolLine && nodeEndLine >= symbolLine;

                                if (functionSymbol.get().nameEquals(node.functionName().text()) && withinRange) {

                                    // Get the new semantic model for found document
                                    PackageCompilation packageCompilation = document.module()
                                            .packageInstance().getCompilation();
                                    SemanticModel semanticModelNew = packageCompilation
                                            .getSemanticModel(document.module().moduleId());

                                    // Get the file path of the found node definition
                                    Path defFilePathLocation = PathUtil.getPathFromLocation(module, node.location());

                                    // Set the node syntax tree JSON with type info and source code.
                                    reply.setSource(node.toSourceCode());
                                    reply.setSyntaxTree(DiagramUtil.getSyntaxTreeJSON(node, semanticModelNew));
                                    reply.setParseSuccess(reply.getSyntaxTree() != null);
                                    reply.setDefFilePath(defFilePathLocation.toUri().toString());
                                }
                            });
                        }
                    });
                });
                return reply;
            } catch (Throwable e) {
                reply.setParseSuccess(false);
                String msg = "Operation 'ballerinaDocument/syntaxTreeByName' failed!";
                this.clientLogger.logError(DocumentContext.DC_SYNTAX_TREE_BY_NAME, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return reply;
            }
        });
    }
    /**
     * @deprecated use {@link BallerinaPackageService} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    @JsonRequest
    public CompletableFuture<PackageMetadataResponse> project(BallerinaProjectParams params) {
        return CompletableFuture.supplyAsync(() -> {
            PackageMetadataResponse ballerinaProject = new PackageMetadataResponse();
            try {
                Optional<Path> filePath = PathUtil.getPathFromURI(params.getDocumentIdentifier().getUri());
                if (filePath.isEmpty()) {
                    return ballerinaProject;
                }
                Optional<Project> project = this.workspaceManagerProxy.get().project(filePath.get());
                if (project.isEmpty()) {
                    return ballerinaProject;
                }
                ballerinaProject.setPath(project.get().sourceRoot().toString());
                ProjectKind projectKind = project.get().kind();
                if (projectKind != ProjectKind.SINGLE_FILE_PROJECT) {
                    ballerinaProject.setPackageName(project.get().currentPackage().packageName().value());
                }
                ballerinaProject.setKind(projectKind.name());
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaDocument/project' failed!";
                this.clientLogger.logError(DocumentContext.DC_PROJECT, msg, e, params.getDocumentIdentifier(),
                        (Position) null);
            }
            return ballerinaProject;
        });
    }

    @JsonRequest
    public CompletableFuture<List<PublishDiagnosticsParams>> diagnostics(BallerinaProjectParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getDocumentIdentifier().getUri();
            try {
                DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(fileUri,
                        this.workspaceManagerProxy.get(fileUri),
                        LSContextOperation.DOC_DIAGNOSTICS,
                        this.serverContext);
                DiagnosticsHelper diagnosticsHelper = DiagnosticsHelper.getInstance(this.serverContext);
                return diagnosticsHelper.getLatestDiagnostics(context).entrySet().stream()
                        .filter(entry -> fileUri.equals(entry.getKey()))
                        .map((entry) -> new PublishDiagnosticsParams(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaDocument/diagnostics' failed!";
                this.clientLogger.logError(DocumentContext.DC_DIAGNOSTICS, msg, e, params.getDocumentIdentifier(),
                        (Position) null);
                return Collections.emptyList();
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SyntaxTreeNodeResponse> syntaxTreeNode(SyntaxTreeNodeRequest params) {
        return CompletableFuture.supplyAsync(() -> {
            SyntaxTreeNodeResponse syntaxTreeNodeResponse = new SyntaxTreeNodeResponse();
            try {
                Optional<Path> filePath = PathUtil.getPathFromURI(params.getDocumentIdentifier().getUri());
                if (filePath.isEmpty()) {
                    return syntaxTreeNodeResponse;
                }
                SyntaxTree syntaxTree = this.workspaceManagerProxy.get().syntaxTree(filePath.get()).orElseThrow();
                NonTerminalNode currentNode = CommonUtil.findNode(params.getRange(), syntaxTree);
                LinePosition startLine = currentNode.lineRange().startLine();
                LinePosition endLine = currentNode.lineRange().endLine();
                Range cursor = params.getRange();
                if ((startLine.line() < cursor.getStart().getLine() || startLine.line() == cursor.getStart().getLine()
                        && startLine.offset() < cursor.getStart().getCharacter()) &&
                        (endLine.line() > cursor.getEnd().getLine() || endLine.line() == cursor.getEnd().getLine()
                                && endLine.offset() > cursor.getEnd().getCharacter())) {
                    syntaxTreeNodeResponse.setKind(currentNode.kind().name());
                } else {
                    syntaxTreeNodeResponse.setKind(MINUTIAE);
                }
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaDocument/syntaxTreeNode' failed!";
                this.clientLogger.logError(DocumentContext.DC_SYNTAX_TREE_NODE, msg, e, params.getDocumentIdentifier(),
                        (Position) null);
            }
            return syntaxTreeNodeResponse;
        });
    }

    @JsonRequest
    public CompletableFuture<ExecutorPositionsResponse> executorPositions(BallerinaProjectParams params) {
        return CompletableFuture.supplyAsync(() -> {
            ExecutorPositionsResponse response = new ExecutorPositionsResponse();
            try {
                String fileUri = params.getDocumentIdentifier().getUri();
                Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
                if (filePath.isEmpty()) {
                    return response;
                }

                response.setExecutorPositions(ExecutorPositionsUtil.getExecutorPositions(workspaceManagerProxy.get(),
                        filePath.get()));
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaDocument/executorPositions' failed!";
                this.clientLogger.logError(DocumentContext.DC_EXEC_POSITION, msg, e, params.getDocumentIdentifier(),
                        (Position) null);
            }
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<BallerinaSyntaxTreeResponse> resolveMissingDependencies(
            BallerinaSyntaxTreeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
            if (filePath.isEmpty()) {
                return reply;
            }

            try {
                Optional<Project> project = this.workspaceManagerProxy.get().project(filePath.get());
                if (project.isEmpty()) {
                    reply.setParseSuccess(false);
                    return reply;
                }
                DependencyUtils.pullMissingDependencies(project.get());
                reply.setParseSuccess(true);
            } catch (Throwable e) {
                reply.setParseSuccess(false);
                String msg = "Operation 'ballerinaDocument/resolveMissingDependencies' failed!";
                this.clientLogger.logError(DocumentContext.DC_RESOLVE_MISSING_DEPENDENCIES, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
            }
            return reply;
        });
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }
}
