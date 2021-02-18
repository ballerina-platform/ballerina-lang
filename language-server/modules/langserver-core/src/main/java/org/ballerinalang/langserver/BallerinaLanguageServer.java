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
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.langserver.command.LSCommandExecutorProvidersHolder;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClientAware;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.extensions.AbstractExtendedLanguageServer;
import org.ballerinalang.langserver.extensions.ExtendedLanguageServer;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorService;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaDocumentService;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaDocumentServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.example.BallerinaExampleService;
import org.ballerinalang.langserver.extensions.ballerina.example.BallerinaExampleServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolService;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceService;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceServiceImpl;
import org.ballerinalang.langserver.extensions.ballerina.traces.Listener;
import org.ballerinalang.langserver.extensions.ballerina.traces.ProviderOptions;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WatchKind;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.ballerinalang.langserver.Experimental.API_EDITOR_PROVIDER;
import static org.ballerinalang.langserver.Experimental.AST_PROVIDER;
import static org.ballerinalang.langserver.Experimental.EXAMPLES_PROVIDER;
import static org.ballerinalang.langserver.Experimental.INTROSPECTION;

/**
 * Language server implementation for Ballerina.
 */
public class BallerinaLanguageServer extends AbstractExtendedLanguageServer
        implements ExtendedLanguageClientAware, ExtendedLanguageServer {
    private ExtendedLanguageClient client = null;
    private final TextDocumentService textService;
    private final WorkspaceService workspaceService;
    private final BallerinaDocumentService ballerinaDocumentService;
    private final BallerinaConnectorService ballerinaConnectorService;
    private final BallerinaExampleService ballerinaExampleService;
    private final BallerinaTraceService ballerinaTraceService;
    private final Listener ballerinaTraceListener;
    private final BallerinaSymbolService ballerinaSymbolService;
    private int shutdown = 1;

    public BallerinaLanguageServer() {
        this(new LanguageServerContextImpl());
    }

    private BallerinaLanguageServer(LanguageServerContext serverContext) {
        super(serverContext);
        this.textService = new BallerinaTextDocumentService(this, workspaceManager, this.serverContext);
        this.workspaceService = new BallerinaWorkspaceService(this, workspaceManager, this.serverContext);
        this.ballerinaDocumentService = new BallerinaDocumentServiceImpl(workspaceManager, this.serverContext);
        this.ballerinaConnectorService = new BallerinaConnectorServiceImpl(this.serverContext);
        this.ballerinaExampleService = new BallerinaExampleServiceImpl(this.serverContext);
        this.ballerinaTraceService = new BallerinaTraceServiceImpl(this);
        this.ballerinaTraceListener = new Listener(this.ballerinaTraceService);
        this.ballerinaSymbolService = new BallerinaSymbolServiceImpl();

        LSAnnotationCache.getInstance(this.serverContext).initiate();
    }

    public ExtendedLanguageClient getClient() {
        return this.client;
    }

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        final InitializeResult res = new InitializeResult(new ServerCapabilities());
        final SignatureHelpOptions signatureHelpOptions = new SignatureHelpOptions(Arrays.asList("(", ","));
        final List<String> commandList = LSCommandExecutorProvidersHolder.getInstance(this.serverContext)
                .getCommandsList();
        final ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions(commandList);
        final CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setTriggerCharacters(Arrays.asList(":", ".", ">", "@"));

        res.getCapabilities().setCompletionProvider(completionOptions);
        res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        res.getCapabilities().setSignatureHelpProvider(signatureHelpOptions);
        res.getCapabilities().setHoverProvider(true);
        res.getCapabilities().setDocumentSymbolProvider(false);
        res.getCapabilities().setDefinitionProvider(true);
        res.getCapabilities().setReferencesProvider(true);
        res.getCapabilities().setCodeActionProvider(true);
        res.getCapabilities().setExecuteCommandProvider(executeCommandOptions);
        res.getCapabilities().setDocumentFormattingProvider(true);
        res.getCapabilities().setDocumentRangeFormattingProvider(true);
        res.getCapabilities().setRenameProvider(true);
        res.getCapabilities().setWorkspaceSymbolProvider(false);
        res.getCapabilities().setImplementationProvider(false);
        res.getCapabilities().setFoldingRangeProvider(true);
        res.getCapabilities().setCodeLensProvider(new CodeLensOptions());

        HashMap experimentalClientCapabilities = null;
        if (params.getCapabilities().getExperimental() != null) {
            experimentalClientCapabilities = new Gson().fromJson(params.getCapabilities().getExperimental().toString(),
                                                                 HashMap.class);
        }

        // Set AST provider and examples provider capabilities
        HashMap<String, Object> experimentalServerCapabilities = new HashMap<>();
        experimentalServerCapabilities.put(AST_PROVIDER.getValue(), true);
        experimentalServerCapabilities.put(EXAMPLES_PROVIDER.getValue(), true);
        experimentalServerCapabilities.put(API_EDITOR_PROVIDER.getValue(), true);

        if (experimentalClientCapabilities != null) {
            Object introspectionObj = experimentalClientCapabilities.get(INTROSPECTION.getValue());
            if (introspectionObj instanceof Boolean && (Boolean) introspectionObj) {
                int port = ballerinaTraceListener.startListener();
                experimentalServerCapabilities.put(INTROSPECTION.getValue(), new ProviderOptions(port));
            }
        }
        res.getCapabilities().setExperimental(experimentalServerCapabilities);


        TextDocumentClientCapabilities textDocClientCapabilities = params.getCapabilities().getTextDocument();
        WorkspaceClientCapabilities workspaceClientCapabilities = params.getCapabilities().getWorkspace();
        LSClientCapabilities capabilities = new LSClientCapabilitiesImpl(textDocClientCapabilities,
                                                                         workspaceClientCapabilities,
                                                                         experimentalClientCapabilities);
        ((BallerinaTextDocumentService) textService).setClientCapabilities(capabilities);
        ((BallerinaWorkspaceService) workspaceService).setClientCapabilities(capabilities);
        return CompletableFuture.supplyAsync(() -> res);
    }

    @Override
    public void initialized(InitializedParams params) {
        startListeningFileChanges();
    }

    public CompletableFuture<Object> shutdown() {
        shutdown = 0;
        ballerinaTraceListener.stopListener();
        for (ExtendedLanguageServerService service : extendedServices) {
            service.shutdown();
        }
        return CompletableFuture.supplyAsync(Object::new);
    }

    public void exit() {
        for (ExtendedLanguageServerService service : extendedServices) {
            service.exit(shutdown);
        }
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
    public BallerinaConnectorService getBallerinaConnectorService() {
        return this.ballerinaConnectorService;
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
    public void connect(ExtendedLanguageClient languageClient) {
        this.client = languageClient;
        this.serverContext.put(ExtendedLanguageClient.class, client);
        LSClientLogger.getInstance(this.serverContext).initialize(this.client, this.serverContext);
    }

    public BallerinaSymbolService getBallerinaSymbolService() {
        return ballerinaSymbolService;
    }

    private void startListeningFileChanges() {
        ExtendedLanguageClient languageClient = serverContext.get(ExtendedLanguageClient.class);
        List<FileSystemWatcher> watchers = new ArrayList<>();
        watchers.add(new FileSystemWatcher("/**/*.bal", WatchKind.Create + WatchKind.Delete));
        watchers.add(new FileSystemWatcher("/**/modules/*", WatchKind.Create + WatchKind.Delete));
        watchers.add(new FileSystemWatcher("/**/modules", WatchKind.Delete));
        watchers.add(
                new FileSystemWatcher("/**/" + ProjectConstants.BALLERINA_TOML, WatchKind.Create + WatchKind.Delete));
        watchers.add(
                new FileSystemWatcher("/**/" + ProjectConstants.CLOUD_TOML, WatchKind.Create + WatchKind.Delete));
        DidChangeWatchedFilesRegistrationOptions opts = new DidChangeWatchedFilesRegistrationOptions(watchers);
        Registration registration = new Registration(UUID.randomUUID().toString(),
                                                     "workspace/didChangeWatchedFiles", opts);
        languageClient.registerCapability(new RegistrationParams(Collections.singletonList(registration)));
    }
}
