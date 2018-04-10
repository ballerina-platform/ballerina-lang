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

import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManagerImpl;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Language server implementation for Ballerina.
 */
public class BallerinaLanguageServer implements LanguageServer, LanguageClientAware {
    private LanguageClient client = null;
    private TextDocumentService textService;
    private WorkspaceService workspaceService;
    private int shutdown = 1;

    public BallerinaLanguageServer() {
        LSGlobalContext lsGlobalContext = new LSGlobalContext();
        lsGlobalContext.put(LSGlobalContextKeys.LANGUAGE_SERVER_KEY, this);
        lsGlobalContext.put(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY, WorkspaceDocumentManagerImpl.getInstance());
        LSAnnotationCache.initiate();
        
        textService = new BallerinaTextDocumentService(lsGlobalContext);
        workspaceService = new BallerinaWorkspaceService(lsGlobalContext);
    }

    public LanguageClient getClient() {
        return this.client;
    }

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        final InitializeResult res = new InitializeResult(new ServerCapabilities());
        final SignatureHelpOptions signatureHelpOptions = new SignatureHelpOptions(Arrays.asList("(", ","));
        final List<String> commandList = new ArrayList<>(Arrays.asList(CommandConstants.CMD_IMPORT_PACKAGE,
                CommandConstants.CMD_ADD_DOCUMENTATION, CommandConstants.CMD_ADD_ALL_DOC));
        final ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions(commandList);
        final CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setTriggerCharacters(Arrays.asList(":", ".", ">", "@"));
        
        res.getCapabilities().setCompletionProvider(completionOptions);
        res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        res.getCapabilities().setSignatureHelpProvider(signatureHelpOptions);
        res.getCapabilities().setHoverProvider(true);
        res.getCapabilities().setDocumentSymbolProvider(true);
        res.getCapabilities().setDefinitionProvider(true);
        res.getCapabilities().setReferencesProvider(true);
        res.getCapabilities().setCodeActionProvider(true);
        res.getCapabilities().setExecuteCommandProvider(executeCommandOptions);
        res.getCapabilities().setDocumentFormattingProvider(true);
        res.getCapabilities().setRenameProvider(true);

        return CompletableFuture.supplyAsync(() -> res);
    }

    public CompletableFuture<Object> shutdown() {
        shutdown = 0;
        return CompletableFuture.supplyAsync(Object::new);
    }

    public void exit() {
        System.exit(shutdown);
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

