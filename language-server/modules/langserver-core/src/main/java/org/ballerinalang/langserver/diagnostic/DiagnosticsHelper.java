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
package org.ballerinalang.langserver.diagnostic;

import org.ballerinalang.langserver.compiler.CollectDiagnosticListener;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for the diagnostics related operations.
 *
 * @since 0.983.0
 */
public class DiagnosticsHelper {
    private static final List<Diagnostic> EMPTY_DIAGNOSTIC_LIST = new ArrayList<>(0);
    /**
     * Holds last sent diagnostics for the purpose of clear-off when publishing new diagnostics.
     */
    private Map<String, List<Diagnostic>> lastDiagnosticMap;

    public DiagnosticsHelper() {
        this.lastDiagnosticMap = new HashMap<>();
    }

    /**
     * Compiles and publishes diagnostics for a source file.
     *
     * @param client     Language server client
     * @param lsCompiler LS Compiler
     * @param context    LS context
     * @param docManager LS Document manager
     * @throws LSCompilerException throws a LS compiler exception
     */
    public synchronized void compileAndSendDiagnostics(LanguageClient client, LSCompiler lsCompiler, LSContext context,
                                                       WorkspaceDocumentManager docManager) throws LSCompilerException {
        // Compile diagnostics
        List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics = new ArrayList<>();
        LSDocument lsDocument = new LSDocument(context.get(DocumentServiceKeys.FILE_URI_KEY));
        lsCompiler.getBLangPackages(context, docManager, true, null, true, true);
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        if (compilerContext.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
             diagnostics = ((CollectDiagnosticListener) compilerContext.get(DiagnosticListener.class)).getDiagnostics();
        }

        Map<String, List<Diagnostic>> diagnosticMap = getDiagnostics(diagnostics, lsDocument);
        // If the client is null, returns
        if (client == null) {
            return;
        }

        // Replace old entries with an empty list
        lastDiagnosticMap.keySet().forEach((key) -> diagnosticMap.computeIfAbsent(key, value -> EMPTY_DIAGNOSTIC_LIST));

        // Publish diagnostics
        diagnosticMap.forEach((key, value) -> client.publishDiagnostics(new PublishDiagnosticsParams(key, value)));

        // Replace old map
        lastDiagnosticMap = diagnosticMap;
    }

    /**
     * Returns diagnostics for this file.
     *
     * @param diagnostics  List of ballerina diagnostics
     * @param lsDocument project path
     * @return diagnostics map
     */
    private Map<String, List<Diagnostic>> getDiagnostics(List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics,
                                                         LSDocument lsDocument) {
        Map<String, List<Diagnostic>> diagnosticsMap = new HashMap<>();
        for (org.ballerinalang.util.diagnostic.Diagnostic diag : diagnostics) {
            Path diagnosticRoot = lsDocument.getProjectRootPath();
            final org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition position = diag.getPosition();
            String moduleName = position.getSource().getPackageName();
            String fileName = position.getSource().getCompilationUnitName();
            if (lsDocument.isWithinProject()) {
                diagnosticRoot = diagnosticRoot.resolve("src");
            }
            if (!".".equals(moduleName)) {
                diagnosticRoot = diagnosticRoot.resolve(moduleName);
            }
            String fileURI = diagnosticRoot.resolve(fileName).toUri().toString() + "";
            diagnosticsMap.putIfAbsent(fileURI, new ArrayList<>());

            List<Diagnostic> clientDiagnostics = diagnosticsMap.get(fileURI);
            int startLine = position.getStartLine() - 1; // LSP diagnostics range is 0 based
            int startChar = position.getStartColumn() - 1;
            int endLine = position.getEndLine() - 1;
            int endChar = position.getEndColumn() - 1;

            endLine = (endLine <= 0) ? startLine : endLine;
            endChar = (endChar <= 0) ? startChar + 1 : endChar;

            Range range = new Range(new Position(startLine, startChar), new Position(endLine, endChar));
            Diagnostic diagnostic = new Diagnostic(range, diag.getMessage());
            diagnostic.setSeverity(DiagnosticSeverity.Error);
            clientDiagnostics.add(diagnostic);
        }
        return diagnosticsMap;
    }
}
