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
package org.ballerinalang.langserver.command.executors;

import io.ballerina.projects.Project;
import io.ballerina.projects.util.DependencyUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.codeaction.providers.imports.PullModuleCodeAction;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.eventsync.EventSyncPubSubHolder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Command executor for pulling a package from central.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class PullModuleExecutor implements LSCommandExecutor {

    public static final String COMMAND = "PULL_MODULE";
    
    private static final String TITLE_PULL_MODULE = "Pull Module";

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    @Override
    public Object execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        String fileUri = null;
        String moduleName = null;
        for (CommandArgument arg : context.getArguments()) {
            switch (arg.key()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    fileUri = arg.valueAs(String.class);
                    break;
                case CommandConstants.ARG_KEY_MODULE_NAME:
                    moduleName = arg.valueAs(String.class);
                    break;
                default:
            }
        }

        // TODO Prevent running parallel tasks for the same project in future
        String taskId = UUID.randomUUID().toString();
        Path filePath = PathUtil.getPathFromURI(fileUri)
                .orElseThrow(() -> new UserErrorException("Couldn't determine file path"));

        Project project = context.workspace().project(filePath)
                .orElseThrow(() -> new UserErrorException("Couldn't find project to pull modules"));

        ExtendedLanguageClient languageClient = context.getLanguageClient();
        LSClientLogger clientLogger = LSClientLogger.getInstance(context.languageServercontext());
        String finalFileUri = fileUri;
        CompletableFuture
                .runAsync(() -> {
                    clientLogger.logTrace("Started pulling modules for project: " + project.sourceRoot().toString());

                    // Initialize progress notification
                    WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
                    workDoneProgressCreateParams.setToken(taskId);
                    languageClient.createProgress(workDoneProgressCreateParams);

                    // Start progress
                    WorkDoneProgressBegin beginNotification = new WorkDoneProgressBegin();
                    beginNotification.setTitle(TITLE_PULL_MODULE);
                    beginNotification.setCancellable(false);
                    beginNotification.setMessage("pulling missing ballerina modules");
                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                            Either.forLeft(beginNotification)));
                })
                .thenRunAsync(() -> DependencyUtils.pullMissingDependencies(project))
                .thenRunAsync(() -> {
                    try {
                        // Refresh project
                        ((BallerinaWorkspaceManager) context.workspace()).refreshProject(filePath);
                    } catch (WorkspaceDocumentException e) {
                        throw new UserErrorException("Failed to refresh project");
                    }
                })
                .thenRunAsync(() -> {
                    DocumentServiceContext docContext = ContextBuilder.buildDocumentServiceContext(finalFileUri,
                            context.workspace(), LSContextOperation.TXT_DID_CHANGE,
                            context.languageServercontext());
                    DiagnosticsHelper.getInstance(context.languageServercontext())
                            .schedulePublishDiagnostics(languageClient, docContext);
                })
                .thenRunAsync(() -> {
                    Optional<List<String>> missingModules = context.workspace()
                            .waitAndGetPackageCompilation(filePath)
                            .map(compilation -> compilation.diagnosticResult().diagnostics().stream()
                                    .filter(diagnostic -> DiagnosticErrorCode.MODULE_NOT_FOUND.diagnosticId()
                                            .equals(diagnostic.diagnosticInfo().code()))
                                    .map(PullModuleCodeAction::getMissingModuleNameFromDiagnostic)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList())
                            );
                    if (missingModules.isEmpty()) {
                        throw new UserErrorException("Failed to pull modules!");
                    } else if (!missingModules.get().isEmpty()) {
                        String moduleNames = String.join(", ", missingModules.get());
                        throw new UserErrorException(String.format("Failed to pull modules: %s", moduleNames));
                    }
                })
                .whenComplete((missingModules, t) -> {
                    boolean failed = true;
                    if (t != null) {
                        clientLogger.logError(LSContextOperation.WS_EXEC_CMD,
                                "Pull modules failed for project: " + project.sourceRoot().toString(),
                                t, null, (Position) null);
                        if (t.getCause() instanceof UserErrorException) {
                            String errorMessage = t.getCause().getMessage();
                            CommandUtil.notifyClient(languageClient, MessageType.Error, errorMessage);
                        } else {
                            CommandUtil.notifyClient(languageClient, MessageType.Error, "Failed to pull modules!");
                        }
                    } else {
                        failed = false;
                        CommandUtil.notifyClient(languageClient, MessageType.Info, "Module(s) pulled successfully!");
                        clientLogger
                                .logTrace("Finished pulling modules for project: " + project.sourceRoot().toString());
                        try {
                            DocumentServiceContext documentServiceContext = 
                                    ContextBuilder.buildDocumentServiceContext(filePath.toUri().toString(), 
                                            context.workspace(), LSContextOperation.WS_EXEC_CMD,
                                            context.languageServercontext());
                            EventSyncPubSubHolder.getInstance(context.languageServercontext())
                                    .getPublisher(EventKind.PULL_MODULE)
                                    .publish(languageClient, context.languageServercontext(), documentServiceContext);
                        } catch (Throwable e) {
                            //ignore
                        }
                    }

                    WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
                    if (failed) {
                        endNotification.setMessage("Failed to pull unresolved modules!");
                    } else {
                        endNotification.setMessage("Modules pulled successfully!");
                    }
                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                            Either.forLeft(endNotification)));
                });

        return new Object();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
