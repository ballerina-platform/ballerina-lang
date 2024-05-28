/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.workspace;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.BalToolToml;
import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.CloudToml;
import io.ballerina.projects.CompilerPluginToml;
import io.ballerina.projects.DependenciesToml;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleCompilation;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.eventsync.EventSyncPubSubHolder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MODULE_INIT_CLASS_NAME;

/**
 * Contains a set of utility methods to manage projects.
 *
 * @since 2.0.0
 */
public class BallerinaWorkspaceManager implements WorkspaceManager {

    /**
     * Cache mapping of document path to source root.
     */
    private final Map<Path, Path> pathToSourceRootCache;
    /**
     * Mapping of source root to project instance.
     */
    protected final Map<Path, ProjectContext> sourceRootToProject;
    protected final LSClientLogger clientLogger;
    private final LanguageServerContext serverContext;
    private final Set<Path> openedDocuments = new HashSet<>();

    public BallerinaWorkspaceManager(LanguageServerContext serverContext) {
        this.serverContext = serverContext;
        this.clientLogger = LSClientLogger.getInstance(serverContext);
        Cache<Path, Path> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
        this.pathToSourceRootCache = cache.asMap();
        this.sourceRootToProject = new SourceRootToProjectMap<>(pathToSourceRootCache);

        // We are only doing a best effort cleanup here. If we held a strong reference to the map
        // GC will not be able to clean the projects. It impacts tests since all run in the same JVM.
        WeakReference<Map<Path, ProjectContext>> weekMap = new WeakReference<>(sourceRootToProject);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Map<Path, ProjectContext> map = weekMap.get();
            if (map == null) {
                return;
            }
            for (ProjectContext projectContext : map.values()) {
                // Since we are anyway shutting down no need to acquire locks for each
                projectContext.process().ifPresent(Process::destroy);
            }
        }));
    }

    @Override
    public Optional<String> relativePath(Path path) {
        Optional<Document> document = this.document(path);
        return document.map(Document::name);
    }

    @Override
    public Optional<String> relativePath(Path path, @Nonnull CancelChecker cancelChecker) {
        Optional<Document> document = this.document(path, cancelChecker);
        return document.map(Document::name);
    }

    /**
     * Returns a project root from the path provided.
     *
     * @param filePath ballerina project or standalone file path
     * @return project root
     */
    public Path projectRoot(Path filePath) {
        return pathToSourceRootCache.computeIfAbsent(filePath, this::computeProjectRoot);
    }

    @Override
    public Path projectRoot(Path filePath, @Nonnull CancelChecker cancelChecker) {
        cancelChecker.checkCanceled();
        return this.projectRoot(filePath);
    }

    /**
     * Returns project from the path provided.
     *
     * @param filePath ballerina project or standalone file path
     * @return project of applicable type
     */
    @Override
    public Optional<Project> project(Path filePath) {
        return projectContext(projectRoot(filePath)).map(ProjectContext::project);
    }

    /**
     * Loads the project from the path provided.
     *
     * @param filePath ballerina project or standalone file path
     * @return project of applicable type
     */
    @Override
    public Project loadProject(Path filePath) throws ProjectException, WorkspaceDocumentException, EventSyncException {
        Project project;
        Optional<Project> optionalProject = project(filePath);

        if (optionalProject.isPresent()) {
            project = optionalProject.get();
        } else {
            project = createOrGetProjectPair(filePath, LSContextOperation.LOAD_PROJECT.getName()).project();

            BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
            DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(
                    filePath.toUri().toString(),
                    languageServer.getWorkspaceManager(),
                    LSContextOperation.LOAD_PROJECT, this.serverContext);
            EventSyncPubSubHolder.getInstance(this.serverContext)
                    .getPublisher(EventKind.PROJECT_UPDATE)
                    .publish(languageServer.getClient(), this.serverContext, context);
        }

        return project;
    }

    /**
     * Returns module from the path provided.
     *
     * @param filePath file path of the document
     * @return project of applicable type
     */
    @Override
    public Optional<Module> module(Path filePath) {
        Optional<Project> project = project(filePath);
        if (project.isEmpty()) {
            return Optional.empty();
        }
        Optional<Document> document = document(filePath, project.get(), null);
        if (document.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(document.get().module());
    }

    @Override
    public Optional<Module> module(Path filePath, @Nonnull CancelChecker cancelChecker) {
        cancelChecker.checkCanceled();
        Optional<Project> project = project(filePath);
        if (project.isEmpty()) {
            return Optional.empty();
        }
        Optional<Document> document = document(filePath, project.get(), cancelChecker);
        if (document.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(document.get().module());
    }

    /**
     * Returns document of the project of this path.
     *
     * @param filePath file path of the document
     * @return {@link Document}
     */
    @Override
    public Optional<Document> document(Path filePath) {
        Optional<Project> project = project(filePath);
        return project.isPresent() ? document(filePath, project.get(), null) : Optional.empty();
    }

    @Override
    public Optional<Document> document(Path filePath, @Nonnull CancelChecker cancelChecker) {
        Optional<Project> project = project(filePath);
        return project.isPresent() ? document(filePath, project.get(), cancelChecker) : Optional.empty();
    }

    /**
     * Returns syntax tree from the path provided.
     *
     * @param filePath file path of the document
     * @return {@link io.ballerina.compiler.syntax.tree.SyntaxTree}
     */
    @Override
    public Optional<SyntaxTree> syntaxTree(Path filePath) {
        Optional<Document> document = this.document(filePath);
        if (document.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(document.get().syntaxTree());
    }

    @Override
    public Optional<SyntaxTree> syntaxTree(Path filePath, @Nonnull CancelChecker cancelChecker) {
        Optional<Document> document = this.document(filePath, cancelChecker);
        if (document.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(document.get().syntaxTree());
    }

    /**
     * Returns semantic model from the path provided.
     *
     * @param filePath file path of the document
     * @return {@link SemanticModel}
     */
    @Override
    public Optional<SemanticModel> semanticModel(Path filePath) {
        Optional<Module> module = this.module(filePath);
        Optional<PackageCompilation> packageCompilation = waitAndGetPackageCompilation(filePath);
        Optional<ProjectContext> projectPair = projectContext(projectRoot(filePath));
        if (module.isEmpty() || packageCompilation.isEmpty() || projectPair.isEmpty()
                || projectPair.get().compilationCrashed()) {
            return Optional.empty();
        }
        return Optional.of(packageCompilation.get().getSemanticModel(module.get().moduleId()));
    }

    @Override
    public Optional<SemanticModel> semanticModel(Path filePath, @Nonnull CancelChecker cancelChecker) {
        Optional<Module> module = this.module(filePath);
        Optional<PackageCompilation> packageCompilation = waitAndGetPackageCompilation(filePath, cancelChecker);
        Optional<ProjectContext> projectPair = projectContext(projectRoot(filePath));
        if (module.isEmpty() || packageCompilation.isEmpty() || projectPair.isEmpty()
                || projectPair.get().compilationCrashed()) {
            return Optional.empty();
        }
        return Optional.of(packageCompilation.get().getSemanticModel(module.get().moduleId()));
    }

    /**
     * Returns module compilation from the file path provided.
     *
     * @param filePath       file path of the document
     * @param isSourceChange True if the given file's source is changed
     * @return {@link ModuleCompilation}
     */
    public Optional<PackageCompilation> waitAndGetPackageCompilation(Path filePath, boolean isSourceChange) {
        // Get Project and Lock
        Optional<ProjectContext> projectPair = projectContext(projectRoot(filePath));
        if (projectPair.isEmpty() || (projectPair.get().compilationCrashed() && !isSourceChange)) {
            return Optional.empty();
        }

        // Lock Project Instance
        Lock lock = projectPair.get().lockAndGet();
        try {
            PackageCompilation compilation = projectPair.get().project().currentPackage().getCompilation();
            if (projectPair.get().compilationCrashed()) {
                projectPair.get().setCompilationCrashed(false);
            }
            if (compilation.diagnosticResult().diagnostics().stream()
                    .anyMatch(diagnostic ->
                            Arrays.asList(DiagnosticErrorCode.BAD_SAD_FROM_COMPILER.diagnosticId(),
                                            DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.diagnosticId())
                                    .contains(diagnostic.diagnosticInfo().code()))) {
                projectPair.get().setCompilationCrashed(true);
                projectPair.get().project().clearCaches();
            }
            return Optional.of(compilation);
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    /**
     * Returns module compilation from the file path provided.
     *
     * @param filePath file path of the document
     * @return {@link ModuleCompilation}
     */
    @Override
    public Optional<PackageCompilation> waitAndGetPackageCompilation(Path filePath) {
        return waitAndGetPackageCompilation(filePath, false);
    }

    @Override
    public Optional<PackageCompilation> waitAndGetPackageCompilation(Path filePath,
                                                                     @Nonnull CancelChecker cancelChecker) {
        cancelChecker.checkCanceled();
        return waitAndGetPackageCompilation(filePath);
    }

    /**
     * The document open notification is sent from the client to the server to signal newly opened text documents.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidOpenTextDocumentParams}
     */
    @Override
    public void didOpen(Path filePath, DidOpenTextDocumentParams params) throws WorkspaceDocumentException {
        // Add the document to the opened documents set and the entry will only be removed via didClose.
        // Hence we assume the safe concurrent access for a given document path
        this.openedDocuments.add(filePath);
        ProjectContext projectContext = createOrGetProjectPair(filePath,
                LSContextOperation.TXT_DID_OPEN.getName(), true);
        Project project = projectContext.project();
        if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML))) {
            // Create or update Ballerina.toml
            updateBallerinaToml(params.getTextDocument().getText(), projectContext, true);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.DEPENDENCIES_TOML))) {
            // Create or update Dependencies.toml
            updateDependenciesToml(params.getTextDocument().getText(), projectContext, true);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.CLOUD_TOML))) {
            // Create or update Cloud.toml
            updateCloudToml(params.getTextDocument().getText(), projectContext, true);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.COMPILER_PLUGIN_TOML))) {
            // Create or update Compiler-plugin.toml
            updateCompilerPluginToml(params.getTextDocument().getText(), projectContext, true);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.BAL_TOOL_TOML))) {
            // Create or update BalTool.toml
            updateBalToolToml(params.getTextDocument().getText(), projectContext, true);
        } else if (ProjectPaths.isBalFile(filePath) && project.kind() != ProjectKind.BALA_PROJECT) {
            // Create a new .bal document.
            createBalDocument(filePath, params.getTextDocument().getText(), projectContext);
        }
    }

    /**
     * The document change notification is sent from the client to the server to signal changes to a text document.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidChangeTextDocumentParams}
     * @throws WorkspaceDocumentException when project or document not found
     */
    @Override
    public void didChange(Path filePath, DidChangeTextDocumentParams params) throws WorkspaceDocumentException {
        // Get Project and Lock
        ProjectContext projectContext = createOrGetProjectPair(filePath,
                LSContextOperation.TXT_DID_CHANGE.getName(), true);

        Project project = projectContext.project();
        if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML))) {
            // Update Ballerina.toml
            updateBallerinaToml(params.getContentChanges().get(0).getText(), projectContext, false);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.DEPENDENCIES_TOML))) {
            // create or update Dependencies.toml
            updateDependenciesToml(params.getContentChanges().get(0).getText(), projectContext, false);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.CLOUD_TOML))) {
            // create or update Cloud.toml
            updateCloudToml(params.getContentChanges().get(0).getText(), projectContext, false);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.COMPILER_PLUGIN_TOML))) {
            // create or update Compiler-plugin.toml
            updateCompilerPluginToml(params.getContentChanges().get(0).getText(), projectContext, false);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.BAL_TOOL_TOML))) {
            // create or update BalTool.toml
            updateBalToolToml(params.getContentChanges().get(0).getText(), projectContext, false);
        } else if (ProjectPaths.isBalFile(filePath) && project.kind() != ProjectKind.BALA_PROJECT) {
            // Update .bal document
            updateBalDocument(filePath, params.getContentChanges().get(0).getText(), projectContext);
        }
    }

    /**
     * The file change notification is sent from the client to the server to signal changes to watched files.
     *
     * @param filePath  {@link Path} of the document
     * @param fileEvent {@link FileEvent}
     * @throws WorkspaceDocumentException when project or document not found
     */
    @Override
    public void didChangeWatched(Path filePath, FileEvent fileEvent) throws WorkspaceDocumentException {
        if (!LSClientConfigHolder.getInstance(serverContext).getConfig().isEnableFileWatcher()) {
            return;
        }
        Optional<ProjectContext> optProject = getProjectOfWatchedFileChange(filePath, fileEvent);
        if (optProject.isEmpty()) {
            clientLogger.logTrace(
                    String.format("Operation '%s' No matching project found, {fileUri: '%s' event: '%s'} ignored",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri(),
                            fileEvent.getType().name()));
            return;
        }
        ProjectContext projectContext = optProject.get();
        Project project = projectContext.project();
        String fileName = filePath.getFileName().toString();
        boolean isBallerinaSourceChange = fileName.endsWith(ProjectConstants.BLANG_SOURCE_EXT);
        boolean isBallerinaTomlChange = filePath.endsWith(ProjectConstants.BALLERINA_TOML);
        boolean isDependenciesTomlChange = filePath.endsWith(ProjectConstants.DEPENDENCIES_TOML);
        boolean isCloudTomlChange = filePath.endsWith(ProjectConstants.CLOUD_TOML);
        boolean isCompilerPluginTomlChange = filePath.endsWith(ProjectConstants.COMPILER_PLUGIN_TOML);
        boolean isBalToolTomlChange = filePath.endsWith(ProjectConstants.BAL_TOOL_TOML);
        if (fileEvent.getType() == FileChangeType.Created &&
                (isBallerinaSourceChange || isBallerinaTomlChange || isCloudTomlChange || isCompilerPluginTomlChange
                        || isBalToolTomlChange)
                && hasDocumentOrToml(filePath, project)) {
            // Document might already exists when text/didOpen hits before workspace/didChangeWatchedFiles,
            // Thus, return silently
            clientLogger.logTrace(
                    String.format("Operation '%s' File already exits, {fileUri: '%s' event: '%s'} ignored",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri(),
                            fileEvent.getType().name()));
            return;
        }

        if (isBallerinaSourceChange) {
            handleWatchedBalSourceChange(filePath, fileEvent, projectContext);
        } else if (isBallerinaTomlChange) {
            handleWatchedBallerinaTomlChange(filePath, fileEvent, projectContext);
        } else if (isCloudTomlChange) {
            handleWatchedCloudTomlChange(filePath, fileEvent, projectContext);
        } else if (isDependenciesTomlChange) {
            handleWatchedDependenciesTomlChange(filePath, fileEvent, projectContext);
        } else if (isCompilerPluginTomlChange) {
            handleWatchedCompilerPluginTomlChange(filePath, fileEvent, projectContext);
        } else if (isBalToolTomlChange) {
            handleWatchedBalToolTomlChange(filePath, fileEvent, projectContext);
        } else {
            handleWatchedModuleChange(filePath, fileEvent, projectContext);
        }
    }

    @Override
    public List<Path> didChangeWatched(DidChangeWatchedFilesParams params) throws WorkspaceDocumentException {
        if (!LSClientConfigHolder.getInstance(serverContext).getConfig().isEnableFileWatcher()) {
            return Collections.emptyList();
        }
        List<FileEvent> changes = params.getChanges();
        if (changes.size() == 1) {
            FileEvent fileEvent = changes.get(0);
            String uri = fileEvent.getUri();
            Optional<Path> pathFromURI = PathUtil.getPathFromURI(uri);
            if (pathFromURI.isEmpty()) {
                return Collections.emptyList();
            }
            Path filePath = pathFromURI.get();
            if (!this.openedDocuments.contains(filePath) || fileEvent.getType() == FileChangeType.Deleted) {
                // If already opened in the cache, this will be captured via the textDocument/didChange event
                this.didChangeWatched(filePath, fileEvent);
                Optional<ProjectContext> optProject = getProjectOfWatchedFileChange(filePath, fileEvent);
                if (optProject.isPresent()) {
                    ProjectContext projectContext = optProject.get();
                    Project project = projectContext.project();
                    return List.of(project.sourceRoot());
                }
            }
            return Collections.emptyList();
        }

        Set<Path> reloadableProjects = new HashSet<>();
        for (FileEvent fileEvent : changes) {
            String uri = fileEvent.getUri();
            Optional<Path> pathFromURI = PathUtil.getPathFromURI(uri);

            if (pathFromURI.isEmpty()) {
                return Collections.emptyList();
            }
            Path filePath = pathFromURI.get();

            try {
                reloadableProjects.add(ProjectPaths.packageRoot(filePath));
            } catch (ProjectException e) {
                // ignore the project exception which can be thrown when path identification is failed
            }
        }

        reloadableProjects.forEach(path -> {
            Optional<ProjectContext> projectPair = this.projectContext(path);
            if (projectPair.isEmpty()) {
                return;
            }
            Lock lock = projectPair.get().lockAndGet();
            try {
                Optional<ProjectContext> projectContext =
                        createProjectContext(path, LSContextOperation.WS_WF_CHANGED.getName());
                if (projectContext.isEmpty()) {
                    // NOTE: This will never happen since we create a project if not exists
                    throw new WorkspaceDocumentException("Cannot find the project of uri: " + path.toString());
                }
                projectPair.get().setProject(projectContext.get().project());
            } catch (Throwable e) {
                // Failed to reload the project
                String message = "Failed to reload project: ["
                        + projectPair.get().project().sourceRoot().toString() + "]";
                clientLogger.logError(LSContextOperation.WS_WF_CHANGED, message, e, null, (Position) null);
            } finally {
                lock.unlock();
            }
        });
        return new ArrayList<>(reloadableProjects);
    }

    @Override
    public String uriScheme() {
        return "file";
    }

    @Override
    public Optional<Process> run(Path filePath) throws IOException {
        Optional<ProjectContext> projectPairOpt = projectContext(projectRoot(filePath));
        if (projectPairOpt.isEmpty()) {
            String msg = "Run command execution aborted because project is not loaded";
            UserErrorException e = new UserErrorException(msg);
            clientLogger.logError(LSContextOperation.WS_EXEC_CMD, msg, e, null, (Position) null);
            return Optional.empty();
        }
        ProjectContext projectContext = projectPairOpt.get();
        if (!stopProject(projectContext)) {
            String msg = "Run command execution aborted because couldn't stop the previous run";
            UserErrorException e = new UserErrorException(msg);
            clientLogger.logError(LSContextOperation.WS_EXEC_CMD, msg, e, null, (Position) null);
            return Optional.empty();
        }

        Project project = projectContext.project();
        Package pkg = project.currentPackage();
        Module executableModule = pkg.getDefaultModule();
        Optional<PackageCompilation> packageCompilation = waitAndGetPackageCompilation(project.sourceRoot(), true);
        if (packageCompilation.isEmpty()) {
            return Optional.empty();
        }
        JBallerinaBackend jBallerinaBackend = execBackend(projectContext, packageCompilation.get());
        Collection<Diagnostic> diagnostics = jBallerinaBackend.diagnosticResult().diagnostics(false);
        if (diagnostics.stream().anyMatch(BallerinaWorkspaceManager::isError)) {
            String msg = "Run command execution aborted due to compilation errors: " + diagnostics;
            UserErrorException e = new UserErrorException(msg);
            clientLogger.logError(LSContextOperation.WS_EXEC_CMD, msg, e, null, (Position) null);
            return Optional.empty();
        }
        JarResolver jarResolver = jBallerinaBackend.jarResolver();
        String initClassName = JarResolver.getQualifiedClassName(
                executableModule.packageInstance().packageOrg().toString(),
                executableModule.packageInstance().packageName().toString(),
                executableModule.packageInstance().packageVersion().toString(),
                MODULE_INIT_CLASS_NAME);
        List<String> commands = new ArrayList<>();
        commands.add(System.getProperty("java.command"));
        commands.add("-XX:+HeapDumpOnOutOfMemoryError");
        commands.add("-XX:HeapDumpPath=" + System.getProperty(USER_DIR));
        commands.add("-cp");
        commands.add(getAllClassPaths(jarResolver));
        commands.add(initClassName);
        ProcessBuilder pb = new ProcessBuilder(commands);

        Lock lock = projectContext.lockAndGet();
        try {
            Optional<Process> existing = projectContext.process();
            if (existing.isPresent()) {
                // We just removed this in above `stopProject`. This means there is a parallel command running.
                String msg = "Run command execution aborted because another run is in progress";
                UserErrorException e = new UserErrorException(msg);
                clientLogger.logError(LSContextOperation.WS_EXEC_CMD, msg, e, null, (Position) null);
                return Optional.empty();
            }
            Process ps = pb.start();
            projectContext.setProcess(ps);
            return Optional.of(ps);
        } finally {
            lock.unlock();
        }
    }

    private static JBallerinaBackend execBackend(ProjectContext projectContext,
                                                 PackageCompilation packageCompilation) {
        Lock lock = projectContext.lockAndGet();
        try {
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17, false);
            Package pkg = projectContext.project.currentPackage();
            for (Module module : pkg.modules()) {
                for (DocumentId id : module.documentIds()) {
                    module.document(id).modify().apply();
                }
            }
            return jBallerinaBackend;
        } finally {
            lock.unlock();
        }
    }


    @Override
    public boolean stop(Path filePath) {
        Optional<ProjectContext> projectPairOpt = projectContext(projectRoot(filePath));
        if (projectPairOpt.isEmpty()) {
            clientLogger.logWarning("Failed to stop process: Project not found");
            return false;
        }
        ProjectContext projectContext = projectPairOpt.get();
        return stopProject(projectContext);
    }

    @Override
    public CompletableFuture<Map<Path, Project>> workspaceProjects() {
        ExtendedLanguageClient extendedLanguageClient = serverContext.get(ExtendedLanguageClient.class);
        CompletableFuture<List<WorkspaceFolder>> future = extendedLanguageClient.workspaceFolders();
        return future.thenApply(workspaceFolders -> {
            Map<Path, Project> filteredProjects = new HashMap<>();
            workspaceFolders.forEach(workspaceFolder -> {
                Path workspaceFolderPath = Path.of(URI.create(workspaceFolder.getUri()));
                sourceRootToProject.entrySet().stream()
                        .filter(pathProjectContextEntry -> pathProjectContextEntry.getKey().toAbsolutePath()
                                .startsWith(workspaceFolderPath))
                        .forEach(pathProjectContextEntry ->
                                filteredProjects.put(pathProjectContextEntry.getKey(),
                                        pathProjectContextEntry.getValue().project()));
            });
            return filteredProjects;
        });
    }

    private boolean stopProject(ProjectContext projectContext) {
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<Process> existing = projectContext.process();
            if (existing.isEmpty()) {
                return true;
            }
            boolean killed = killProcess(existing.get());
            if (killed) {
                projectContext.removeProcess();
            }
            return killed;
        } finally {
            lock.unlock();
        }
    }

    private boolean killProcess(Process process) {
        process.destroy();
        try {
            process.waitFor(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            clientLogger.logWarning("Waiting for process to stop was interrupted");
            Thread.currentThread().interrupt();
        }
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        return !process.isAlive();
    }

    private String getAllClassPaths(JarResolver jarResolver) {
        StringJoiner cp = new StringJoiner(File.pathSeparator);
        for (JarLibrary lib : jarResolver.getJarFilePathsRequiredForExecution()) {
            cp.add(lib.path().toString());
        }
        return cp.toString();
    }

    /**
     * Refresh the project by cloning it internally and clearing caches.
     *
     * @param filePath A path of a file in the project
     */
    public void refreshProject(Path filePath) throws WorkspaceDocumentException {
        Optional<ProjectContext> projectPairOpt = projectContext(projectRoot(filePath));
        if (projectPairOpt.isEmpty()) {
            throw new WorkspaceDocumentException("Project not found for filePath: " + filePath);
        }

        Lock lock = projectPairOpt.get().lockAndGet();
        try {
            projectPairOpt.get().project().clearCaches();
        } finally {
            lock.unlock();
        }
    }

    private Optional<ProjectContext> projectOfWatchedFileChange(Path filePath, FileEvent fileEvent,
                                                                boolean isBallerinaSourceChange,
                                                                boolean isBallerinaTomlChange,
                                                                boolean isDependenciesTomlChange,
                                                                boolean isCloudTomlChange,
                                                                boolean isCompilerPluginTomlChange,
                                                                boolean isBalToolTomlChange,
                                                                boolean isModuleChange) {
        if (isBallerinaSourceChange) {
            if (fileEvent.getType() == FileChangeType.Created) {
                return projectContext(projectRoot(filePath));
            } else {
                // DELETED event
                // First try as a single-file-project
                Optional<ProjectContext> optProject = projectContext(filePath);
                if (optProject.isPresent()) {
                    return optProject;
                }
                // Or Else, try as a build-project
                Path parent = filePath.getParent();
                if (ProjectConstants.TEST_DIR_NAME.equals(parent.getFileName().toString())) {
                    // If inside a tests folder, get parent
                    parent = parent.getParent();
                }
                if (ProjectConstants.MODULES_ROOT.equals(parent.getParent().getFileName().toString()) ||
                        ProjectConstants.GENERATED_MODULES_ROOT.equals(parent.getParent().getFileName().toString())) {
                    // If inside a modules or generated folder, get parent of parent
                    parent = parent.getParent().getParent();
                }
                if (ProjectConstants.GENERATED_MODULES_ROOT.equals(parent.getFileName().toString())) {
                    // If a generated source for a non-default module, get parent of parent
                    parent = parent.getParent();
                }
                return projectContext(parent);
            }
        } else if (isBallerinaTomlChange) {
            if (fileEvent.getType() == FileChangeType.Created) {
                // Check for a project upgrade from a single-file to a build-project
                // In such scenario, project will be only available with the key of that single file path.
                Optional<ProjectContext> optProject = sourceRootToProject.entrySet().stream()
                        .filter(entry -> entry.getValue().project().kind() == ProjectKind.SINGLE_FILE_PROJECT &&
                                entry.getKey().getParent().equals(filePath.getParent()))
                        .findFirst()
                        .map(Map.Entry::getValue);
                if (optProject.isEmpty()) {
                    // Single-file project is unavailable if we just downgraded a build-project removing Ballerina.toml
                    // Thus, loading a new build-project here
                    optProject = createProjectContext(filePath, LSContextOperation.WS_WF_CHANGED.getName());
                    sourceRootToProject.put(optProject.get().project().sourceRoot(), optProject.get());

                }
                return optProject;
            } else {
                // Check for a project downgrade from a build-project to a single-file
                return projectContext(filePath.getParent());
            }
        } else if (isCloudTomlChange || isCompilerPluginTomlChange || isBalToolTomlChange || isDependenciesTomlChange) {
            return projectContext(filePath.getParent());
        } else if (isModuleChange) {
            Path projectRoot;
            if (ProjectConstants.MODULES_ROOT.equals(filePath.getFileName().toString()) ||
                    ProjectConstants.GENERATED_MODULES_ROOT.equals(filePath.getFileName().toString())) {
                // If it is **/projectRoot/modules OR **/projectRoot/generated
                projectRoot = filePath.getParent();
            } else {
                // If it is **/projectRoot/modules/mod2 OR **/projectRoot/generated/mod2
                projectRoot = filePath.getParent().getParent();
            }
            return projectContext(projectRoot);
        } else {
            // Skip if unrecognized file change
            return Optional.empty();
        }
    }

    private void handleWatchedBalSourceChange(Path filePath, FileEvent fileEvent, ProjectContext projectContext) {
        switch (fileEvent.getType()) {
            case Created: {
                // Creating new document requires finding the module it resides
                // Thus, reloading the project
                reloadProject(projectContext, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                break;
            }
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectContext, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted: {
                Project project = projectContext.project();
                if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                    // If it is a single-file-project, remove project from mapping
                    Path projectRoot = project.sourceRoot();
                    sourceRootToProject.remove(projectRoot);
                    clientLogger.logTrace(String.format("Operation '%s' {project: '%s' kind: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            projectRoot.toUri().toString(),
                            project.kind().name()
                                    .toLowerCase(Locale.getDefault())));
                } else {
                    // If it is a build-project, need to remove particular file from project
                    Optional<Document> document = document(filePath, project, null);
                    if (document.isPresent()) {
                        Lock lock = projectContext.lockAndGet();
                        try {
                            Project updatedProj = document.get().module().modify().removeDocument(
                                    document.get().documentId()).apply().project();
                            projectContext.setProject(updatedProj);
                            clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                                    LSContextOperation.WS_WF_CHANGED.getName(),
                                    fileEvent.getUri()));
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        // If document-id not found, reload project
                        Path ballerinaTomlPath = project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML);
                        reloadProject(projectContext, ballerinaTomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                    }
                }
            }
        }
    }

    private void handleWatchedBallerinaTomlChange(Path filePath, FileEvent fileEvent, ProjectContext projectContext)
            throws WorkspaceDocumentException {
        Project project = projectContext.project();
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateBallerinaToml(Files.readString(filePath), projectContext, true);
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Ballerina.toml creation!", e);
                }
                break;
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectContext, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted:
                if (project.kind() == ProjectKind.BUILD_PROJECT) {
                    // This results down-grading a build-project into a single-file-project
                    // Thus, removing the project and allow subsequent changes to create single-file-projects
                    Lock lock = projectContext.lockAndGet();
                    try {
                        Path projectRoot = project.sourceRoot();
                        sourceRootToProject.remove(projectRoot);
                        clientLogger.logTrace(
                                String.format("Operation '%s' {project: '%s', kind: '%s'} removed",
                                        LSContextOperation.WS_WF_CHANGED.getName(),
                                        project.sourceRoot().toUri().toString(),
                                        projectContext.project().kind().name()
                                                .toLowerCase(Locale.getDefault())));
                    } finally {
                        // Unlock Project Instance
                        lock.unlock();
                    }
                    break;
                } else {
                    throw new WorkspaceDocumentException("Invalid operation, cannot delete Ballerina.toml!");
                }
        }
    }

    private void handleWatchedDependenciesTomlChange(Path filePath, FileEvent fileEvent, ProjectContext projectContext)
            throws WorkspaceDocumentException {
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateDependenciesToml(Files.readString(filePath), projectContext, true);
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} created",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Dependencies.toml creation!", e);
                }
                break;
            case Changed:
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectContext, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            case Deleted:
                // When removing Dependencies.toml, we are just reloading the project due to api-limitations.
                Lock lock = projectContext.lockAndGet();
                try {
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                    Path ballerinaTomlFile = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    createProjectContext(ballerinaTomlFile, LSContextOperation.WS_WF_CHANGED.getName())
                            .ifPresent(newProjectContext -> projectContext.setProject(newProjectContext.project()));
                } finally {
                    // Unlock Project Instance
                    lock.unlock();
                }
        }
    }

    private void handleWatchedCloudTomlChange(Path filePath, FileEvent fileEvent, ProjectContext projectContext)
            throws WorkspaceDocumentException {
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateCloudToml(Files.readString(filePath), projectContext, true);
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} created",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Cloud.toml creation!", e);
                }
                break;
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectContext, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted:
                // When removing Cloud.toml, we are just reloading the project due to api-limitations.
                Lock lock = projectContext.lockAndGet();
                try {
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                    Path ballerinaTomlFile = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    createProjectContext(ballerinaTomlFile, LSContextOperation.WS_WF_CHANGED.getName())
                            .ifPresent(newProject -> projectContext.setProject(newProject.project));
                } finally {
                    // Unlock Project Instance
                    lock.unlock();
                }
        }
    }

    private void handleWatchedCompilerPluginTomlChange(Path filePath, FileEvent fileEvent,
                                                       ProjectContext projectContext)
            throws WorkspaceDocumentException {
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateCompilerPluginToml(Files.readString(filePath), projectContext, true);
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} created",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Compiler-plugin.toml creation!", e);
                }
                break;
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectContext, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted:
                // When removing Compiler-plugin.toml, we are just reloading the project due to api-limitations.
                Lock lock = projectContext.lockAndGet();
                try {
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                    Path ballerinaTomlFile = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    createProjectContext(ballerinaTomlFile, LSContextOperation.WS_WF_CHANGED.getName())
                            .ifPresent(newProject -> projectContext.setProject(newProject.project()));
                } finally {
                    // Unlock Project Instance
                    lock.unlock();
                }
        }
    }

    private void handleWatchedBalToolTomlChange(Path filePath, FileEvent fileEvent, ProjectContext projectContext)
            throws WorkspaceDocumentException {
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateBalToolToml(Files.readString(filePath), projectContext, true);
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} created",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle BalTool.toml creation!", e);
                }
                break;
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectContext, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted:
                // When removing BalTool.toml, we are just reloading the project due to api-limitations.
                Lock lock = projectContext.lockAndGet();
                try {
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                    Path ballerinaTomlFile = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    createProjectContext(ballerinaTomlFile, LSContextOperation.WS_WF_CHANGED.getName())
                            .ifPresent(newProject -> projectContext.setProject(newProject.project()));
                } finally {
                    // Unlock Project Instance
                    lock.unlock();
                }
        }
    }

    private void handleWatchedModuleChange(Path filePath, FileEvent fileEvent, ProjectContext projectContext) {
        String fileName = filePath.getFileName().toString();
        switch (fileEvent.getType()) {
            case Created:
                // When adding a new module, it requires search and adding new docs and test docs also.
                // Thus, we are simply reloading the project.
                clientLogger.logTrace(String.format("Operation '%s' {module: '%s', uri: '%s'} created",
                        LSContextOperation.WS_WF_CHANGED.getName(),
                        fileName, filePath.toUri().toString()));
                Path ballerinaTomlPath = filePath.getParent().getParent().resolve(ProjectConstants.BALLERINA_TOML);
                reloadProject(projectContext, ballerinaTomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                break;
            case Deleted:
                if (ProjectConstants.MODULES_ROOT.equals(filePath.getFileName().toString())) {
                    // If removing all modules
                    Path tomlPath = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    clientLogger.logTrace(String.format("Operation '%s' {uri: '%s'} removed all modules",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            filePath.toUri().toString()));
                    reloadProject(projectContext, tomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                } else {
                    // If removing a particular module
                    Path tomlPath = filePath.getParent().getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    clientLogger.logTrace(String.format("Operation '%s' {module: '%s', uri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileName,
                            filePath.toUri().toString()));
                    reloadProject(projectContext, tomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
        }
    }

    private void updateBallerinaToml(String content, ProjectContext projectContext, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<BallerinaToml> ballerinaToml = projectContext.project().currentPackage().ballerinaToml();
            // Get toml
            if (ballerinaToml.isEmpty()) {
                if (createIfNotExists) {
                    if (projectContext.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                        // This results upgrading a single-file-project into a build-project
                        // When changing project type; need to remove key as well
                        // First, remove single-file-project key
                        sourceRootToProject.remove(projectContext.project().sourceRoot());
                        // Then, add the project as a build-project
                        Path ballerinaTomlFilePath = projectContext.project().sourceRoot().getParent()
                                .resolve(ProjectConstants.BALLERINA_TOML);
                        Optional<ProjectContext> newProjectContext = createProjectContext(ballerinaTomlFilePath,
                                LSContextOperation.WS_WF_CHANGED.getName());
                        if (newProjectContext.isEmpty()) {
                            throw new WorkspaceDocumentException("Invalid operation, cannot create Ballerina.toml!");
                        }
                        projectContext = newProjectContext.get();
                        sourceRootToProject.put(projectContext.project().sourceRoot(), projectContext);
                        return;
                    } else {
                        throw new WorkspaceDocumentException("Invalid operation, cannot create Ballerina.toml!");
                    }
                }
                throw new WorkspaceDocumentException(ProjectConstants.BALLERINA_TOML + " does not exists!");
            }
            // Update toml
            BallerinaToml updatedToml = ballerinaToml.get().modify().withContent(content).apply();
            // Update project instance
            projectContext.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateDependenciesToml(String content, ProjectContext projectContext, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<DependenciesToml> dependenciesToml = projectContext.project().currentPackage().dependenciesToml();
            // Get toml
            if (dependenciesToml.isEmpty()) {
                if (createIfNotExists) {
                    DocumentConfig documentConfig = DocumentConfig.from(
                            DocumentId.create(ProjectConstants.DEPENDENCIES_TOML, null), content,
                            ProjectConstants.DEPENDENCIES_TOML
                    );
                    Package pkg = projectContext.project().currentPackage().modify()
                            .addDependenciesToml(documentConfig)
                            .apply();
                    // Update project instance
                    projectContext.setProject(pkg.project());
                    return;
                }
                throw new WorkspaceDocumentException(ProjectConstants.DEPENDENCIES_TOML + " does not exist!");
            }
            // Update toml
            DependenciesToml updatedToml = dependenciesToml.get().modify().withContent(content).apply();
            // Update project instance
            projectContext.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateCloudToml(String content, ProjectContext projectContext, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<CloudToml> cloudToml = projectContext.project().currentPackage().cloudToml();
            // Get toml
            if (cloudToml.isEmpty()) {
                if (createIfNotExists) {
                    DocumentConfig documentConfig = DocumentConfig.from(
                            DocumentId.create(ProjectConstants.CLOUD_TOML, null), content,
                            ProjectConstants.CLOUD_TOML
                    );
                    Package pkg = projectContext.project().currentPackage().modify()
                            .addCloudToml(documentConfig)
                            .apply();
                    // Update project instance
                    projectContext.setProject(pkg.project());
                    return;
                }
                throw new WorkspaceDocumentException(ProjectConstants.CLOUD_TOML + " does not exists!");
            }
            // Update toml
            CloudToml updatedToml = cloudToml.get().modify().withContent(content).apply();
            // Update project instance
            projectContext.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateCompilerPluginToml(String content, ProjectContext projectContext, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<CompilerPluginToml> compilerPluginToml =
                    projectContext.project().currentPackage().compilerPluginToml();
            // Get toml
            if (compilerPluginToml.isEmpty()) {
                if (createIfNotExists) {
                    DocumentConfig documentConfig = DocumentConfig.from(
                            DocumentId.create(ProjectConstants.COMPILER_PLUGIN_TOML, null), content,
                            ProjectConstants.COMPILER_PLUGIN_TOML
                    );
                    Package pkg = projectContext.project().currentPackage().modify()
                            .addCompilerPluginToml(documentConfig)
                            .apply();
                    // Update project instance
                    projectContext.setProject(pkg.project());
                    return;
                }
                throw new WorkspaceDocumentException(ProjectConstants.COMPILER_PLUGIN_TOML + " does not exists!");
            }
            // Update toml
            CompilerPluginToml updatedToml = compilerPluginToml.get().modify().withContent(content).apply();
            // Update project instance
            projectContext.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateBalToolToml(String content, ProjectContext projectContext, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<BalToolToml> balToolToml =
                    projectContext.project().currentPackage().balToolToml();
            // Get toml
            if (balToolToml.isEmpty()) {
                if (createIfNotExists) {
                    DocumentConfig documentConfig = DocumentConfig.from(
                            DocumentId.create(ProjectConstants.BAL_TOOL_TOML, null), content,
                            ProjectConstants.BAL_TOOL_TOML
                    );
                    Package pkg = projectContext.project().currentPackage().modify()
                            .addBalToolToml(documentConfig)
                            .apply();
                    // Update project instance
                    projectContext.setProject(pkg.project());
                    return;
                }
                throw new WorkspaceDocumentException(ProjectConstants.BAL_TOOL_TOML + " does not exists!");
            }
            // Update toml
            BalToolToml updatedToml = balToolToml.get().modify().withContent(content).apply();
            // Update project instance
            projectContext.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateBalDocument(Path filePath, String content, ProjectContext projectContext)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            // Get document
            Optional<Document> document = document(filePath, projectContext.project(), null);
            if (document.isEmpty()) {
                throw new WorkspaceDocumentException("Document does not exist in path: " + filePath.toString());
            }
            document.get().modify().withContent(content).apply();
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void createBalDocument(Path filePath, String content, ProjectContext projectContext)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<ProjectContext> newProjectContext =
                    createProjectContext(filePath, LSContextOperation.TXT_DID_OPEN.getName());
            if (newProjectContext.isEmpty()) {
                //Client is notified about the error in the createProjectContext method.
                throw new WorkspaceDocumentException("Could not find the project for file path: "
                        + filePath.toString());
            }
            Optional<Document> document = document(filePath, newProjectContext.get().project(), null);
            if (document.isEmpty()) {
                projectContext.setProjectCrashed(true);
                throw new WorkspaceDocumentException("Could not create a new document for file path: "
                        + filePath.toString());
            }
            //Update the document with the content received via the request
            Document updatedDoc = document.get().modify().withContent(content).apply();
            //Update project instance
            projectContext.setProject(updatedDoc.module().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    /**
     * The document close notification is sent from the client to the server when the document got closed in the
     * client.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidCloseTextDocumentParams}
     */
    @Override
    public void didClose(Path filePath, DidCloseTextDocumentParams params) {
        this.openedDocuments.remove(filePath);
        Optional<Project> project = project(filePath);
        if (project.isEmpty()) {
            return;
        }
        // If it is a single file project, remove project from mapping
        if (project.get().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            Path projectRoot = project.get().sourceRoot();
            sourceRootToProject.remove(projectRoot);
            clientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CLOSE.getName() +
                    "' {project: '" + projectRoot.toUri().toString() +
                    "' kind: '" + project.get().kind().name().toLowerCase(Locale.getDefault()) +
                    "'} removed");
        }
    }

    // ============================================================================================================== //

    private Path computeProjectRoot(Path path) {
        return computeProjectKindAndProjectRoot(path).getRight();
    }

    private Pair<ProjectKind, Path> computeProjectKindAndProjectRoot(Path path) {
        if (ProjectPaths.isStandaloneBalFile(path)) {
            return new ImmutablePair<>(ProjectKind.SINGLE_FILE_PROJECT, path);
        }
        // Following is a temp fix to distinguish Bala and Build projects
        Path tomlPath = ProjectPaths.packageRoot(path).resolve(ProjectConstants.BALLERINA_TOML);
        if (Files.exists(tomlPath)) {
            return new ImmutablePair<>(ProjectKind.BUILD_PROJECT, ProjectPaths.packageRoot(path));
        }
        return new ImmutablePair<>(ProjectKind.BALA_PROJECT, ProjectPaths.packageRoot(path));
    }

    private Optional<ProjectContext> projectContext(Path projectRoot) {
        return Optional.ofNullable(sourceRootToProject.get(projectRoot));
    }

    private Optional<ProjectContext> createProjectContext(Path filePath, String operationName) {
        Project project = createProject(filePath, operationName);
        if (project == null) {
            return Optional.empty();
        }
        return Optional.of(ProjectContext.from(project));
    }

    private Project createProject(Path filePath, String operationName) {
        Pair<ProjectKind, Path> projectKindAndProjectRootPair = computeProjectKindAndProjectRoot(filePath);
        ProjectKind projectKind = projectKindAndProjectRootPair.getLeft();
        Path projectRoot = projectKindAndProjectRootPair.getRight();
        try {
            Project project;
            BuildOptions options = BuildOptions.builder()
                    .setOffline(CommonUtil.COMPILE_OFFLINE)
                    .setSticky(true)
                    .build();
            if (projectKind == ProjectKind.BUILD_PROJECT) {
                project = BuildProject.load(projectRoot, options);
            } else if (projectKind == ProjectKind.SINGLE_FILE_PROJECT) {
                project = SingleFileProject.load(projectRoot, options);
            } else {
                // Projects other than single file and build will use the ProjectLoader.
                project = ProjectLoader.loadProject(projectRoot, options);
            }
            clientLogger.logTrace("Operation '" + operationName +
                    "' {project: '" + projectRoot.toUri().toString() + "' kind: '" +
                    project.kind().name().toLowerCase(Locale.getDefault()) + "'} created");
            return project;
        } catch (ProjectException e) {
            //If there is an error the project crash status should be set.
            this.projectContext(projectRoot).ifPresent(projectContext -> projectContext.setProjectCrashed(true));
            clientLogger.notifyUser("Project load failed: " + e.getMessage(), e);
            clientLogger.logError(LSContextOperation.CREATE_PROJECT, "Operation '" + operationName +
                            "' {project: '" + projectRoot.toUri().toString() + "' kind: '" +
                            projectKind.name().toLowerCase(Locale.getDefault()) + "'} failed", e,
                    new TextDocumentIdentifier(filePath.toUri().toString()));
            return null;
        }
    }

    private Optional<Document> document(Path filePath, Project project, @Nullable CancelChecker cancelChecker) {
        if (cancelChecker != null) {
            cancelChecker.isCanceled();
        }
        try {
            DocumentId documentId = project.documentId(filePath);
            Module module = project.currentPackage().module(documentId.moduleId());
            return Optional.of(module.document(documentId));
        } catch (ProjectException e) {
            return Optional.empty();
        }
    }

    private boolean hasDocumentOrToml(Path filePath, Project project) {
        String fileName = Optional.of(filePath.getFileName()).get().toString();
        switch (fileName) {
            case ProjectConstants.BALLERINA_TOML:
                return project.currentPackage().ballerinaToml().isPresent();
            case ProjectConstants.CLOUD_TOML:
                return project.currentPackage().cloudToml().isPresent();
            case ProjectConstants.COMPILER_PLUGIN_TOML:
                return project.currentPackage().compilerPluginToml().isPresent();
            case ProjectConstants.BAL_TOOL_TOML:
                return project.currentPackage().balToolToml().isPresent();
            case ProjectConstants.DEPENDENCIES_TOML:
                return project.currentPackage().dependenciesToml().isPresent();
            default:
                if (fileName.endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
                    return document(filePath, project, null).isPresent();
                }
                return false;
        }
    }

    private void reloadProject(ProjectContext projectContext, Path filePath, String operationName) {
        // Lock Project Instance
        Lock lock = projectContext.lockAndGet();
        try {
            Optional<ProjectContext> newProjectContext = createProjectContext(filePath, operationName);
            if (newProjectContext.isEmpty()) {
                //Client is notified about this in the createProjectContext() method.
                return;
            }
            projectContext.setProject(newProjectContext.get().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private ProjectContext createOrGetProjectPair(Path filePath, String operationName)
            throws WorkspaceDocumentException {
        return createOrGetProjectPair(filePath, operationName, false);
    }

    private ProjectContext createOrGetProjectPair(Path filePath, String operationName, boolean isSourceChange)
            throws WorkspaceDocumentException {
        Path projectRoot = projectRoot(filePath);
        ProjectContext projectContext = sourceRootToProject.get(projectRoot);
        //Check if the project is crashed and create a new project if there is a change in the source files.
        if (projectContext != null && !(projectContext.isProjectCrashed() && isSourceChange)) {
            return projectContext;
        }
        //Try to create the project again.
        Optional<ProjectContext> newProjectContext = createProjectContext(projectRoot, operationName);
        if (newProjectContext.isEmpty()) {
            throw new WorkspaceDocumentException("Cannot find the project of uri: " + filePath.toString());
        }
        if (projectContext == null) {
            projectContext = newProjectContext.get();
            sourceRootToProject.put(projectRoot, projectContext);
            return projectContext;
        }
        projectContext.setProject(newProjectContext.get().project());
        projectContext.setProjectCrashed(false);
        return projectContext;
    }

    /**
     * This class holds project and its lock.
     */
    public static class ProjectContext {

        private final Lock lock;
        private Project project;

        private boolean compilationCrashed;

        private Process process;

        private boolean projectCrashed;

        private ProjectContext(Project project, Lock lock) {
            this.project = project;
            this.lock = lock;
            this.compilationCrashed = false;
        }

        public static ProjectContext from(Project project) {
            return new ProjectContext(project, new ReentrantLock(true));
        }

        public static ProjectContext from(Project project, Lock lock) {
            return new ProjectContext(project, lock);
        }

        /**
         * Returns the associated lock for the file.
         *
         * @return {@link Lock}
         */
        public Lock locker() {
            return this.lock;
        }

        /**
         * Returns the associated lock for the file.
         *
         * @return {@link Lock}
         */
        public Lock lockAndGet() {
            this.lock.lock();
            return this.lock;
        }

        /**
         * Returns the workspace document.
         *
         * @return {@link WorkspaceDocumentManager}
         */
        public Project project() {
            return this.project;
        }

        /**
         * Set workspace document.
         *
         * @param project {@link Project}
         */
        public void setProject(Project project) {
            this.project = project;
        }

        /**
         * Check if the project is in a crashed state.
         *
         * @return whether the compilation is in a crashed state
         */
        public boolean compilationCrashed() {
            return this.compilationCrashed;
        }

        /**
         * Set the crashed state.
         *
         * @param compilationCrashed crashed state
         */
        public void setCompilationCrashed(boolean compilationCrashed) {
            this.compilationCrashed = compilationCrashed;
        }

        /**
         * Set the project crashed status.
         *
         * @param projectCrashed whether the project is in a crashed state
         */
        public void setProjectCrashed(boolean projectCrashed) {
            this.projectCrashed = projectCrashed;
        }

        public boolean isProjectCrashed() {
            return projectCrashed;
        }

        /**
         * Project lock should be acquired before modifying (such as destroying) the process.
         *
         * @return Process associated with the project.
         */
        public Optional<Process> process() {
            return Optional.ofNullable(this.process);
        }

        /**
         * Set the process associated with the project. Project lock should be acquired before calling.
         *
         * @param process Process to be associated with the project.
         */
        public void setProcess(Process process) {
            this.process = process;
        }

        /**
         * Remove the process associated with the project. Project lock should be acquired before calling.
         */
        public void removeProcess() {
            this.process = null;
        }
    }

    /**
     * Represents a map of Path to ProjectContext.
     * <p>
     *
     * @param <K> cache key
     * @param <V> cache value
     *            Clear out front-faced cache implementation whenever a modification operation triggered for this map.
     */
    private static class SourceRootToProjectMap<K, V> extends HashMap<K, V> {

        private static final long serialVersionUID = 19900410L;
        private final transient Map<Path, Path> cache;

        public SourceRootToProjectMap(Map<Path, Path> pathToSourceRootCache) {
            super();
            this.cache = pathToSourceRootCache;
        }

        @Override
        public V put(K key, V value) {
            V old = super.put(key, value);
            // Clear dependent cache
            cache.clear();
            return old;
        }

        public V remove(Object key) {
            V result = super.remove(key);
            // Clear dependent cache
            cache.clear();
            return result;
        }

        @Override
        public void clear() {
            super.clear();
            // Clear dependent cache
            cache.clear();
        }
    }

    private Optional<Path> findProjectRoot(Path filePath) {
        if (filePath != null) {
            filePath = filePath.toAbsolutePath().normalize();
            if (filePath.toFile().isDirectory()) {
                if (hasBallerinaToml(filePath) || hasPackageJson(filePath)) {
                    return Optional.of(filePath);
                }
            }
            return findProjectRoot(filePath.getParent());
        }
        return Optional.empty();
    }

    private Optional<ProjectContext> getProjectOfWatchedFileChange(Path filePath, FileEvent fileEvent) {
        String fileName = filePath.getFileName().toString();
        boolean isBallerinaSourceChange = fileName.endsWith(ProjectConstants.BLANG_SOURCE_EXT);
        boolean isBallerinaTomlChange = filePath.endsWith(ProjectConstants.BALLERINA_TOML);
        boolean isDependenciesTomlChange = filePath.endsWith(ProjectConstants.DEPENDENCIES_TOML);
        boolean isCloudTomlChange = filePath.endsWith(ProjectConstants.CLOUD_TOML);
        boolean isCompilerPluginTomlChange = filePath.endsWith(ProjectConstants.COMPILER_PLUGIN_TOML);
        boolean isBalToolTomlChange = filePath.endsWith(ProjectConstants.BAL_TOOL_TOML);

        // NOTE: Need to specifically check Deleted events, since `filePath.toFile().isDirectory()`
        // fails when physical file is deleted from the disk
        boolean isModuleChange = filePath.toFile().isDirectory() &&
                filePath.getParent().endsWith(ProjectConstants.MODULES_ROOT) ||
                filePath.getParent().endsWith(ProjectConstants.GENERATED_MODULES_ROOT) ||
                (fileEvent.getType() == FileChangeType.Deleted && !isBallerinaSourceChange && !isBallerinaTomlChange &&
                        !isCloudTomlChange && !isDependenciesTomlChange && !isCompilerPluginTomlChange &&
                        !isBalToolTomlChange);

        return projectOfWatchedFileChange(filePath, fileEvent,
                isBallerinaSourceChange, isBallerinaTomlChange,
                isDependenciesTomlChange, isCloudTomlChange,
                isCompilerPluginTomlChange, isBalToolTomlChange, isModuleChange);
    }

    private boolean hasBallerinaToml(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return absFilePath.resolve(BALLERINA_TOML).toFile().exists();
    }

    private boolean hasPackageJson(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return absFilePath.resolve(ProjectConstants.PACKAGE_JSON).toFile().exists();
    }

    private static boolean isError(Diagnostic diagnostic) {
        return diagnostic.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR);
    }
}
