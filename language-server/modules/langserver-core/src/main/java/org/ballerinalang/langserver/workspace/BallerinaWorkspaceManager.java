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
import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.CloudToml;
import io.ballerina.projects.CompilerPluginToml;
import io.ballerina.projects.DependenciesToml;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
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
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.eventsync.EventSyncPubSubHolder;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;

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
    protected final Map<Path, ProjectPair> sourceRootToProject;
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
        return projectPair(projectRoot(filePath)).map(ProjectPair::project);
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
        Optional<Project> optionalProject = project(ProjectPaths.packageRoot(filePath));

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
        Optional<ProjectPair> projectPair = projectPair(projectRoot(filePath));
        if (module.isEmpty() || packageCompilation.isEmpty() || projectPair.isEmpty() || projectPair.get().crashed()) {
            return Optional.empty();
        }
        return Optional.of(packageCompilation.get().getSemanticModel(module.get().moduleId()));
    }

    @Override
    public Optional<SemanticModel> semanticModel(Path filePath, @Nonnull CancelChecker cancelChecker) {
        Optional<Module> module = this.module(filePath);
        Optional<PackageCompilation> packageCompilation = waitAndGetPackageCompilation(filePath, cancelChecker);
        Optional<ProjectPair> projectPair = projectPair(projectRoot(filePath));
        if (module.isEmpty() || packageCompilation.isEmpty() || projectPair.isEmpty() || projectPair.get().crashed()) {
            return Optional.empty();
        }
        return Optional.of(packageCompilation.get().getSemanticModel(module.get().moduleId()));
    }

    /**
     * Returns module compilation from the file path provided.
     *
     * @param filePath file path of the document
     * @return {@link ModuleCompilation}
     */
    @Override
    public Optional<PackageCompilation> waitAndGetPackageCompilation(Path filePath) {
        // Get Project and Lock
        Optional<ProjectPair> projectPair = projectPair(projectRoot(filePath));
        if (projectPair.isEmpty()) {
            return Optional.empty();
        }

        // Lock Project Instance
        Lock lock = projectPair.get().lockAndGet();
        try {
            PackageCompilation compilation = projectPair.get().project().currentPackage().getCompilation();
            if (compilation.diagnosticResult().diagnostics().stream()
                    .anyMatch(diagnostic -> 
                            Arrays.asList(DiagnosticErrorCode.BAD_SAD_FROM_COMPILER.diagnosticId(), 
                                            DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.diagnosticId())
                            .contains(diagnostic.diagnosticInfo().code()))) {
                projectPair.get().setCrashed(true);
                projectPair.get().project().clearCaches();
            }
            return Optional.of(compilation);
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
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
        ProjectPair projectPair = createOrGetProjectPair(filePath, LSContextOperation.TXT_DID_OPEN.getName());

        Project project = projectPair.project();
        if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML))) {
            // Create or update Ballerina.toml
            updateBallerinaToml(params.getTextDocument().getText(), projectPair, true);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.DEPENDENCIES_TOML))) {
            // Create or update Dependencies.toml
            updateDependenciesToml(params.getTextDocument().getText(), projectPair, true);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.CLOUD_TOML))) {
            // Create or update Cloud.toml
            updateCloudToml(params.getTextDocument().getText(), projectPair, true);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.COMPILER_PLUGIN_TOML))) {
            // Create or update Compiler-plugin.toml
            updateCompilerPluginToml(params.getTextDocument().getText(), projectPair, true);
        } else if (ProjectPaths.isBalFile(filePath) && project.kind() != ProjectKind.BALA_PROJECT) {
            // Create or update .bal document
            updateBalDocument(filePath, params.getTextDocument().getText(), projectPair, true);
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
        ProjectPair projectPair = createOrGetProjectPair(filePath, LSContextOperation.TXT_DID_CHANGE.getName());

        Project project = projectPair.project();
        if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML))) {
            // Update Ballerina.toml
            updateBallerinaToml(params.getContentChanges().get(0).getText(), projectPair, false);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.DEPENDENCIES_TOML))) {
            // create or update Dependencies.toml
            updateDependenciesToml(params.getContentChanges().get(0).getText(), projectPair, false);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.CLOUD_TOML))) {
            // create or update Cloud.toml
            updateCloudToml(params.getContentChanges().get(0).getText(), projectPair, false);
        } else if (filePath.equals(project.sourceRoot().resolve(ProjectConstants.COMPILER_PLUGIN_TOML))) {
            // create or update Compiler-plugin.toml
            updateCompilerPluginToml(params.getContentChanges().get(0).getText(), projectPair, false);
        } else if (ProjectPaths.isBalFile(filePath) && project.kind() != ProjectKind.BALA_PROJECT) {
            // Update .bal document
            updateBalDocument(filePath, params.getContentChanges().get(0).getText(), projectPair, false);
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
        Optional<ProjectPair> optProject = getProjectOfWatchedFileChange(filePath, fileEvent);
        if (optProject.isEmpty()) {
            clientLogger.logTrace(
                    String.format("Operation '%s' No matching project found, {fileUri: '%s' event: '%s'} ignored",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri(),
                            fileEvent.getType().name()));
            return;
        }
        ProjectPair projectPair = optProject.get();
        Project project = projectPair.project();
        String fileName = filePath.getFileName().toString();
        boolean isBallerinaSourceChange = fileName.endsWith(ProjectConstants.BLANG_SOURCE_EXT);
        boolean isBallerinaTomlChange = filePath.endsWith(ProjectConstants.BALLERINA_TOML);
        boolean isDependenciesTomlChange = filePath.endsWith(ProjectConstants.DEPENDENCIES_TOML);
        boolean isCloudTomlChange = filePath.endsWith(ProjectConstants.CLOUD_TOML);
        boolean isCompilerPluginTomlChange = filePath.endsWith(ProjectConstants.COMPILER_PLUGIN_TOML);
        if (fileEvent.getType() == FileChangeType.Created &&
                (isBallerinaSourceChange || isBallerinaTomlChange || isCloudTomlChange || isCompilerPluginTomlChange)
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
            handleWatchedBalSourceChange(filePath, fileEvent, projectPair);
        } else if (isBallerinaTomlChange) {
            handleWatchedBallerinaTomlChange(filePath, fileEvent, projectPair);
        } else if (isCloudTomlChange) {
            handleWatchedCloudTomlChange(filePath, fileEvent, projectPair);
        } else if (isDependenciesTomlChange) {
            handleWatchedDependenciesTomlChange(filePath, fileEvent, projectPair);
        } else if (isCompilerPluginTomlChange) {
            handleWatchedCompilerPluginTomlChange(filePath, fileEvent, projectPair);
        } else {
            handleWatchedModuleChange(filePath, fileEvent, projectPair);
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
                Optional<ProjectPair> optProject = getProjectOfWatchedFileChange(filePath, fileEvent);
                if (optProject.isPresent()) {
                    ProjectPair projectPair = optProject.get();
                    Project project = projectPair.project();
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
            Optional<ProjectPair> projectPair = this.projectPair(path);
            if (projectPair.isEmpty()) {
                return;
            }
            Lock lock = projectPair.get().lockAndGet();
            try {
                ProjectPair project = createProject(path, LSContextOperation.WS_WF_CHANGED.getName());
                projectPair.get().setProject(project.project());
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

    /**
     * Refresh the project by cloning it internally and clearing caches.
     *
     * @param filePath A path of a file in the project
     */
    public void refreshProject(Path filePath) throws WorkspaceDocumentException {
        Optional<ProjectPair> projectPairOpt = projectPair(projectRoot(filePath));
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

    private Optional<ProjectPair> projectOfWatchedFileChange(Path filePath, FileEvent fileEvent,
                                                             boolean isBallerinaSourceChange,
                                                             boolean isBallerinaTomlChange,
                                                             boolean isDependenciesTomlChange,
                                                             boolean isCloudTomlChange,
                                                             boolean isCompilerPluginTomlChange,
                                                             boolean isModuleChange) {
        if (isBallerinaSourceChange) {
            if (fileEvent.getType() == FileChangeType.Created) {
                return projectPair(projectRoot(filePath));
            } else {
                // DELETED event
                // First try as a single-file-project
                Optional<ProjectPair> optProject = projectPair(filePath);
                if (optProject.isPresent()) {
                    return optProject;
                }
                // Or Else, try as a build-project
                Path parent = filePath.getParent();
                if (ProjectConstants.TEST_DIR_NAME.equals(parent.getFileName().toString())) {
                    // If inside a tests folder, get parent
                    parent = parent.getParent();
                }
                if (ProjectConstants.MODULES_ROOT.equals(parent.getParent().getFileName().toString())) {
                    // If inside a module folder, get parent
                    parent = parent.getParent().getParent();
                }
                return projectPair(parent);
            }
        } else if (isBallerinaTomlChange) {
            if (fileEvent.getType() == FileChangeType.Created) {
                // Check for a project upgrade from a single-file to a build-project
                // In such scenario, project will be only available with the key of that single file path.
                Optional<ProjectPair> optProject = sourceRootToProject.entrySet().stream()
                        .filter(entry -> entry.getValue().project.kind() == ProjectKind.SINGLE_FILE_PROJECT &&
                                entry.getKey().getParent().equals(filePath.getParent()))
                        .findFirst()
                        .map(Map.Entry::getValue);
                if (optProject.isEmpty()) {
                    // Single-file project is unavailable if we just downgraded a build-project removing Ballerina.toml
                    // Thus, loading a new build-project here
                    optProject = Optional.of(createProject(filePath, LSContextOperation.WS_WF_CHANGED.getName()));
                    sourceRootToProject.put(optProject.get().project().sourceRoot(), optProject.get());

                }
                return optProject;
            } else {
                // Check for a project downgrade from a build-project to a single-file
                return projectPair(filePath.getParent());
            }
        } else if (isCloudTomlChange || isCompilerPluginTomlChange || isDependenciesTomlChange) {
            return projectPair(filePath.getParent());
        } else if (isModuleChange) {
            Path projectRoot;
            if (ProjectConstants.MODULES_ROOT.equals(filePath.getFileName().toString())) {
                // If it is **/projectRoot/modules
                projectRoot = filePath.getParent();
            } else {
                // If it is **/projectRoot/modules/mod2
                projectRoot = filePath.getParent().getParent();
            }
            return projectPair(projectRoot);
        } else {
            // Skip if unrecognized file change
            return Optional.empty();
        }
    }

    private void handleWatchedBalSourceChange(Path filePath, FileEvent fileEvent, ProjectPair projectPair) {
        switch (fileEvent.getType()) {
            case Created: {
                // Creating new document requires finding the module it resides
                // Thus, reloading the project
                reloadProject(projectPair, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                break;
            }
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectPair, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted: {
                Project project = projectPair.project();
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
                        Lock lock = projectPair.lockAndGet();
                        try {
                            Project updatedProj = document.get().module().modify().removeDocument(
                                    document.get().documentId()).apply().project();
                            projectPair.setProject(updatedProj);
                            clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                                    LSContextOperation.WS_WF_CHANGED.getName(),
                                    fileEvent.getUri()));
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        // If document-id not found, reload project
                        Path ballerinaTomlPath = project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML);
                        reloadProject(projectPair, ballerinaTomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                    }
                }
            }
        }
    }

    private void handleWatchedBallerinaTomlChange(Path filePath, FileEvent fileEvent, ProjectPair projectPair)
            throws WorkspaceDocumentException {
        Project project = projectPair.project();
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateBallerinaToml(Files.readString(filePath), projectPair, true);
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Ballerina.toml creation!", e);
                }
                break;
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectPair, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted:
                if (project.kind() == ProjectKind.BUILD_PROJECT) {
                    // This results down-grading a build-project into a single-file-project
                    // Thus, removing the project and allow subsequent changes to create single-file-projects
                    Lock lock = projectPair.lockAndGet();
                    try {
                        Path projectRoot = project.sourceRoot();
                        sourceRootToProject.remove(projectRoot);
                        clientLogger.logTrace(
                                String.format("Operation '%s' {project: '%s', kind: '%s'} removed",
                                        LSContextOperation.WS_WF_CHANGED.getName(),
                                        project.sourceRoot().toUri().toString(),
                                        projectPair.project().kind().name()
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

    private void handleWatchedDependenciesTomlChange(Path filePath, FileEvent fileEvent, ProjectPair projectPair)
            throws WorkspaceDocumentException {
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateDependenciesToml(Files.readString(filePath), projectPair, true);
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} created",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Dependencies.toml creation!", e);
                }
                break;
            case Changed:
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectPair, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            case Deleted:
                // When removing Dependencies.toml, we are just reloading the project due to api-limitations.
                Lock lock = projectPair.lockAndGet();
                try {
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                    Path ballerinaTomlFile = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    projectPair.setProject(
                            createProject(ballerinaTomlFile, LSContextOperation.WS_WF_CHANGED.getName()).project());
                } finally {
                    // Unlock Project Instance
                    lock.unlock();
                }
        }
    }

    private void handleWatchedCloudTomlChange(Path filePath, FileEvent fileEvent, ProjectPair projectPair)
            throws WorkspaceDocumentException {
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateCloudToml(Files.readString(filePath), projectPair, true);
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} created",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Cloud.toml creation!", e);
                }
                break;
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectPair, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted:
                // When removing Cloud.toml, we are just reloading the project due to api-limitations.
                Lock lock = projectPair.lockAndGet();
                try {
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                    Path ballerinaTomlFile = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    projectPair.setProject(
                            createProject(ballerinaTomlFile, LSContextOperation.WS_WF_CHANGED.getName()).project());
                } finally {
                    // Unlock Project Instance
                    lock.unlock();
                }
        }
    }

    private void handleWatchedCompilerPluginTomlChange(Path filePath, FileEvent fileEvent, ProjectPair projectPair)
            throws WorkspaceDocumentException {
        switch (fileEvent.getType()) {
            case Created:
                try {
                    updateCompilerPluginToml(Files.readString(filePath), projectPair, true);
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} created",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                } catch (IOException e) {
                    throw new WorkspaceDocumentException("Could not handle Compiler-plugin.toml creation!", e);
                }
                break;
            case Changed: {
                if (!this.openedDocuments.contains(filePath)) {
                    reloadProject(projectPair, filePath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
            }
            case Deleted:
                // When removing Compiler-plugin.toml, we are just reloading the project due to api-limitations.
                Lock lock = projectPair.lockAndGet();
                try {
                    clientLogger.logTrace(String.format("Operation '%s' {fileUri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileEvent.getUri()));
                    Path ballerinaTomlFile = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    projectPair.setProject(
                            createProject(ballerinaTomlFile, LSContextOperation.WS_WF_CHANGED.getName()).project());
                } finally {
                    // Unlock Project Instance
                    lock.unlock();
                }
        }
    }

    private void handleWatchedModuleChange(Path filePath, FileEvent fileEvent, ProjectPair projectPair) {
        String fileName = filePath.getFileName().toString();
        switch (fileEvent.getType()) {
            case Created:
                // When adding a new module, it requires search and adding new docs and test docs also.
                // Thus, we are simply reloading the project.
                clientLogger.logTrace(String.format("Operation '%s' {module: '%s', uri: '%s'} created",
                        LSContextOperation.WS_WF_CHANGED.getName(),
                        fileName, filePath.toUri().toString()));
                Path ballerinaTomlPath = filePath.getParent().getParent().resolve(ProjectConstants.BALLERINA_TOML);
                reloadProject(projectPair, ballerinaTomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                break;
            case Deleted:
                if (ProjectConstants.MODULES_ROOT.equals(filePath.getFileName().toString())) {
                    // If removing all modules
                    Path tomlPath = filePath.getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    clientLogger.logTrace(String.format("Operation '%s' {uri: '%s'} removed all modules",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            filePath.toUri().toString()));
                    reloadProject(projectPair, tomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                } else {
                    // If removing a particular module
                    Path tomlPath = filePath.getParent().getParent().resolve(ProjectConstants.BALLERINA_TOML);
                    clientLogger.logTrace(String.format("Operation '%s' {module: '%s', uri: '%s'} removed",
                            LSContextOperation.WS_WF_CHANGED.getName(),
                            fileName,
                            filePath.toUri().toString()));
                    reloadProject(projectPair, tomlPath, LSContextOperation.WS_WF_CHANGED.getName());
                }
                break;
        }
    }

    private void updateBallerinaToml(String content, ProjectPair projectPair, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectPair.lockAndGet();
        try {
            Optional<BallerinaToml> ballerinaToml = projectPair.project().currentPackage().ballerinaToml();
            // Get toml
            if (ballerinaToml.isEmpty()) {
                if (createIfNotExists) {
                    if (projectPair.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                        // This results upgrading a single-file-project into a build-project
                        // When changing project type; need to remove key as well
                        // First, remove single-file-project key
                        sourceRootToProject.remove(projectPair.project().sourceRoot());
                        // Then, add the project as a build-project
                        Path ballerinaTomlFilePath = projectPair.project().sourceRoot().getParent()
                                .resolve(ProjectConstants.BALLERINA_TOML);
                        projectPair = createProject(ballerinaTomlFilePath, LSContextOperation.WS_WF_CHANGED.getName());
                        sourceRootToProject.put(projectPair.project().sourceRoot(), projectPair);
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
            projectPair.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateDependenciesToml(String content, ProjectPair projectPair, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectPair.lockAndGet();
        try {
            Optional<DependenciesToml> dependenciesToml = projectPair.project().currentPackage().dependenciesToml();
            // Get toml
            if (dependenciesToml.isEmpty()) {
                if (createIfNotExists) {
                    DocumentConfig documentConfig = DocumentConfig.from(
                            DocumentId.create(ProjectConstants.DEPENDENCIES_TOML, null), content,
                            ProjectConstants.DEPENDENCIES_TOML
                    );
                    Package pkg = projectPair.project().currentPackage().modify()
                            .addDependenciesToml(documentConfig)
                            .apply();
                    // Update project instance
                    projectPair.setProject(pkg.project());
                    return;
                }
                throw new WorkspaceDocumentException(ProjectConstants.DEPENDENCIES_TOML + " does not exists!");
            }
            // Update toml
            DependenciesToml updatedToml = dependenciesToml.get().modify().withContent(content).apply();
            // Update project instance
            projectPair.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateCloudToml(String content, ProjectPair projectPair, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectPair.lockAndGet();
        try {
            Optional<CloudToml> cloudToml = projectPair.project().currentPackage().cloudToml();
            // Get toml
            if (cloudToml.isEmpty()) {
                if (createIfNotExists) {
                    DocumentConfig documentConfig = DocumentConfig.from(
                            DocumentId.create(ProjectConstants.CLOUD_TOML, null), content,
                            ProjectConstants.CLOUD_TOML
                    );
                    Package pkg = projectPair.project().currentPackage().modify()
                            .addCloudToml(documentConfig)
                            .apply();
                    // Update project instance
                    projectPair.setProject(pkg.project());
                    return;
                }
                throw new WorkspaceDocumentException(ProjectConstants.CLOUD_TOML + " does not exists!");
            }
            // Update toml
            CloudToml updatedToml = cloudToml.get().modify().withContent(content).apply();
            // Update project instance
            projectPair.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateCompilerPluginToml(String content, ProjectPair projectPair, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectPair.lockAndGet();
        try {
            Optional<CompilerPluginToml> compilerPluginToml =
                    projectPair.project().currentPackage().compilerPluginToml();
            // Get toml
            if (compilerPluginToml.isEmpty()) {
                if (createIfNotExists) {
                    DocumentConfig documentConfig = DocumentConfig.from(
                            DocumentId.create(ProjectConstants.COMPILER_PLUGIN_TOML, null), content,
                            ProjectConstants.COMPILER_PLUGIN_TOML
                    );
                    Package pkg = projectPair.project().currentPackage().modify()
                            .addCompilerPluginToml(documentConfig)
                            .apply();
                    // Update project instance
                    projectPair.setProject(pkg.project());
                    return;
                }
                throw new WorkspaceDocumentException(ProjectConstants.COMPILER_PLUGIN_TOML + " does not exists!");
            }
            // Update toml
            CompilerPluginToml updatedToml = compilerPluginToml.get().modify().withContent(content).apply();
            // Update project instance
            projectPair.setProject(updatedToml.packageInstance().project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private void updateBalDocument(Path filePath, String content, ProjectPair projectPair, boolean createIfNotExists)
            throws WorkspaceDocumentException {
        // Lock Project Instance
        Lock lock = projectPair.lockAndGet();
        try {
            // Get document
            Optional<Document> document = document(filePath, projectPair.project(), null);
            if (document.isEmpty()) {
                if (createIfNotExists) {
                    //TODO: Need to create document here, Need to address with workspace events
                    // Reload the project
                    projectPair.setProject(
                            createProject(filePath, LSContextOperation.TXT_DID_OPEN.getName()).project());
                    document = document(filePath, projectPair.project(), null);
                } else {
                    throw new WorkspaceDocumentException("Document does not exist in path: " + filePath.toString());
                }
            }

            // Update file
            Document updatedDoc = document.get().modify().withContent(content).apply();

            // Update project instance
            projectPair.setProject(updatedDoc.module().project());
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

    private Optional<ProjectPair> projectPair(Path projectRoot) {
        return Optional.ofNullable(sourceRootToProject.get(projectRoot));
    }

    private ProjectPair createProject(Path filePath, String operationName) {
        Pair<ProjectKind, Path> projectKindAndProjectRootPair = computeProjectKindAndProjectRoot(filePath);
        ProjectKind projectKind = projectKindAndProjectRootPair.getLeft();
        Path projectRoot = projectKindAndProjectRootPair.getRight();
        try {
            Project project;
            BuildOptions options = BuildOptions.builder()
                    .setOffline(CommonUtil.COMPILE_OFFLINE)
                    .setSticky(true)
                    .targetDir(ProjectUtils.getTemporaryTargetPath())
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
            return ProjectPair.from(project);
        } catch (ProjectException e) {
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
            case ProjectConstants.DEPENDENCIES_TOML:
                return project.currentPackage().dependenciesToml().isPresent();
            default:
                if (fileName.endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
                    return document(filePath, project, null).isPresent();
                }
                return false;
        }
    }

    private void reloadProject(ProjectPair projectPair, Path filePath, String operationName) {
        // Lock Project Instance
        Lock lock = projectPair.lockAndGet();
        try {
            projectPair.setProject(createProject(filePath, operationName).project());
        } finally {
            // Unlock Project Instance
            lock.unlock();
        }
    }

    private ProjectPair createOrGetProjectPair(Path filePath, String operationName) throws WorkspaceDocumentException {
        Path projectRoot = projectRoot(filePath);
        sourceRootToProject.computeIfAbsent(projectRoot, path -> createProject(filePath, operationName));

        // Get document
        ProjectPair projectPair = sourceRootToProject.get(projectRoot);
        if (projectPair == null) {
            // NOTE: This will never happen since we create a project if not exists
            throw new WorkspaceDocumentException("Cannot find the project of uri: " + filePath.toString());
        }
        return projectPair;
    }

    /**
     * This class holds project and its lock.
     */
    public static class ProjectPair {

        private final Lock lock;
        private Project project;

        private boolean crashed;

        private ProjectPair(Project project, Lock lock) {
            this.project = project;
            this.lock = lock;
        }

        public static ProjectPair from(Project project) {
            return new ProjectPair(project, new ReentrantLock(true));
        }

        public static ProjectPair from(Project project, Lock lock) {
            return new ProjectPair(project, lock);
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
         * @return whether the project is in a crashed state
         */
        public boolean crashed() {
            return Boolean.TRUE.equals(this.crashed);
        }

        /**
         * Set the crashed state.
         *
         * @param crashed crashed state
         */
        public void setCrashed(boolean crashed) {
            this.crashed = crashed;
        }
    }

    /**
     * Represents a map of Path to ProjectPair.
     * <p>
     * Clear out front-faced cache implementation whenever a modification operation triggered for this map.
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

    private Optional<ProjectPair> getProjectOfWatchedFileChange(Path filePath, FileEvent fileEvent) {
        String fileName = filePath.getFileName().toString();
        boolean isBallerinaSourceChange = fileName.endsWith(ProjectConstants.BLANG_SOURCE_EXT);
        boolean isBallerinaTomlChange = filePath.endsWith(ProjectConstants.BALLERINA_TOML);
        boolean isDependenciesTomlChange = filePath.endsWith(ProjectConstants.DEPENDENCIES_TOML);
        boolean isCloudTomlChange = filePath.endsWith(ProjectConstants.CLOUD_TOML);
        boolean isCompilerPluginTomlChange = filePath.endsWith(ProjectConstants.COMPILER_PLUGIN_TOML);

        // NOTE: Need to specifically check Deleted events, since `filePath.toFile().isDirectory()`
        // fails when physical file is deleted from the disk
        boolean isModuleChange = filePath.toFile().isDirectory() &&
                filePath.getParent().endsWith(ProjectConstants.MODULES_ROOT) ||
                (fileEvent.getType() == FileChangeType.Deleted && !isBallerinaSourceChange && !isBallerinaTomlChange &&
                        !isCloudTomlChange && !isDependenciesTomlChange && !isCompilerPluginTomlChange);

        return projectOfWatchedFileChange(filePath, fileEvent,
                isBallerinaSourceChange, isBallerinaTomlChange,
                isDependenciesTomlChange, isCloudTomlChange,
                isCompilerPluginTomlChange, isModuleChange);
    }

    private boolean hasBallerinaToml(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return absFilePath.resolve(BALLERINA_TOML).toFile().exists();
    }

    private boolean hasPackageJson(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return absFilePath.resolve(ProjectConstants.PACKAGE_JSON).toFile().exists();
    }
}
