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

import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utilities for the diagnostics related operations.
 *
 * @since 0.983.0
 */
public class DiagnosticsHelper {
    private final List<Diagnostic> emptyDiagnosticList = new ArrayList<>(0);
    private static final LanguageServerContext.Key<DiagnosticsHelper> DIAGNOSTICS_HELPER_KEY =
            new LanguageServerContext.Key<>();
    /**
     * Holds last sent diagnostics for the purpose of clear-off when publishing new diagnostics.
     */
    private Map<String, List<Diagnostic>> lastDiagnosticMap;

    public static DiagnosticsHelper getInstance(LanguageServerContext serverContext) {
        DiagnosticsHelper diagnosticsHelper = serverContext.get(DIAGNOSTICS_HELPER_KEY);
        if (diagnosticsHelper == null) {
            diagnosticsHelper = new DiagnosticsHelper(serverContext);
        }

        return diagnosticsHelper;
    }

    private DiagnosticsHelper(LanguageServerContext serverContext) {
        serverContext.put(DIAGNOSTICS_HELPER_KEY, this);
        this.lastDiagnosticMap = new HashMap<>();
    }

    /**
     * Compiles and publishes diagnostics for a source file.
     *
     * @param client  Language server client
     * @param context LS context
     */
    public synchronized void compileAndSendDiagnostics(ExtendedLanguageClient client, DocumentServiceContext context) {
        // Compile diagnostics
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return;
        }
        Map<String, List<Diagnostic>> diagnosticMap = getLatestDiagnostics(context);

        // If the client is null, returns
        if (client == null) {
            return;
        }

        // Clear old entries with an empty list
        lastDiagnosticMap.forEach((key, value) -> {
            if (!diagnosticMap.containsKey(key)) {
                client.publishDiagnostics(new PublishDiagnosticsParams(key, emptyDiagnosticList));
            }
        });

        // Publish diagnostics
        diagnosticMap.forEach((key, value) -> client.publishDiagnostics(new PublishDiagnosticsParams(key, value)));

        // Replace old map
        lastDiagnosticMap = diagnosticMap;
    }

    public Map<String, List<Diagnostic>> getLatestDiagnostics(DocumentServiceContext context) {
        WorkspaceManager workspace = context.workspace();
        Map<String, List<Diagnostic>> diagnosticMap = new HashMap<>();

        Optional<Project> project = workspace.project(context.filePath());
        if (project.isEmpty()) {
            return diagnosticMap;
        }
        // NOTE: We are not using `project.sourceRoot()` since it provides the single file project uses a temp path and
        // IDE requires the original path.
        Path projectRoot = workspace.projectRoot(context.filePath());
        if (project.get().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            projectRoot = projectRoot.getParent();
        }
        PackageCompilation compilation = workspace.waitAndGetPackageCompilation(context.filePath()).orElseThrow();
        // We do not send the internal diagnostics
        diagnosticMap.putAll(toDiagnosticsMap(compilation.diagnosticResult().diagnostics(false), projectRoot));
        return diagnosticMap;
    }

    private Map<String, List<Diagnostic>> toDiagnosticsMap(Collection<io.ballerina.tools.diagnostics.Diagnostic> diags,
                                                           Path projectRoot) {
        Map<String, List<Diagnostic>> diagnosticsMap = new HashMap<>();
        for (io.ballerina.tools.diagnostics.Diagnostic diag : diags) {
            LineRange lineRange = diag.location().lineRange();

            int startLine = lineRange.startLine().line();
            int startChar = lineRange.startLine().offset();
            int endLine = lineRange.endLine().line();
            int endChar = lineRange.endLine().offset();

            endLine = (endLine <= 0) ? startLine : endLine;
            endChar = (endChar <= 0) ? startChar + 1 : endChar;

            Range range = new Range(new Position(startLine, startChar), new Position(endLine, endChar));
            Diagnostic diagnostic = new Diagnostic(range, diag.message(), null, null, diag.diagnosticInfo().code());

            switch (diag.diagnosticInfo().severity()) {
                case ERROR:
                    diagnostic.setSeverity(DiagnosticSeverity.Error);
                    break;
                case WARNING:
                    diagnostic.setSeverity(DiagnosticSeverity.Warning);
                    break;
                case HINT:
                    diagnostic.setSeverity(DiagnosticSeverity.Hint);
                    break;
                case INFO:
                    diagnostic.setSeverity(DiagnosticSeverity.Information);
                    break;
                default:
                    break;
            }

            String fileURI = projectRoot.resolve(lineRange.filePath()).toUri().toString();
            List<Diagnostic> clientDiagnostics = diagnosticsMap.computeIfAbsent(fileURI, s -> new ArrayList<>());
            clientDiagnostics.add(diagnostic);
        }
        return diagnosticsMap;
    }
}
