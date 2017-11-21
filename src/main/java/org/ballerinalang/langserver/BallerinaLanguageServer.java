package org.ballerinalang.langserver;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class BallerinaLanguageServer implements LanguageServer {
    private TextDocumentService textService;
	private WorkspaceService workspaceService;

    public BallerinaLanguageServer() {
		textService = new BallerinaTextDocumentService(this);
		workspaceService = new BallerinaWorkspaceService();
	}

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		final InitializeResult res = new InitializeResult(new ServerCapabilities());
		res.getCapabilities().setCompletionProvider(new CompletionOptions());
		
		return CompletableFuture.supplyAsync(() -> res);
    }

	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.supplyAsync(() -> Boolean.TRUE);
	}

	public void exit() {
    }
    
    public TextDocumentService getTextDocumentService() {
		return this.textService;
	}

	public WorkspaceService getWorkspaceService() {
		return this.workspaceService;
	}
}    