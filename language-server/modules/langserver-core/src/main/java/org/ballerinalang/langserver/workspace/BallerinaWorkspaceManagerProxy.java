/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.workspace;

import io.ballerina.projects.Project;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;

import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/**
 * Ballerina workspace manager proxy implementation.
 * This proxy maintains two workspace managers, one for the expr file scheme based documents and the default manager
 * for the file scheme based documents.
 *
 * @since 2.0.0
 */
public class BallerinaWorkspaceManagerProxy implements WorkspaceManagerProxy {
    private static final String EXPR_SCHEME = "expr";
    private final WorkspaceManager baseWorkspaceManager;
    private final ClonedWorkspace clonedWorkspaceManager;

    public BallerinaWorkspaceManagerProxy(LanguageServerContext serverContext) {
        this.baseWorkspaceManager = new BallerinaWorkspaceManager(serverContext);
        this.clonedWorkspaceManager = new ClonedWorkspace(serverContext);
    }
    
    @Override
    public WorkspaceManager get() {
        return this.baseWorkspaceManager;
    }

    @Override
    public WorkspaceManager get(String fileUri) {
        return URI.create(fileUri).getScheme().equals(EXPR_SCHEME) ?
                this.clonedWorkspaceManager : this.baseWorkspaceManager;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) throws WorkspaceDocumentException {
        String uri = params.getTextDocument().getUri();
        Optional<Path> path = CommonUtil.getPathFromURI(uri);
        if (path.isEmpty()) {
            return;
        }
        this.baseWorkspaceManager.didOpen(path.get(), params);
        if (this.isExprScheme(uri)) {
            Optional<Project> project = this.baseWorkspaceManager.project(path.get());
            project.ifPresent(this.clonedWorkspaceManager::open);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) throws WorkspaceDocumentException {
        String uri = params.getTextDocument().getUri();
        Optional<Path> path = CommonUtil.getPathFromURI(uri);
        if (path.isEmpty()) {
            return;
        }
        if (this.isExprScheme(uri)) {
            this.clonedWorkspaceManager.didChange(path.get(), params);
            return;
        }
        this.baseWorkspaceManager.didChange(path.get(), params);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) throws WorkspaceDocumentException {
        String uri = params.getTextDocument().getUri();
        Optional<Path> path = CommonUtil.getPathFromURI(uri);
        if (path.isEmpty()) {
            return;
        }
        if (this.isExprScheme(uri)) {
            this.clonedWorkspaceManager.didClose(path.get(), params);
            return;
        }
        this.baseWorkspaceManager.didClose(path.get(), params);
    }

    private static class ClonedWorkspace extends BallerinaWorkspaceManager {
        public ClonedWorkspace(LanguageServerContext serverContext) {
            super(serverContext);
        }

        public void open(Project project) {
            this.sourceRootToProject.put(project.sourceRoot(), ProjectPair.from(project.duplicate()));
        }

        @Override
        public void didClose(Path filePath, DidCloseTextDocumentParams params) {
            Optional<Project> project = project(filePath);
            if (project.isEmpty()) {
                return;
            }
            Path projectRoot = project.get().sourceRoot();
            sourceRootToProject.remove(projectRoot);
            this.clientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CLOSE.getName() +
                    "' {project: '" + projectRoot.toUri().toString() +
                    "' kind: '" + project.get().kind().name().toLowerCase(Locale.getDefault()) +
                    "'} removed");
        }
    }

    private boolean isExprScheme(String uri) {
        return URI.create(uri).getScheme().equals(CommonUtil.EXPR_SCHEME);
    }
}
