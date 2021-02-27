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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.command.LSCommandExecutorProvidersHolder;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private final Gson gson = new Gson();

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

    private String generateHash(BLangCompilationUnit compUnit, String basePath) {
        return compUnit.getPackageID().toString() + "$" + basePath + "$" + compUnit.getName();
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
        for (FileEvent fileEvent : new HashSet<>(params.getChanges())) {
            String uri = fileEvent.getUri();
            Optional<Path> optFilePath = CommonUtil.getPathFromURI(uri);
            if (optFilePath.isEmpty()) {
                continue;
            }
            Path filePath = optFilePath.get();
            try {
                workspaceManager.didChangeWatched(filePath, fileEvent);
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("File Change Failed to Handle", e);
            } catch (Throwable e) {
                String msg = "Operation 'workspace/didChangeWatchedFiles' failed!";
                this.clientLogger.logError(msg, e, null, (Position) null);
            }
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
                    return executor.get().execute(context);
                }
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Execute Command", e);
            } catch (Throwable e) {
                String msg = "Operation 'workspace/executeCommand' failed!";
                this.clientLogger.logError(msg, e, null, (Position) null);
            }
            this.clientLogger.logError("Operation 'workspace/executeCommand' failed!",
                                       new LSCommandExecutorException(
                                               "No command executor found for '" + params.getCommand() + "'"),
                                       null, (Position) null);
            return false;
        });
    }
}
