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
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderFactory;
import org.ballerinalang.langserver.command.LSCommandExecutorProvider;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSClientLogger;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.completions.LSCompletionProviderFactory;
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
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolService;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter.BallerinaSyntaxHighlightService;
import org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter.BallerinaSyntaxHighlightServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceService;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.Listener;
import org.ballerinalang.langserver.extensions.ballerina.traces.ProviderOptions;
import org.ballerinalang.langserver.index.LSIndexImpl;
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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.ballerinalang.langserver.BallerinaWorkspaceService.Experimental.INTROSPECTION;

/**
 * Language server implementation for Ballerina.
 */
public class BallerinaLanguageServer implements ExtendedLanguageServer, ExtendedLanguageClientAware {
    private LSIndexImpl lsIndex;
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
    private BallerinaSyntaxHighlightService ballerinaSyntaxHighlightService;
    private int shutdown = 1;

    public BallerinaLanguageServer() {
        this(WorkspaceDocumentManagerImpl.getInstance());
    }

    public BallerinaLanguageServer(WorkspaceDocumentManager documentManager) {
        this.lsIndex = initLSIndex();

        LSGlobalContext lsGlobalContext = new LSGlobalContext(LSContextOperation.LS_INIT);
        lsGlobalContext.put(LSGlobalContextKeys.LANGUAGE_SERVER_KEY, this);
        lsGlobalContext.put(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY, documentManager);
        lsGlobalContext.put(LSGlobalContextKeys.DIAGNOSTIC_HELPER_KEY, new DiagnosticsHelper());
        lsGlobalContext.put(LSGlobalContextKeys.LS_INDEX_KEY, this.lsIndex);

        this.textService = new BallerinaTextDocumentService(lsGlobalContext);
        this.workspaceService = new BallerinaWorkspaceService(lsGlobalContext);
        this.ballerinaDocumentService = new BallerinaDocumentServiceImpl(lsGlobalContext);
        this.ballerinaProjectService = new BallerinaProjectServiceImpl(lsGlobalContext);
        this.ballerinaExampleService = new BallerinaExampleServiceImpl(lsGlobalContext);
        this.ballerinaTraceService = new BallerinaTraceServiceImpl(lsGlobalContext);
        this.ballerinaTraceListener = new Listener(this.ballerinaTraceService);
        this.ballerinaSymbolService = new BallerinaSymbolServiceImpl(lsGlobalContext);
        this.ballerinaFragmentService = new BallerinaFragmentServiceImpl(lsGlobalContext);
        this.ballerinaSyntaxHighlightService = new BallerinaSyntaxHighlightServiceImpl();

        LSAnnotationCache.initiate();
        LSCodeLensesProviderFactory.getInstance().initiate();
        LSCompletionProviderFactory.getInstance().initiate();
    }

    public ExtendedLanguageClient getClient() {
        return this.client;
    }

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        final InitializeResult res = new InitializeResult(new ServerCapabilities());
        final SignatureHelpOptions signatureHelpOptions = new SignatureHelpOptions(Arrays.asList("(", ","));
        final List<String> commandList = LSCommandExecutorProvider.getInstance().getCommandsList();
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

        Map<String, Boolean> experimentalClientCapabilities = null;
        if (params.getCapabilities().getExperimental() != null) {
            experimentalClientCapabilities =
                    new Gson().fromJson(params.getCapabilities().getExperimental().toString(), HashMap.class);
        }

        BallerinaWorkspaceService workspaceService = (BallerinaWorkspaceService) this.workspaceService;
        workspaceService.setExperimentalClientCapabilities(experimentalClientCapabilities);

        // Set AST provider and examples provider capabilities
        HashMap<String, Object> experimentalServerCapabilities = new HashMap<>();
        experimentalServerCapabilities.put("astProvider", true);
        experimentalServerCapabilities.put("examplesProvider", true);
        experimentalServerCapabilities.put("apiEditorProvider", true);
        if (experimentalClientCapabilities != null && experimentalClientCapabilities.get(INTROSPECTION.getValue())) {
            int port = ballerinaTraceListener.startListener();
            experimentalServerCapabilities.put("introspection", new ProviderOptions(port));
        }
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
    public BallerinaProjectService getBallerinaProjectService() {
        return this.ballerinaProjectService;
    }

    @Override
    public BallerinaTraceService getBallerinaTraceService() {
        return this.ballerinaTraceService;
    }

    @Override
    public  BallerinaSyntaxHighlightService getBallerinaSyntaxHighlightService() {
        return this.ballerinaSyntaxHighlightService;
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

    private LSIndexImpl initLSIndex() {
        String indexDumpPath = Paths.get(CommonUtil.BALLERINA_HOME + "/lib/tools/lang-server/resources/" +
                "lang-server-index.sql").toString();
        return new LSIndexImpl(indexDumpPath);
    }

}
