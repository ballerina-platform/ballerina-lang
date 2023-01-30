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
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.WorkspaceServiceContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for the diagnostics related operations.
 *
 * @since 0.983.0
 */
public class DiagnosticsHelper {
    private final List<Diagnostic> emptyDiagnosticList = new ArrayList<>(0);
    private static final LanguageServerContext.Key<DiagnosticsHelper> DIAGNOSTICS_HELPER_KEY =
            new LanguageServerContext.Key<>();
    private static final long DIAGNOSTIC_DELAY = 1;
    /**
     * Holds last sent diagnostics for the purpose of clear-off when publishing new diagnostics.
     */
    private final Map<Path, Map<String, List<Diagnostic>>> lastDiagnosticMap;
    private CompletableFuture<Boolean> latestScheduled = null;

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
     * Schedule the diagnostics publishing.
     * In general the diagnostics publishing is done for document open, close and change events. When the document
     * change events are triggered frequently in subsequent edits, we do compilations and diagnostic calculation for
     * each of the change event. This is time-consuming for the large projects and from the user experience point of
     * view, we can publish the diagnostics after a delay. The default delay specified in {@link #DIAGNOSTIC_DELAY}
     *
     * @param client  Language client
     * @param context Document Service context.
     */
    public synchronized void schedulePublishDiagnostics(ExtendedLanguageClient client, DocumentServiceContext context) {
        WorkspaceManager workspaceManager = context.workspace();
        Path projectRoot = workspaceManager.projectRoot(context.filePath());
        compileAndSendDiagnostics(workspaceManager, projectRoot, client);
    }

    /**
     * Schedule the diagnostics publishing for a project specified with the given project root.
     * This particular diagnostics publishing API is used for publishing diagnostics through the workspace service.
     * This is time-consuming for the large projects and from the user experience point of
     * view, we can publish the diagnostics after a delay. The default delay specified in {@link #DIAGNOSTIC_DELAY}
     *
     * @param client      Language client
     * @param context     Workspace Service context
     * @param projectRoot project root
     */
    public synchronized void schedulePublishDiagnostics(ExtendedLanguageClient client,
                                                        WorkspaceServiceContext context,
                                                        Path projectRoot) {
        WorkspaceManager workspaceManager = context.workspace();
        compileAndSendDiagnostics(workspaceManager, projectRoot, client);
    }

    /**
     * Compiles and publishes diagnostics for a source file.
     * In order to avoid the unnecessary compilations, we will be scheduling the diagnostic compilations. Hence, instead
     * of this method, it is highly recommended to use
     * {@link #schedulePublishDiagnostics(ExtendedLanguageClient, DocumentServiceContext)}
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
        Map<String, List<Diagnostic>> latestDiagnostics = getLatestDiagnostics(context);

        // If the client is null, returns
        if (client == null) {
            return;
        }
        Map<String, List<Diagnostic>> lastProjectDiagnostics =
                lastDiagnosticMap.getOrDefault(project.get().sourceRoot(), new HashMap<>());

        // Clear old diagnostic entries of the project with an empty list
        lastProjectDiagnostics.forEach((key, value) -> {
            if (!latestDiagnostics.containsKey(key)) {
                client.publishDiagnostics(new PublishDiagnosticsParams(key, emptyDiagnosticList));
            }
        });

        // Publish diagnostics for the project
        latestDiagnostics.forEach((key, value) -> client.publishDiagnostics(new PublishDiagnosticsParams(key, value)));

        // Replace old diagnostic map associated with the project
        lastDiagnosticMap.put(project.get().sourceRoot(), latestDiagnostics);
    }

    /**
     * Compiles and publishes diagnostics for a project.
     *
     * @param client      Language server client
     * @param projectRoot project root
     * @param compilation package compilation
     */
    private synchronized void compileAndSendDiagnostics(ExtendedLanguageClient client, Path projectRoot,
                                                        PackageCompilation compilation,
                                                        WorkspaceManager workspaceManager) {
        Map<String, List<Diagnostic>> diagnosticMap =
                toDiagnosticsMap(compilation.diagnosticResult().diagnostics(false), projectRoot, workspaceManager);
        // If the client is null, returns
        if (client == null) {
            return;
        }
        Map<String, List<Diagnostic>> lastProjectDiagnostics =
                lastDiagnosticMap.getOrDefault(projectRoot, new HashMap<>());

        // Clear old diagnostic entries of the project with an empty list
        lastProjectDiagnostics.forEach((key, value) -> {
            if (!diagnosticMap.containsKey(key)) {
                client.publishDiagnostics(new PublishDiagnosticsParams(key, emptyDiagnosticList));
            }
        });

        // Publish diagnostics for the project
        diagnosticMap.forEach((key, value) -> client.publishDiagnostics(new PublishDiagnosticsParams(key, value)));

        // Replace old diagnostic map associated with the project
        lastDiagnosticMap.put(projectRoot, diagnosticMap);
    }

    public Map<String, List<Diagnostic>> getLatestDiagnostics(DocumentServiceContext context) {
        BallerinaWorkspaceManager workspace = (BallerinaWorkspaceManager) context.workspace();
        Map<String, List<Diagnostic>> diagnosticMap = new HashMap<>();

        Optional<Project> project = workspace.project(context.filePath());
        if (project.isEmpty()) {
            return diagnosticMap;
        }
        // NOTE: We are not using `project.sourceRoot()` since it provides the single file project uses a temp path and
        // IDE requires the original path.
        Path projectRoot = workspace.projectRoot(context.filePath());
        Path originalPath = project.get().kind() == ProjectKind.SINGLE_FILE_PROJECT
                ? projectRoot.getParent() : projectRoot;
        Optional<PackageCompilation> compilationResult = workspace.waitAndGetPackageCompilation(context.filePath(),
                context.operation() == LSContextOperation.TXT_DID_CHANGE);
        // We do not send the internal diagnostics
        compilationResult.ifPresent(compilation -> diagnosticMap.putAll(
                toDiagnosticsMap(compilation.diagnosticResult().diagnostics(false), originalPath, workspace)));
        return diagnosticMap;
    }

    private Map<String, List<Diagnostic>> toDiagnosticsMap(Collection<io.ballerina.tools.diagnostics.Diagnostic> diags,
                                                           Path projectRoot, WorkspaceManager workspaceManager) {
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

            /*
            If the project root is a directory, that means it is a build project and in the other case, a single 
            file project. So we only append the file URI for the build project case.
             */
            Path resolvedPath = projectRoot.toFile().isDirectory()
                    ? projectRoot.resolve(lineRange.filePath())
                    : projectRoot;
            String resolvedUri = resolvedPath.toUri().toString();
            String fileURI = PathUtil.getModifiedUri(workspaceManager, resolvedUri);
            List<Diagnostic> clientDiagnostics = diagnosticsMap.computeIfAbsent(fileURI, s -> new ArrayList<>());
            clientDiagnostics.add(diagnostic);
        }
        return diagnosticsMap;
    }

    private synchronized void compileAndSendDiagnostics(WorkspaceManager workspaceManager,
                                                        Path projectRoot,
                                                        ExtendedLanguageClient client) {
        if (latestScheduled != null && !latestScheduled.isDone()) {
            latestScheduled.completeExceptionally(new Throwable("Cancelled diagnostic publisher"));
        }

        Executor delayedExecutor = CompletableFuture.delayedExecutor(DIAGNOSTIC_DELAY, TimeUnit.SECONDS);
        CompletableFuture<Boolean> scheduledFuture = CompletableFuture.supplyAsync(() -> true, delayedExecutor);
        latestScheduled = scheduledFuture;
        scheduledFuture
                .thenApplyAsync((bool) -> workspaceManager.waitAndGetPackageCompilation(projectRoot))
                .thenAccept(compilation ->
                        compilation.ifPresent(pkgCompilation ->
                                compileAndSendDiagnostics(client, projectRoot, pkgCompilation, workspaceManager)));
    }
}
