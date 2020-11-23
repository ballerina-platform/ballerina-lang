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
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.compiler.LSClientLogger;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Contains a set of utility methods to manage projects.
 *
 * @since 2.0.0
 */
public class BallerinaWorkspaceManager implements WorkspaceManager {
    /**
     * Mapping of source root to project instance.
     */
    private final Map<Path, Project> sourceRootToProject = new HashMap<>();
    /**
     * Cache mapping of document path to source root.
     */
    private static final Map<Path, Path> pathToSourceRootCache;

    static {
        Cache<Path, Path> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
        pathToSourceRootCache = cache.asMap();
    }

    /**
     * Returns a project root from the path provided.
     *
     * @param path ballerina project or standalone file path
     * @return project root
     */
    public Path projectRoot(Path path) {
        return pathToSourceRootCache.computeIfAbsent(path, this::computeProjectRoot);
    }

    /**
     * Returns project from the path provided.
     *
     * @param filePath ballerina project or standalone file path
     * @return project of applicable type
     */
    public Optional<Project> project(Path filePath) {
        return Optional.ofNullable(sourceRootToProject.get(projectRoot(filePath)));
    }

    /**
     * Returns module from the path provided.
     *
     * @param filePath file path of the document
     * @return project of applicable type
     */
    public Optional<Module> module(Path filePath) {
        Optional<Project> project = project(filePath);
        if (project.isEmpty()) {
            return Optional.empty();
        }
        Optional<Document> document = document(filePath, project.get());
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
    public Optional<Document> document(Path filePath) {
        Optional<Project> project = project(filePath);
        return project.isPresent() ? document(filePath, project.get()) : Optional.empty();
    }

    /**
     * Returns syntax tree from the path provided.
     *
     * @param filePath file path of the document
     * @return {@link io.ballerina.compiler.syntax.tree.SyntaxTree}
     */
    public Optional<SyntaxTree> syntaxTree(Path filePath) {
        Optional<Document> document = this.document(filePath);
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
    public Optional<SemanticModel> semanticModel(Path filePath) {
        Optional<Module> optModule = this.module(filePath);
        if (optModule.isEmpty()) {
            return Optional.empty();
        }
        Module module = optModule.get();
        return Optional.ofNullable(module.packageInstance().getCompilation().getSemanticModel(module.moduleId()));
    }

    /**
     * The document open notification is sent from the client to the server to signal newly opened text documents.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidOpenTextDocumentParams}
     */
    public void didOpen(Path filePath, DidOpenTextDocumentParams params) {
        // Create project, if not exists
        sourceRootToProject.computeIfAbsent(projectRoot(filePath), this::createProject);
    }

    /**
     * The document change notification is sent from the client to the server to signal changes to a text document.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidChangeTextDocumentParams}
     * @throws WorkspaceDocumentException when project or document not found
     */
    public void didChange(Path filePath, DidChangeTextDocumentParams params) throws WorkspaceDocumentException {
        // Get project
        Optional<Project> project = project(filePath);
        if (project.isEmpty()) {
            throw new WorkspaceDocumentException("Cannot add changes to a file in an un-opened project!");
        }

        // Get document
        Optional<Document> document = document(filePath, project.get());
        if (document.isEmpty()) {
            throw new WorkspaceDocumentException("Document does not exist in path: " + filePath.toString());
        }

        // Update file
        String content = params.getContentChanges().get(0).getText();
        Document updatedDoc = document.get().modify().withContent(content).apply();

        // Update project instance
        sourceRootToProject.put(projectRoot(filePath), updatedDoc.module().project());
    }

    /**
     * The document close notification is sent from the client to the server when the document got closed in the
     * client.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidCloseTextDocumentParams}
     * @throws WorkspaceDocumentException when project not found
     */
    public void didClose(Path filePath, DidCloseTextDocumentParams params) throws WorkspaceDocumentException {
        Optional<Project> project = project(filePath);
        if (project.isEmpty()) {
            throw new WorkspaceDocumentException("Cannot close a file in an un-opened project!");
        }
        // If it is a single file project, remove project from mapping
        if (project.get().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            Path projectRoot = projectRoot(filePath);
            sourceRootToProject.remove(projectRoot);
            LSClientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CLOSE.getName() +
                                            "' {project: '" + projectRoot.toUri().toString() +
                                            "' kind: '" + project.get().kind().name().toLowerCase(Locale.getDefault()) +
                                            "'} removed}");
        }
    }

    // ============================================================================================================== //

    private Path computeProjectRoot(Path path) {
        return computeProjectKindAndProjectRoot(path).getRight();
    }

    private Pair<ProjectKind, Path> computeProjectKindAndProjectRoot(Path path) {
        Path projectRoot;
        if (path.toFile().isDirectory()) {
            if (ProjectConstants.MODULES_ROOT.equals(path.getParent().toFile().getName())) {
                projectRoot = path.getParent().getParent();
            } else {
                projectRoot = path;
            }
            return new ImmutablePair<>(ProjectKind.BUILD_PROJECT, projectRoot);
        }
        // Check if the file is a source file in the default module
        projectRoot = path.getParent();
        if (hasBallerinaToml(projectRoot)) {
            return new ImmutablePair<>(ProjectKind.BUILD_PROJECT, projectRoot);
        }

        // Check if the file is a test file in the default module
        Path testsRoot = path.getParent();
        projectRoot = testsRoot.getParent();
        if (ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return new ImmutablePair<>(ProjectKind.BUILD_PROJECT, projectRoot);
        }

        // Check if the file is a source file in a non-default module
        Path modulesRoot = path.getParent().getParent();
        projectRoot = modulesRoot.getParent();
        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return new ImmutablePair<>(ProjectKind.BUILD_PROJECT, projectRoot);
        }

        // Check if the file is a test file in a non-default module
        modulesRoot = testsRoot.getParent().getParent();
        projectRoot = modulesRoot.getParent();

        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return new ImmutablePair<>(ProjectKind.BUILD_PROJECT, projectRoot);
        }

