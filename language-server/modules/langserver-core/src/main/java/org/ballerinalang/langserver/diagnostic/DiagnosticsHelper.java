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

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

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
    private static final DiagnosticsHelper INSTANCE = new DiagnosticsHelper();
    /**
     * Holds last sent diagnostics for the purpose of clear-off when publishing new diagnostics.
     */
    private Map<String, List<Diagnostic>> lastDiagnosticMap;

    public static DiagnosticsHelper getInstance() {
        return INSTANCE;
    }

    private DiagnosticsHelper() {
        this.lastDiagnosticMap = new HashMap<>();
    }

    /**
     * Compiles and publishes diagnostics for a source file.
     *
     * @param client     Language server client
     * @param context    LS context
     * @param lsDoc {@link LSDocumentIdentifierImpl}
     * @param docManager LS Document manager
     * @throws CompilationFailedException throws a LS compiler exception
     */
    public synchronized void compileAndSendDiagnostics(ExtendedLanguageClient client, LSContext context,
                                                       LSDocumentIdentifier lsDoc, WorkspaceDocumentManager docManager)
            throws CompilationFailedException {
        // Compile diagnostics
        List<BLangPackage> packages = LSModuleCompiler.getBLangPackages(context, docManager, true, true, true);
        Map<String, List<Diagnostic>> diagnosticMap = new HashMap<>();
        for (BLangPackage pkg : packages) {
            populateDiagnostics(diagnosticMap, pkg.packageID, pkg.getDiagnostics(), lsDoc);
        }

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

    private void populateDiagnostics(Map<String, List<Diagnostic>> diagnosticsMap, PackageID pkgId,
                                     List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics,
                                     LSDocumentIdentifier lsDocument) {
        for (io.ballerina.tools.diagnostics.Diagnostic diag : diagnostics) {
            Path diagnosticRoot = lsDocument.getProjectRootPath();
            Location location = diag.location();
            String moduleName = pkgId.getName().getValue();
            String fileName = location.lineRange().filePath();
            if (lsDocument.isWithinProject()) {
                diagnosticRoot = diagnosticRoot.resolve("src");
            }
            
            if (!".".equals(moduleName)) {
                diagnosticRoot = diagnosticRoot.resolve(moduleName);
            }
            String fileURI = diagnosticRoot.resolve(fileName).toUri().toString() + "";
            diagnosticsMap.putIfAbsent(fileURI, new ArrayList<>());

            LineRange lineRange = location.lineRange();
            int startLine = lineRange.startLine().line();
            int startChar = lineRange.startLine().offset();
            int endLine = lineRange.endLine().line();
            int endChar = lineRange.endLine().offset();

            endLine = (endLine <= 0) ? startLine : endLine;
            endChar = (endChar <= 0) ? startChar + 1 : endChar;

            Range range = new Range(new Position(startLine, startChar), new Position(endLine, endChar));
            Diagnostic diagnostic = new Diagnostic(range, diag.message());

            io.ballerina.tools.diagnostics.DiagnosticSeverity severity = diag.diagnosticInfo().severity();
            if (severity == io.ballerina.tools.diagnostics.DiagnosticSeverity.ERROR) {
                // set diagnostic log kind
                diagnostic.setSeverity(DiagnosticSeverity.Error);
            } else if (severity == io.ballerina.tools.diagnostics.DiagnosticSeverity.WARNING) {
                diagnostic.setSeverity(DiagnosticSeverity.Warning);
            }

            List<Diagnostic> clientDiagnostics = diagnosticsMap.get(fileURI);
            clientDiagnostics.add(diagnostic);
        }
    }
}
