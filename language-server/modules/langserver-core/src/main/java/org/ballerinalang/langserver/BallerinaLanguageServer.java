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

import com.google.gson.Gson;
import org.ballerinalang.langserver.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.client.ExtendedLanguageClientAware;
import org.ballerinalang.langserver.command.LSCommandExecutorProvidersHolder;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.LSClientLogger;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.extensions.ExtendedLanguageServer;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaDocumentService;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaDocumentServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.example.BallerinaExampleService;
import org.ballerinalang.langserver.extensions.ballerina.example.BallerinaExampleServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.fragment.BallerinaFragmentService;
import org.ballerinalang.langserver.extensions.ballerina.fragment.BallerinaFragmentServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.project.BallerinaProjectService;
import org.ballerinalang.langserver.extensions.ballerina.project.BallerinaProjectServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.semantichighlighter.ScopeEnum;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolService;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceService;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.Listener;
import org.ballerinalang.langserver.extensions.ballerina.traces.ProviderOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.ballerinalang.langserver.Experimental.API_EDITOR_PROVIDER;
import static org.ballerinalang.langserver.Experimental.AST_PROVIDER;
import static org.ballerinalang.langserver.Experimental.EXAMPLES_PROVIDER;
import static org.ballerinalang.langserver.Experimental.INTROSPECTION;
import static org.ballerinalang.langserver.Experimental.SEMANTIC_SCOPES;
import static org.ballerinalang.langserver.Experimental.SEMANTIC_SYNTAX_HIGHLIGHTER;

/**
 * Language server implementation for Ballerina.
 */
public class BallerinaLanguageServer implements ExtendedLanguageServer, ExtendedLanguageClientAware {
    private ExtendedLanguageClient client = null;
    private TextDocumentService textService;
    private WorkspaceService workspaceService;
    private BallerinaDocumentService ballerinaDocumentService;
    private BallerinaProjectService ballerinaProjectService;
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
        LSGlobalContext lsGlobalContext = new LSGlobalContext(LSContextOperation.LS_INIT);
        lsGlobalContext.put(LSGlobalContextKeys.LANGUAGE_SERVER_KEY, this);
        lsGlobalContext.put(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY, documentManager);
        lsGlobalContext.put(LSGlobalContextKeys.DIAGNOSTIC_HELPER_KEY, DiagnosticsHelper.getInstance());
        lsGlobalContext.put(LSGlobalContextKeys.ENABLE_STDLIB_DEFINITION,
                Boolean.valueOf(System.getProperty("ballerina.definition.enableStdlib")));

        this.textService = new BallerinaTextDocumentService(lsGlobalContext);
        this.workspaceService = new BallerinaWorkspaceService(lsGlobalContext);
        this.ballerinaDocumentService = new BallerinaDocumentServiceImpl(lsGlobalContext);
        this.ballerinaProjectService = new BallerinaProjectServiceImpl(lsGlobalContext);
        this.ballerinaExampleService = new BallerinaExampleServiceImpl(lsGlobalContext);
        this.ballerinaTraceService = new BallerinaTraceServiceImpl(lsGlobalContext);
        this.ballerinaTraceListener = new Listener(this.ballerinaTraceService);
        this.ballerinaSymbolService = new BallerinaSymbolServiceImpl();
        this.ballerinaFragmentService = new BallerinaFragmentServiceImpl();

