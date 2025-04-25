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
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.ContextBuilder;
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
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Command executor for pulling a package from central.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class PullModuleExecutor implements LSCommandExecutor {

    public static final String COMMAND = "PULL_MODULE";
    private static final String TITLE_PULL_MODULE = "Pull Module";
    private static final String PULL_MODULE_TASK_PREFIX = "pull-module-";

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    @Override
    public Object execute(ExecuteCommandContext context) {
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
        resolveModules(fileUri, context.getLanguageClient(), context.workspace(), context.languageServercontext());
        return new Object();
    }

    public static void resolveModules(String fileUri, ExtendedLanguageClient languageClient,
                                      WorkspaceManager workspaceManager, LanguageServerContext languageServerContext) {
        // TODO Prevent running parallel tasks for the same project in future
        String taskId = PULL_MODULE_TASK_PREFIX + UUID.randomUUID();
        Path filePath = PathUtil.getPathFromURI(fileUri)
                .orElseThrow(() -> new UserErrorException("Couldn't determine file path"));
        Project project = workspaceManager.project(filePath)
                .orElseThrow(() -> new UserErrorException("Couldn't find project to pull modules"));

        LSClientLogger clientLogger = LSClientLogger.getInstance(languageServerContext);
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
                    beginNotification.setMessage("pulling the missing ballerina modules");
                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                            Either.forLeft(beginNotification)));
                })
                .thenRunAsync(() -> DependencyUtils.pullMissingDependencies(project))
                .thenRunAsync(() -> {
                    try {
                        // Refresh project
                        ((BallerinaWorkspaceManager) workspaceManager).refreshProject(filePath);
                    } catch (WorkspaceDocumentException e) {
                        throw new UserErrorException("Failed to refresh project");
                    }
                })
                .thenRunAsync(() -> {
                    DocumentServiceContext docContext = ContextBuilder.buildDocumentServiceContext(
                            fileUri,
                            workspaceManager,
                            LSContextOperation.RELOAD_PROJECT,
                            languageServerContext);
                    try {
                        EventSyncPubSubHolder.getInstance(languageServerContext)
                                .getPublisher(EventKind.PROJECT_UPDATE)
                                .publish(languageClient, languageServerContext, docContext);
                    } catch (EventSyncException e) {
                        // ignore
                    }
                })
                .thenRunAsync(() -> {
                    WorkDoneProgressReport workDoneProgressReport = new WorkDoneProgressReport();
                    workDoneProgressReport.setCancellable(false);
                    workDoneProgressReport.setMessage("compiling the project");
                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                            Either.forLeft(workDoneProgressReport)));

                    Optional<List<String>> missingModules = workspaceManager
                            .waitAndGetPackageCompilation(filePath)
                            .map(compilation -> compilation.diagnosticResult().diagnostics().stream()
                                    .filter(diagnostic -> DiagnosticErrorCode.MODULE_NOT_FOUND.diagnosticId()
                                            .equals(diagnostic.diagnosticInfo().code()))
                                    .map(PullModuleCodeAction::getMissingModuleNameFromDiagnostic)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .toList()
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
                                            workspaceManager, LSContextOperation.WS_EXEC_CMD,
                                            languageServerContext);
                            EventSyncPubSubHolder.getInstance(languageServerContext)
                                    .getPublisher(EventKind.PULL_MODULE)
                                    .publish(languageClient, languageServerContext, documentServiceContext);
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
