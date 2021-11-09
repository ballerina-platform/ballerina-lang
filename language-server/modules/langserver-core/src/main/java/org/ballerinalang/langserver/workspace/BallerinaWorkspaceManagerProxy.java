package org.ballerinalang.langserver.workspace;

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
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
            this.sourceRootToProject.put(project.sourceRoot(), ProjectPair.from(project));
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