        LSAnnotationCache.initiate();
    }

    public ExtendedLanguageClient getClient() {
        return this.client;
    }

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        final InitializeResult res = new InitializeResult(new ServerCapabilities());
        final SignatureHelpOptions signatureHelpOptions = new SignatureHelpOptions(Arrays.asList("(", ","));
        final List<String> commandList = LSCommandExecutorProvidersHolder.getInstance().getCommandsList();
        final ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions(commandList);
        final CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setTriggerCharacters(Arrays.asList(":", ".", ">", "@"));

        res.getCapabilities().setCompletionProvider(completionOptions);
        res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        res.getCapabilities().setSignatureHelpProvider(signatureHelpOptions);
        res.getCapabilities().setHoverProvider(true);
        res.getCapabilities().setDocumentSymbolProvider(false);
        res.getCapabilities().setDefinitionProvider(true);
        res.getCapabilities().setReferencesProvider(false);
        res.getCapabilities().setCodeActionProvider(true);
        res.getCapabilities().setExecuteCommandProvider(executeCommandOptions);
        res.getCapabilities().setDocumentFormattingProvider(true);
        res.getCapabilities().setRenameProvider(false);
        res.getCapabilities().setWorkspaceSymbolProvider(false);
        res.getCapabilities().setImplementationProvider(false);
//        res.getCapabilities().setCodeLensProvider(new CodeLensOptions());

        TextDocumentClientCapabilities textDocCapabilities = params.getCapabilities().getTextDocument();
        ((BallerinaTextDocumentService) this.textService).setClientCapabilities(textDocCapabilities);

        HashMap experimentalClientCapabilities = null;
        if (params.getCapabilities().getExperimental() != null) {
            experimentalClientCapabilities =
                    new Gson().fromJson(params.getCapabilities().getExperimental().toString(), HashMap.class);
        }

        BallerinaWorkspaceService workspaceService = (BallerinaWorkspaceService) this.workspaceService;
        workspaceService.setExperimentalClientCapabilities(experimentalClientCapabilities);

        // Set AST provider and examples provider capabilities
        HashMap<String, Object> experimentalServerCapabilities = new HashMap<>();
        experimentalServerCapabilities.put(AST_PROVIDER.getValue(), true);
        experimentalServerCapabilities.put(EXAMPLES_PROVIDER.getValue(), true);
        experimentalServerCapabilities.put(API_EDITOR_PROVIDER.getValue(), true);
        if (experimentalClientCapabilities != null &&
                experimentalClientCapabilities.get(INTROSPECTION.getValue()) != null) {
            int port = ballerinaTraceListener.startListener();
            experimentalServerCapabilities.put(INTROSPECTION.getValue(), new ProviderOptions(port));
        }
        if (experimentalClientCapabilities != null &&
                experimentalClientCapabilities.get(SEMANTIC_SYNTAX_HIGHLIGHTER.getValue()) != null) {
            experimentalServerCapabilities.put(SEMANTIC_SYNTAX_HIGHLIGHTER.getValue(), true);
            String[][] scopes = getScopes();
            experimentalServerCapabilities.put(SEMANTIC_SCOPES.getValue(), scopes);
        }
        res.getCapabilities().setExperimental(experimentalServerCapabilities);

        return CompletableFuture.supplyAsync(() -> res);
    }

    public CompletableFuture<Object> shutdown() {
        shutdown = 0;
        ballerinaTraceListener.stopListener();
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
    public BallerinaProjectService getBallerinaProjectService() {
        return this.ballerinaProjectService;
    }

    @Override
    public BallerinaTraceService getBallerinaTraceService() {
        return this.ballerinaTraceService;
    }

    @Override
    public void connect(ExtendedLanguageClient languageClient) {
        this.client = languageClient;
        LSClientLogger.init(this.client, CommonUtil.LS_DEBUG_ENABLED, CommonUtil.LS_TRACE_ENABLED);
    }

    public BallerinaSymbolService getBallerinaSymbolService() {
        return ballerinaSymbolService;
    }

    @Override
    public BallerinaFragmentService getBallerinaFragmentService() {
        return ballerinaFragmentService;
    }
    // Private Methods

    private String[][] getScopes() {
        String[][] scopes = new String[2][1];
        scopes[0][0] = ScopeEnum.ENDPOINT.getScopeName();
        scopes[1][0] = ScopeEnum.UNUSED.getScopeName();
        return scopes;
    }
}
