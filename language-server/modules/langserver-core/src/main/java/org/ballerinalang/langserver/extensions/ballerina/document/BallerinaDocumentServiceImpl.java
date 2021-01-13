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
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Ballerina Document extension for Language Server.
 *
 * @since 0.981.2
 */
public class BallerinaDocumentServiceImpl implements BallerinaDocumentService {
    private final WorkspaceManager workspaceManager;
    private final LSClientLogger clientLogger;

    public BallerinaDocumentServiceImpl(WorkspaceManager workspaceManager, LanguageServerContext serverContext) {
        this.workspaceManager = workspaceManager;
        this.clientLogger = LSClientLogger.getInstance(serverContext);
    }

    @Override
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTree(BallerinaSyntaxTreeRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (filePath.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }

        try {
            // Get the syntax tree for the document.
            Optional<SyntaxTree> syntaxTree = this.workspaceManager.syntaxTree(filePath.get());
            if (syntaxTree.isEmpty()) {
                return CompletableFuture.supplyAsync(() -> reply);
            }

            // Get the semantic model.
            Optional<SemanticModel> semanticModel = this.workspaceManager.semanticModel(filePath.get());

            // Get the generated syntax tree JSON with type info.
            JsonElement jsonSyntaxTree = DiagramUtil
                    .getSyntaxTreeJSON(syntaxTree.get(), semanticModel.get());

            // Preparing the response.
            reply.setSource(syntaxTree.get().toSourceCode());
            reply.setSyntaxTree(jsonSyntaxTree);
            reply.setParseSuccess(reply.getSyntaxTree() != null);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/syntaxTree' failed!";
            this.clientLogger.logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTreeModify(BallerinaSyntaxTreeModifyRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (filePath.isEmpty()) {
            reply.setParseSuccess(false);
            return CompletableFuture.supplyAsync(() -> reply);
        }

        try {
            // Apply modifications.
            JsonElement syntaxTreeWithSource = BallerinaTreeModifyUtil.modifyTree(request.getAstModifications(),
                    filePath.get(), this.workspaceManager);

            // Preparing the response.
            reply.setSource(syntaxTreeWithSource.getAsJsonObject().get("source").getAsString());
            reply.setSyntaxTree(syntaxTreeWithSource.getAsJsonObject().get("tree"));
            reply.setParseSuccess(reply.getSyntaxTree() != null);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/syntaxTreeModify' failed!";
            this.clientLogger.logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaSyntaxTreeResponse> triggerModify(BallerinaTriggerModifyRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (filePath.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        try {
            // Apply modifications to the trigger
            JsonElement syntaxTreeWithSource = BallerinaTriggerModifyUtil.modifyTrigger(request.getType(),
                    request.getConfig(), filePath.get(), this.workspaceManager);

            // Preparing the response.
            reply.setSource(syntaxTreeWithSource.getAsJsonObject().get("source").getAsString());
            reply.setSyntaxTree(syntaxTreeWithSource.getAsJsonObject().get("tree"));
            reply.setParseSuccess(reply.getSyntaxTree() != null);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/ast' failed!";
            this.clientLogger.logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaProject> project(BallerinaProjectParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaProject project = new BallerinaProject();
            try {
                Optional<Path> filePath = CommonUtil.getPathFromURI(params.getDocumentIdentifier().getUri());
                if (filePath.isEmpty()) {
                    return project;
                }
                Path projectRoot = this.workspaceManager.projectRoot(filePath.get());
                project.setPath(projectRoot.toString());
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaDocument/project' failed!";
                this.clientLogger.logError(msg, e, params.getDocumentIdentifier(), (Position) null);
            }
            return project;
        });
    }
}
