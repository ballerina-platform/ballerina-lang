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
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.config.LSClientConfig;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.symbols.SymbolFindingVisitor;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSClientLogger.logError;
import static org.ballerinalang.langserver.compiler.LSClientLogger.notifyUser;

/**
 * Workspace service implementation for Ballerina.
 */
public class BallerinaWorkspaceService implements WorkspaceService {
    private BallerinaLanguageServer languageServer;
    private WorkspaceDocumentManager workspaceDocumentManager;
    private static final Gson GSON = new Gson();
    private LSClientConfigHolder configHolder = LSClientConfigHolder.getInstance();
    private LSClientCapabilities clientCapabilities;

    BallerinaWorkspaceService(LSGlobalContext globalContext) {
        this.languageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.workspaceDocumentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
    }

    public void setClientCapabilities(LSClientCapabilities clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<Either<SymbolInformation, DocumentSymbol>> symbols = new ArrayList<>();
            LSContext symbolsContext = new WorkspaceServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.WS_SYMBOL)
                    .build();
            Map<String, Object[]> compUnits = new HashMap<>();
            try {
                for (Path path : this.workspaceDocumentManager.getAllFilePaths()) {
                    symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);
                    symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, path.toUri().toString());
                    List<BLangPackage> bLangPackage = LSModuleCompiler.getBLangPackages(symbolsContext,
                            workspaceDocumentManager, true, false, false);
                    bLangPackage.forEach(aPackage -> aPackage.compUnits.forEach(compUnit -> {
                        String unitName = compUnit.getName();
                        String sourceRoot = LSCompilerUtil.getProjectRoot(path);
                        String basePath = sourceRoot + File.separator + compUnit.getPosition().src.getPackageName();
                        String hash = generateHash(compUnit, basePath);
                        compUnits.put(hash, new Object[]{
                                new File(basePath + File.separator + unitName).toURI(), compUnit});
                    }));
                }

                compUnits.values().forEach(compilationUnit -> {
                    symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);
                    symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, compilationUnit[0].toString());
                    symbolsContext.put(DocumentServiceKeys.SYMBOL_QUERY, params.getQuery());
                    SymbolFindingVisitor visitor = new SymbolFindingVisitor(symbolsContext);
                    ((BLangCompilationUnit) compilationUnit[1]).accept(visitor);
                });
            } catch (UserErrorException e) {
                notifyUser("Workspace Symbols", e);
            } catch (Throwable e) {
                String msg = "Operation 'workspace/symbol' failed!";
                logError(msg, e, null, (Position) null);
            }
            // Here we should extract only the Symbol information only.
            // TODO: Need to find a decoupled way to manage both with the same Symbol finding visitor
            return symbols.stream()
                    .filter(Either::isLeft).map(Either::getLeft)
                    .collect(Collectors.toList());
        });
    }

    private String generateHash(BLangCompilationUnit compUnit, String basePath) {
        return compUnit.getPosition().getSource().pkgID.toString() + "$" + basePath + "$" + compUnit.getName();
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        if (!(params.getSettings() instanceof JsonObject)) {
            return;
        }
        JsonObject settings = (JsonObject) params.getSettings();
        if (settings.get("ballerina") != null) {
            configHolder.updateConfig(GSON.fromJson(settings.get("ballerina"), LSClientConfig.class));
        } else {
            // To support old plugins versions
            configHolder.updateConfig(GSON.fromJson(settings, LSClientConfig.class));
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // Operation not supported
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            LSContext executeCmdContext = new WorkspaceServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.WS_EXEC_CMD)
                    .withExecuteCommandParams(params.getArguments(), workspaceDocumentManager, languageServer,
                            clientCapabilities)
                    .build();

            try {
                Optional<LSCommandExecutor> executor = LSCommandExecutorProvidersHolder.getInstance()
                        .getCommandExecutor(params.getCommand());
                if (executor.isPresent()) {
                    return executor.get().execute(executeCmdContext);
                }
            } catch (UserErrorException e) {
                notifyUser("Execute Command", e);
            } catch (Throwable e) {
                String msg = "Operation 'workspace/executeCommand' failed!";
                logError(msg, e, null, (Position) null);
            }
            logError("Operation 'workspace/executeCommand' failed!",
                    new LSCommandExecutorException("No command executor found for '" + params.getCommand() + "'"),
                    null, (Position) null);
            return false;
        });
    }
}
