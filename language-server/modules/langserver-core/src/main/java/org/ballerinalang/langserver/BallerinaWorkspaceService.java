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
import org.ballerinalang.langserver.client.config.BallerinaClientConfig;
import org.ballerinalang.langserver.client.config.BallerinaClientConfigHolder;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorProvider;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.symbols.SymbolFindingVisitor;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Workspace service implementation for Ballerina.
 */
public class BallerinaWorkspaceService implements WorkspaceService {
    private static final Logger logger = LoggerFactory.getLogger(BallerinaWorkspaceService.class);
    private BallerinaLanguageServer ballerinaLanguageServer;
    private WorkspaceDocumentManager workspaceDocumentManager;
    private DiagnosticsHelper diagnosticsHelper;
    private LSCompiler lsCompiler;
    private Map<String, Boolean> experimentalClientCapabilities;
    private static final Gson GSON = new Gson();
    private BallerinaClientConfigHolder configHolder = BallerinaClientConfigHolder.getInstance();

    BallerinaWorkspaceService(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.workspaceDocumentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
        this.diagnosticsHelper = globalContext.get(LSGlobalContextKeys.DIAGNOSTIC_HELPER_KEY);
        this.lsCompiler = new LSCompiler(workspaceDocumentManager);
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        List<Either<SymbolInformation, DocumentSymbol>> symbols = new ArrayList<>();
        LSServiceOperationContext symbolsContext = new LSServiceOperationContext();
        Map<String, Object[]> compUnits = new HashMap<>();
        this.workspaceDocumentManager.getAllFilePaths().forEach(path -> {
            symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);
            symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, path.toUri().toString());
            List<BLangPackage> bLangPackage = lsCompiler.getBLangPackages(symbolsContext, workspaceDocumentManager,
                                                                          false, LSCustomErrorStrategy.class, true);
            if (bLangPackage != null) {
                bLangPackage.forEach(aPackage -> aPackage.compUnits.forEach(compUnit -> {
                    String unitName = compUnit.getName();
                    String sourceRoot = LSCompilerUtil.getSourceRoot(path);
                    String basePath = sourceRoot + File.separator + compUnit.getPosition().src.getPackageName();
                    String hash = generateHash(compUnit, basePath);
                    compUnits.put(hash, new Object[]{
                            new File(basePath + File.separator + unitName).toURI(), compUnit});
                }));
            }
        });

        compUnits.values().forEach(compilationUnit -> {
            symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);
            symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, compilationUnit[0].toString());
            symbolsContext.put(DocumentServiceKeys.SYMBOL_QUERY, params.getQuery());
            SymbolFindingVisitor visitor = new SymbolFindingVisitor(symbolsContext);
            ((BLangCompilationUnit) compilationUnit[1]).accept(visitor);
        });
        // Here we should extract only the Symbol information only.
        // TODO: Need to find a decoupled way to manage both with the same Symbol finding visitor
        List<SymbolInformation> extractedSymbols = symbols.stream()
                .filter(Either::isLeft).map(Either::getLeft)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(extractedSymbols);
    }

    private String generateHash(BLangCompilationUnit compUnit, String basePath) {
        return compUnit.getPosition().getSource().pkgID.toString() + "$" + basePath + "$" + compUnit.getName();
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        if (!(params.getSettings() instanceof JsonObject)) {
            return;
        }
        configHolder.updateConfig(GSON.fromJson((JsonObject) params.getSettings(), BallerinaClientConfig.class));
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // Operation not supported
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        LSServiceOperationContext executeCommandContext = new LSServiceOperationContext();
        executeCommandContext.put(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY, params.getArguments());
        executeCommandContext.put(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY, this.workspaceDocumentManager);
        executeCommandContext.put(ExecuteCommandKeys.LANGUAGE_SERVER_KEY, this.ballerinaLanguageServer);
        executeCommandContext.put(ExecuteCommandKeys.LS_COMPILER_KEY, this.lsCompiler);
        executeCommandContext.put(ExecuteCommandKeys.DIAGNOSTICS_HELPER_KEY, this.diagnosticsHelper);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<LSCommandExecutor> executor = LSCommandExecutorProvider.getInstance()
                        .getCommandExecutor(params.getCommand());
                if (executor.isPresent()) {
                    return executor.get().execute(executeCommandContext);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            logger.warn("No command executor found for \"" + params.getCommand() + "\"");
            return false;
        });
    }

    /**
     * Sets experimental client capabilities.
     *
     * @param experimentalClientCapabilities a map of capabilities
     */
    public void setExperimentalClientCapabilities(Map<String, Boolean> experimentalClientCapabilities) {
        this.experimentalClientCapabilities = experimentalClientCapabilities;
    }

    /**
     * Returns experimental client capabilities.
     *
     * @return a map of capabilities
     */
    public Map<String, Boolean> getExperimentalClientCapabilities() {
        return this.experimentalClientCapabilities;
    }

    /**
     * Experimental capabilities.
     */
    public enum Experimental {
        INTROSPECTION("introspection"), SHOW_TEXT_DOCUMENT("showTextDocument");

        private final String value;

        Experimental(String value) {
            this.value = value;
        }

        /**
         * Returns value.
         *
         * @return value
         */
        public String getValue() {
            return value;
        }
    }
}
