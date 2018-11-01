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

import org.ballerinalang.langserver.command.CommandExecutor;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
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
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Workspace service implementation for Ballerina.
 */
class BallerinaWorkspaceService implements WorkspaceService {
    private BallerinaLanguageServer ballerinaLanguageServer;
    private WorkspaceDocumentManager workspaceDocumentManager;
    private DiagnosticsHelper diagnosticsHelper;
    private LSGlobalContext lsGlobalContext;
    private LSCompiler lsCompiler;

    BallerinaWorkspaceService(LSGlobalContext globalContext) {
        this.lsGlobalContext = globalContext;
        this.ballerinaLanguageServer = this.lsGlobalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.workspaceDocumentManager = this.lsGlobalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
        this.diagnosticsHelper = this.lsGlobalContext.get(LSGlobalContextKeys.DIAGNOSTIC_HELPER_KEY);
        this.lsCompiler = new LSCompiler(workspaceDocumentManager);
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        List<SymbolInformation> symbols = new ArrayList<>();
        LSServiceOperationContext symbolsContext = new LSServiceOperationContext();
        Map<String, Object[]> compUnits = new HashMap<>();
        this.workspaceDocumentManager.getAllFilePaths().forEach(path -> {
            symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);
            symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, path.toUri().toString());
            List<BLangPackage> bLangPackage = lsCompiler.getBLangPackage(symbolsContext, workspaceDocumentManager,
                                                                         false,
                                                                         LSCustomErrorStrategy.class, true).getLeft();
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
        return CompletableFuture.completedFuture(symbols);
    }

    private String generateHash(BLangCompilationUnit compUnit, String basePath) {
        return compUnit.getPosition().getSource().pkgID.toString() + "$" + basePath + "$" + compUnit.getName();
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // Operation not supported
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

        return CompletableFuture.supplyAsync(() -> CommandExecutor.executeCommand(params, executeCommandContext));
    }
}
