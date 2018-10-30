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
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.extensions.ExtendedLanguageServer;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaDocumentService;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaDocumentServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.example.BallerinaExampleService;
import org.ballerinalang.langserver.extensions.ballerina.example.BallerinaExampleServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.fragment.BallerinaFragmentService;
import org.ballerinalang.langserver.extensions.ballerina.fragment.BallerinaFragmentServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolService;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceService;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.Listener;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Language server implementation for Ballerina.
 */
public class BallerinaLanguageServer implements ExtendedLanguageServer, LanguageClientAware {
    private LSIndexImpl lsIndex = null;
    private LanguageClient client = null;
    private TextDocumentService textService;
    private WorkspaceService workspaceService;
    private BallerinaDocumentService ballerinaDocumentService;
    private BallerinaExampleService ballerinaExampleService;
    private BallerinaTraceService ballerinaTraceService;
    private Listener ballerinaTraceListener;
    private BallerinaSymbolService ballerinaSymbolService;
    private BallerinaFragmentService ballerinaFragmentService;
    private int shutdown = 1;

    public BallerinaLanguageServer() {
        this(WorkspaceDocumentManagerImpl.getInstance());
    }

    public BallerinaLanguageServer(WorkspaceDocumentManager documentManager) {
        this.lsIndex = initLSIndex();

        LSGlobalContext lsGlobalContext = new LSGlobalContext();
        lsGlobalContext.put(LSGlobalContextKeys.LANGUAGE_SERVER_KEY, this);
        lsGlobalContext.put(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY, documentManager);
        lsGlobalContext.put(LSGlobalContextKeys.DIAGNOSTIC_HELPER_KEY, new DiagnosticsHelper());
        lsGlobalContext.put(LSGlobalContextKeys.LS_INDEX_KEY, this.lsIndex);

        this.textService = new BallerinaTextDocumentService(lsGlobalContext);
        this.workspaceService = new BallerinaWorkspaceService(lsGlobalContext);
        this.ballerinaDocumentService = new BallerinaDocumentServiceImpl(lsGlobalContext);
        this.ballerinaExampleService = new BallerinaExampleServiceImpl(lsGlobalContext);
        this.ballerinaTraceService = new BallerinaTraceServiceImpl(lsGlobalContext);
        this.ballerinaTraceListener = new Listener(this.ballerinaTraceService);
        this.ballerinaSymbolService = new BallerinaSymbolServiceImpl(lsGlobalContext);
        this.ballerinaFragmentService = new BallerinaFragmentServiceImpl(lsGlobalContext);

        LSAnnotationCache.initiate();
    }

    public LanguageClient getClient() {
        return this.client;
    }

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        final InitializeResult res = new InitializeResult(new ServerCapabilities());
        final SignatureHelpOptions signatureHelpOptions = new SignatureHelpOptions(Arrays.asList("(", ","));
        final List<String> commandList = new ArrayList<>(Arrays.asList(
                CommandConstants.CMD_IMPORT_MODULE,
                CommandConstants.CMD_ADD_DOCUMENTATION,
                CommandConstants.CMD_ADD_ALL_DOC,
                CommandConstants.CMD_CREATE_FUNCTION,
                CommandConstants.CMD_CREATE_VARIABLE,
                CommandConstants.CMD_CREATE_CONSTRUCTOR,
                CommandConstants.CMD_PULL_MODULE));
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
        res.getCapabilities().setWorkspaceSymbolProvider(true);
        
        TextDocumentClientCapabilities textDocCapabilities = params.getCapabilities().getTextDocument();
        ((BallerinaTextDocumentService) this.textService).setClientCapabilities(textDocCapabilities);

        HashMap<String, Boolean> experimentalCapabilities =
                (HashMap<String, Boolean>) params.getCapabilities().getExperimental();

        if (experimentalCapabilities != null && experimentalCapabilities.get("introspection")) {
            ballerinaTraceListener.startListener();
        }

        // Set AST provider and examples provider capabilities
        HashMap<String, Boolean> experimentalServerCapabilities = new HashMap<String, Boolean>();
        experimentalServerCapabilities.put("astProvider", true);
        experimentalServerCapabilities.put("examplesProvider", true);
        res.getCapabilities().setExperimental(experimentalServerCapabilities);

        return CompletableFuture.supplyAsync(() -> res);
    }

    public CompletableFuture<Object> shutdown() {
        shutdown = 0;
        ballerinaTraceListener.stopListener();
        lsIndex.closeConnection();
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

    public BallerinaDocumentService getBallerinaDocumentService() {
        return this.ballerinaDocumentService;
    }
    @Override
    public BallerinaExampleService getBallerinaExampleService() {
        return this.ballerinaExampleService;
    }

    @Override
    public BallerinaTraceService getBallerinaTraceService() {
        return this.ballerinaTraceService;
    }

    @Override
    public void connect(LanguageClient languageClient) {
        this.client = languageClient;
    }

    public BallerinaSymbolService getBallerinaSymbolService() {
        return ballerinaSymbolService;
    }

    @Override
    public BallerinaFragmentService getBallerinaFragmentService() {
        return ballerinaFragmentService;
    }

    // Private Methods

    private LSIndexImpl initLSIndex() {
        String indexDumpPath = Paths.get(CommonUtil.BALLERINA_HOME + "/lib/tools/lang-server/resources/" +
                "lang-server-index.sql").toString();
        return new LSIndexImpl(indexDumpPath);
    }

}

