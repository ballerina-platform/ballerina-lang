/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver;

import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.CodeActionManager;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContextImpl;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.command.LSCommandExecutorProvidersHolder;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.telemetry.TelemetryUtil;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Workspace service implementation for Ballerina.
 */
public class BallerinaWorkspaceService implements WorkspaceService {

    private final BallerinaLanguageServer languageServer;
    private final LSClientConfigHolder configHolder;
    private LSClientCapabilities clientCapabilities;
    private final WorkspaceManager workspaceManager;
    private final LanguageServerContext serverContext;
    private final LSClientLogger clientLogger;

    BallerinaWorkspaceService(BallerinaLanguageServer languageServer, WorkspaceManager workspaceManager,
                              LanguageServerContext serverContext) {
        this.languageServer = languageServer;
        this.workspaceManager = workspaceManager;
        this.serverContext = serverContext;
        this.configHolder = LSClientConfigHolder.getInstance(this.serverContext);
        this.clientLogger = LSClientLogger.getInstance(this.serverContext);
    }

    public void setClientCapabilities(LSClientCapabilities clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        if (!(params.getSettings() instanceof JsonObject)) {
            return;
        }
        JsonObject settings = (JsonObject) params.getSettings();
        if (settings.get("ballerina") != null) {
            configHolder.updateConfig(settings.get("ballerina"));
        } else {
            // To support old plugins versions
            configHolder.updateConfig(settings);
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // Looping through a set to avoid duplicated file events
//        for (FileEvent fileEvent : new HashSet<>(params.getChanges())) {
//            String uri = fileEvent.getUri();
//            Optional<Path> optFilePath = CommonUtil.getPathFromURI(uri);
//            if (optFilePath.isEmpty()) {
//                continue;
//            }
//            Path filePath = optFilePath.get();
//            try {
//                workspaceManager.didChangeWatched(filePath, fileEvent);
//            } catch (UserErrorException e) {
//                this.clientLogger.notifyUser("File Change Failed to Handle", e);
//            } catch (Throwable e) {
//                String msg = "Operation 'workspace/didChangeWatchedFiles' failed!";
//                this.clientLogger.logError(LSContextOperation.WS_WF_CHANGED, msg, e, null, (Position) null);
//            }
//        }
        try {
            this.workspaceManager.didChangeWatched(params);
        } catch (WorkspaceDocumentException e) {
            String msg = "Operation 'workspace/didChangeWatchedFiles' failed!";
            this.clientLogger.logError(LSContextOperation.WS_WF_CHANGED, msg, e, null, (Position) null);
        }
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CommandArgument> commandArguments = params.getArguments().stream()
                    .map(CommandArgument::from)
                    .collect(Collectors.toList());
            ExecuteCommandContext context = ContextBuilder.buildExecuteCommandContext(this.workspaceManager,
                    this.serverContext,
                    commandArguments,
                    this.clientCapabilities,
                    this.languageServer);

            try {
                Optional<LSCommandExecutor> executor = LSCommandExecutorProvidersHolder.getInstance(this.serverContext)
                        .getCommandExecutor(params.getCommand());
                if (executor.isPresent()) {
                    LSCommandExecutor commandExecutor = executor.get();
                    Object result = commandExecutor.execute(context);
                    // Send feature usage telemetry event if applicable
                    TelemetryUtil.featureUsageEventFromCommandExecutor(commandExecutor)
                            .ifPresent(lsFeatureUsageTelemetryEvent ->
                                    TelemetryUtil.sendTelemetryEvent(context.languageServercontext(),
                                            lsFeatureUsageTelemetryEvent));
                    return result;
                } else {
                    // Check in plugins
                    return executeCommandExternal(context, params);
                }
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Execute Command", e);
            } catch (Throwable e) {
                String msg = "Operation 'workspace/executeCommand' failed!";
                this.clientLogger.logError(LSContextOperation.WS_EXEC_CMD, msg, e, null, (Position) null);
            }
            this.clientLogger.logError(LSContextOperation.WS_EXEC_CMD, "Operation 'workspace/executeCommand' failed!",
                    new LSCommandExecutorException(
                            "No command executor found for '" + params.getCommand() + "'"),
                    null, (Position) null);
            return false;
        });
    }

    /**
     * Execute commands provided by compiler plugins. Depending on the result, relevant workspace edits will be
     * performed.
     *
     * @param context Execute command context
     * @param params  Execute command params
     * @return any | null
     */
    private Object executeCommandExternal(ExecuteCommandContext context, ExecuteCommandParams params) {
        List<CodeActionArgument> args = new LinkedList<>();
        String uri = null;
        for (CommandArgument arg : context.getArguments()) {
            if (CommandConstants.ARG_KEY_DOC_URI.equals(arg.key())) {
                uri = arg.valueAs(String.class);
            } else {
                args.add(CodeActionArgument.from(arg.key(), arg.value()));
            }
        }

        Optional<Path> filePath = CommonUtil.getPathFromURI(uri);
        if (filePath.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<PackageCompilation> packageCompilation =
                context.workspace().waitAndGetPackageCompilation(filePath.get());
        Optional<Document> document = context.workspace().document(filePath.get());
        Optional<SemanticModel> semanticModel = context.workspace().semanticModel(filePath.get());
        if (packageCompilation.isEmpty() || document.isEmpty() || semanticModel.isEmpty()) {
            return Collections.emptyList();
        }

        CodeActionExecutionContext codeActionContext = CodeActionExecutionContextImpl.from(uri, filePath.get(),
                null, document.get(), semanticModel.get(), args);

        String providerName = params.getCommand();
        CodeActionManager codeActionManager = packageCompilation.get().getCodeActionManager();
        List<DocumentEdit> docEdits = codeActionManager.executeCodeAction(providerName, codeActionContext);

        List<Either<TextDocumentEdit, ResourceOperation>> edits = new LinkedList<>();
        docEdits.forEach(docEdit -> {
            Optional<SyntaxTree> originalST = CommonUtil.getPathFromURI(docEdit.getFileUri())
                    .flatMap(workspaceManager::document)
                    .flatMap(doc -> Optional.of(doc.syntaxTree()));
            if (originalST.isEmpty()) {
                return;
            }

            LineRange lineRange = originalST.get().rootNode().lineRange();
            Range range = CommonUtil.toRange(LineRange.from(docEdit.getFileUri(),
                    lineRange.startLine(), lineRange.endLine()));
            TextEdit edit = new TextEdit(range, docEdit.getModifiedSyntaxTree().toSourceCode());
            TextDocumentEdit documentEdit = new TextDocumentEdit(new VersionedTextDocumentIdentifier(
                    docEdit.getFileUri(), null), Collections.singletonList(edit));
            Either<TextDocumentEdit, ResourceOperation> either = Either.forLeft(documentEdit);
            edits.add(either);
        });
        return CommandUtil.applyWorkspaceEdit(edits, languageServer.getClient());
    }
}
