/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class BallerinaLanguageServer implements LanguageServer, LanguageClientAware {
	private LanguageClient client = null;
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

	@Override
	public void connect(LanguageClient languageClient) {
    	this.client = languageClient;
	}
}