        return new ImmutablePair<>(ProjectKind.SINGLE_FILE_PROJECT, path);
    }

    private static boolean hasBallerinaToml(Path filePath) {
        return filePath.resolve(ProjectConstants.BALLERINA_TOML).toFile().exists();
    }

    private Project createProject(Path filePath) {
        Pair<ProjectKind, Path> projectKindAndProjectRootPair = computeProjectKindAndProjectRoot(filePath);
        ProjectKind projectKind = projectKindAndProjectRootPair.getLeft();
        Path projectRoot = projectKindAndProjectRootPair.getRight();
        Project project;
        if (projectKind == ProjectKind.BUILD_PROJECT) {
            project = BuildProject.load(projectRoot);
        } else {
            project = SingleFileProject.load(projectRoot);
        }
        LSClientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_OPEN.getName() +
                                        "' {project: '" + projectRoot.toUri().toString() + "' kind: '" +
                                        project.kind().name().toLowerCase(Locale.getDefault()) + "'} created}");
        return project;
    }

    private Optional<Path> modulePath(ModuleId moduleId, Project project) {
        if (project.currentPackage().moduleIds().contains(moduleId)) {
            if (project.currentPackage().getDefaultModule().moduleId() == moduleId) {
                return Optional.of(project.sourceRoot());
            } else {
                return Optional.of(project.sourceRoot().resolve(ProjectConstants.MODULES_ROOT).resolve(
                        project.currentPackage().module(moduleId).moduleName().moduleNamePart()));
            }
        }
        return Optional.empty();
    }

    private Optional<Document> document(Path filePath, Project project) {
        Optional<DocumentId> documentId = documentId(filePath, project);
        if (documentId.isEmpty()) {
            return Optional.empty();
        }
        Module module = project.currentPackage().module(documentId.get().moduleId());
        return Optional.of(module.document(documentId.get()));
    }

    private Optional<DocumentId> documentId(Path documentFilePath, Project project) {
        // Single File Project
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            Module oldModule = project.currentPackage().module(
                    project.currentPackage().moduleIds().iterator().next());
            return Optional.of(oldModule.documentIds().iterator().next());
        }

        // Build Project
        Path parent = documentFilePath.getParent();
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            // TODO: Check whether this logic also works for Single File projects
            Optional<Path> modulePath = modulePath(moduleId, project);
            if (modulePath.isPresent()) {
                if (parent.equals(modulePath.get())
                        || parent.equals(modulePath.get().resolve(ProjectConstants.TEST_DIR_NAME))) {
                    Module module = project.currentPackage().module(moduleId);
                    for (DocumentId documentId : module.documentIds()) {
                        if (module.document(documentId).name().equals(
                                documentFilePath.getFileName().toString())) {
                            return Optional.of(documentId);
                        }
                    }

                    for (DocumentId documentId : module.testDocumentIds()) {
                        if (module.document(documentId).name().equals(
                                documentFilePath.getFileName().toString())) {
                            return Optional.of(documentId);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
}
