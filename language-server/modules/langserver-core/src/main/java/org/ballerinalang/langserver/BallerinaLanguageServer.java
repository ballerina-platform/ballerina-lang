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
import com.google.gson.JsonObject;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.langserver.command.LSCommandExecutorProvidersHolder;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClientAware;
import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
import org.ballerinalang.langserver.commons.registration.BallerinaInitializeParams;
import org.ballerinalang.langserver.commons.registration.BallerinaInitializeResult;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.completions.providers.context.util.ServiceTemplateGenerator;
import org.ballerinalang.langserver.config.ClientConfigListener;
import org.ballerinalang.langserver.config.LSClientConfig;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.extensions.AbstractExtendedLanguageServer;
import org.ballerinalang.langserver.extensions.ExtendedLanguageServer;
import org.ballerinalang.langserver.semantictokens.SemanticTokensUtils;
import org.ballerinalang.langserver.util.LSClientUtil;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionRegistrationOptions;
import org.eclipse.lsp4j.DefinitionRegistrationOptions;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.HoverRegistrationOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ReferenceRegistrationOptions;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.RenameOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentChangeRegistrationOptions;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentRegistrationOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WatchKind;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.NotebookDocumentService;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.ballerinalang.langserver.Experimental.API_EDITOR_PROVIDER;
import static org.ballerinalang.langserver.Experimental.AST_PROVIDER;
import static org.ballerinalang.langserver.Experimental.EXAMPLES_PROVIDER;

/**
 * Language server implementation for Ballerina.
 */
public class BallerinaLanguageServer extends AbstractExtendedLanguageServer
        implements ExtendedLanguageClientAware, ExtendedLanguageServer {

    private ExtendedLanguageClient client = null;
    private final TextDocumentService textService;
    private final WorkspaceService workspaceService;
    private final NotebookDocumentService notebookService;
    private int shutdown = 1;

    private static final String LS_ENABLE_SEMANTIC_HIGHLIGHTING = "enableSemanticHighlighting";

    public BallerinaLanguageServer() {
        this(new LanguageServerContextImpl());
    }

    private BallerinaLanguageServer(LanguageServerContext serverContext) {
        super(serverContext);
        this.textService = new BallerinaTextDocumentService(this, workspaceManagerProxy, this.serverContext);
        this.workspaceService = new BallerinaWorkspaceService(this, workspaceManagerProxy, this.serverContext);
        this.notebookService = new BallerinaNotebookDocumentService(this.serverContext);
    }

    public ExtendedLanguageClient getClient() {
        return this.client;
    }

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        final InitializeResult res = new InitializeResult(new ServerCapabilities());
        res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);

        Map experimentalClientCapabilities = null;
        if (params.getCapabilities().getExperimental() != null) {
            experimentalClientCapabilities = new Gson().fromJson(params.getCapabilities().getExperimental().toString(),
                    HashMap.class);
        }

        Map initializationOptions = null;
        if (params.getInitializationOptions() != null) {
            initializationOptions = new Gson().fromJson(params.getInitializationOptions().toString(), HashMap.class);
        }

        TextDocumentClientCapabilities textDocClientCapabilities = params.getCapabilities().getTextDocument();
        WorkspaceClientCapabilities workspaceClientCapabilities = params.getCapabilities().getWorkspace();
        LSClientCapabilities capabilities = new LSClientCapabilitiesImpl(textDocClientCapabilities,
                workspaceClientCapabilities,
                experimentalClientCapabilities,
                initializationOptions);
        this.serverContext.put(LSClientCapabilities.class, capabilities);

        //Checks for instances in which the LS needs to be initiated in lightweight mode
        if (capabilities.getInitializationOptions().isEnableLightWeightMode()) {
            return CompletableFuture.supplyAsync(() -> res);
        }

        final SignatureHelpOptions signatureHelpOptions = new SignatureHelpOptions(Arrays.asList("(", ","));

        res.getCapabilities().setSignatureHelpProvider(signatureHelpOptions);
        res.getCapabilities().setDocumentSymbolProvider(true);
        res.getCapabilities().setDocumentFormattingProvider(true);
        res.getCapabilities().setDocumentRangeFormattingProvider(true);
        res.getCapabilities().setWorkspaceSymbolProvider(false);
        res.getCapabilities().setImplementationProvider(false);
        res.getCapabilities().setFoldingRangeProvider(true);
        res.getCapabilities().setCodeLensProvider(new CodeLensOptions());

        CodeActionOptions codeActionOptions = new CodeActionOptions(List.of(CodeActionKind.Refactor,
                CodeActionKind.QuickFix, CodeActionKind.Source));
        codeActionOptions.setResolveProvider(true);
        res.getCapabilities().setCodeActionProvider(codeActionOptions);

        // Hover, references and definition support will be registered dynamically if supported
        if (!LSClientUtil.isDynamicHoverRegistrationSupported(params.getCapabilities().getTextDocument())) {
            res.getCapabilities().setHoverProvider(true);
        }
        if (!LSClientUtil.isDynamicDefinitionRegistrationSupported(params.getCapabilities().getTextDocument())) {
            res.getCapabilities().setDefinitionProvider(true);
        }
        if (!LSClientUtil.isDynamicReferencesRegistrationSupported(params.getCapabilities().getTextDocument())) {
            res.getCapabilities().setReferencesProvider(true);
        }
        if (!LSClientUtil.isDynamicCompletionRegistrationSupported(params.getCapabilities().getTextDocument())) {
            final CompletionOptions completionOptions = new CompletionOptions();
            completionOptions.setTriggerCharacters(this.getCompletionTriggerCharacters());

            res.getCapabilities().setCompletionProvider(completionOptions);
        }

        // Register LS semantic tokens capabilities if dynamic registration is not available
        if (!LSClientUtil.isDynamicSemanticTokensRegistrationSupported(params.getCapabilities().getTextDocument()) &&
                enableBallerinaSemanticTokens(params)) {
            res.getCapabilities().setSemanticTokensProvider(SemanticTokensUtils.getSemanticTokensRegistrationOptions());
        }

        // Check and set prepare rename provider
        boolean prepareSupport = LSClientUtil.clientSupportsPrepareRename(params.getCapabilities());
        res.getCapabilities().setRenameProvider(new RenameOptions(prepareSupport));

        // Register commands
        List<String> commandsList = LSCommandExecutorProvidersHolder.getInstance(serverContext).getCommandsList();
        ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions(commandsList);
        res.getCapabilities().setExecuteCommandProvider(executeCommandOptions);

        // Set AST provider and examples provider capabilities
        HashMap<String, Object> experimentalServerCapabilities = new HashMap<>();
        experimentalServerCapabilities.put(AST_PROVIDER.getValue(), true);
        experimentalServerCapabilities.put(EXAMPLES_PROVIDER.getValue(), true);
        experimentalServerCapabilities.put(API_EDITOR_PROVIDER.getValue(), true);
        res.getCapabilities().setExperimental(experimentalServerCapabilities);

        this.serverContext.put(ServerCapabilities.class, res.getCapabilities());
        ((BallerinaTextDocumentService) textService).setClientCapabilities(capabilities);
        ((BallerinaWorkspaceService) workspaceService).setClientCapabilities(capabilities);
        return CompletableFuture.supplyAsync(() -> res);
    }

    @Override
    public void initialized(InitializedParams params) {
        LSClientLogger clientLogger = LSClientLogger.getInstance(this.serverContext);
        clientLogger.logMessage("LS offline source compilation set to " + CommonUtil.COMPILE_OFFLINE);

        // Register dynamic capabilities
        registerDynamicCapabilities();

        startListeningFileChanges();

        //Initialize Service Template Generator.
        ServiceTemplateGenerator.getInstance(this.serverContext);
    }

    /**
     * Checks and registers required dynamic capabilities.
     */
    private void registerDynamicCapabilities() {
        registerTextSynchronizationForCustomUriSchemes();

        DocumentFilter balaFilter = new DocumentFilter();
        balaFilter.setScheme(CommonUtil.URI_SCHEME_BALA);

        DocumentFilter fileFilter = new DocumentFilter();
        fileFilter.setScheme(CommonUtil.URI_SCHEME_FILE);
        fileFilter.setLanguage(CommonUtil.LANGUAGE_ID_BALLERINA);

        DocumentFilter fileFilterToml = new DocumentFilter();
        fileFilterToml.setScheme(CommonUtil.URI_SCHEME_FILE);
        fileFilterToml.setLanguage(CommonUtil.LANGUAGE_ID_TOML);

        DocumentFilter exprFilter = new DocumentFilter();
        exprFilter.setScheme(CommonUtil.URI_SCHEME_EXPR);
        exprFilter.setLanguage(CommonUtil.LANGUAGE_ID_BALLERINA);

        List<DocumentFilter> documentSelectors = List.of(balaFilter, fileFilter);

        registerDynamicHoverSupport(documentSelectors);
        registerDynamicDefinitionSupport(documentSelectors);
        registerDynamicReferencesSupport(documentSelectors);
        registerDynamicCompletionSupport(List.of(fileFilter, exprFilter, fileFilterToml));

        registerDynamicSemanticTokenSupport();
    }

    /**
     * "bala" URI scheme is used to make stdlib and langlib files readonly at the editor.
     * "expr" URI scheme is used to make expression editor based use-cases
     */
    private void registerTextSynchronizationForCustomUriSchemes() {
        LanguageClient client = serverContext.get(ExtendedLanguageClient.class);
        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);

        DocumentFilter balaFilter = new DocumentFilter();
        balaFilter.setScheme(CommonUtil.URI_SCHEME_BALA);

        DocumentFilter exprFilter = new DocumentFilter();
        exprFilter.setScheme(CommonUtil.URI_SCHEME_EXPR);

        // Register text synchronization for bala and expr schemes
        if (LSClientUtil.isDynamicSynchronizationRegistrationSupported(clientCapabilities.getTextDocCapabilities())) {
            TextDocumentRegistrationOptions openRegOptions = new TextDocumentRegistrationOptions();
            openRegOptions.setDocumentSelector(List.of(balaFilter, exprFilter));
            Registration didOpenRegistration = new Registration(UUID.randomUUID().toString(),
                    "textDocument/didOpen", openRegOptions);

            TextDocumentChangeRegistrationOptions changeRegOptions = new TextDocumentChangeRegistrationOptions();
            changeRegOptions.setDocumentSelector(List.of(balaFilter, exprFilter));
            changeRegOptions.setSyncKind(TextDocumentSyncKind.Full);
            Registration changeRegistration = new Registration(UUID.randomUUID().toString(),
                    "textDocument/didChange", changeRegOptions);

            TextDocumentRegistrationOptions closeRegOptions = new TextDocumentRegistrationOptions();
            closeRegOptions.setDocumentSelector(List.of(balaFilter, exprFilter));
            Registration closeRegistration = new Registration(UUID.randomUUID().toString(),
                    "textDocument/didClose", closeRegOptions);

            client.registerCapability(new RegistrationParams(List.of(didOpenRegistration)));
            client.registerCapability(new RegistrationParams(List.of(changeRegistration)));
            client.registerCapability(new RegistrationParams(List.of(closeRegistration)));
        }

        // TODO Server capabilities in server context are out of sync now.
    }

    private void registerDynamicHoverSupport(List<DocumentFilter> documentSelectors) {
        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);
        if (LSClientUtil.isDynamicHoverRegistrationSupported(clientCapabilities.getTextDocCapabilities())) {
            HoverRegistrationOptions hoverRegOptions = new HoverRegistrationOptions();
            hoverRegOptions.setDocumentSelector(documentSelectors);
            Registration hoverRegistration = new Registration(UUID.randomUUID().toString(),
                    "textDocument/hover", hoverRegOptions);
            client.registerCapability(new RegistrationParams(List.of(hoverRegistration)));
        }
    }

    private void registerDynamicDefinitionSupport(List<DocumentFilter> documentSelectors) {
        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);
        if (LSClientUtil.isDynamicDefinitionRegistrationSupported(clientCapabilities.getTextDocCapabilities())) {
            DefinitionRegistrationOptions definitionRegistrationOptions = new DefinitionRegistrationOptions();
            definitionRegistrationOptions.setDocumentSelector(documentSelectors);
            Registration definitionRegistration = new Registration(UUID.randomUUID().toString(),
                    "textDocument/definition", definitionRegistrationOptions);
            client.registerCapability(new RegistrationParams(List.of(definitionRegistration)));
        }
    }

    private void registerDynamicCompletionSupport(List<DocumentFilter> documentSelectors) {
        CompletionRegistrationOptions completionRegistrationOptions = new CompletionRegistrationOptions();
        completionRegistrationOptions.setDocumentSelector(documentSelectors);
        completionRegistrationOptions.setTriggerCharacters(this.getCompletionTriggerCharacters());
        Registration completionRegistration = new Registration(UUID.randomUUID().toString(),
                "textDocument/completion", completionRegistrationOptions);
        client.registerCapability(new RegistrationParams(List.of(completionRegistration)));

    }

    private void registerDynamicReferencesSupport(List<DocumentFilter> documentSelectors) {
        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);
        if (LSClientUtil.isDynamicReferencesRegistrationSupported(clientCapabilities.getTextDocCapabilities())) {
            ReferenceRegistrationOptions referencesRegOptions = new ReferenceRegistrationOptions();
            referencesRegOptions.setDocumentSelector(documentSelectors);
            Registration referencesRegistration = new Registration(UUID.randomUUID().toString(),
                    "textDocument/references", referencesRegOptions);
            client.registerCapability(new RegistrationParams(List.of(referencesRegistration)));
        }
    }

    private void registerDynamicSemanticTokenSupport() {
        // Register LS semantic tokens capabilities if dynamic registration is available
        LSClientCapabilities capabilities = this.serverContext.get(LSClientCapabilities.class);
        if (LSClientUtil.isDynamicSemanticTokensRegistrationSupported(capabilities.getTextDocCapabilities())) {
            registerSemanticTokensConfigListener();
            if (capabilities.getWorkspaceCapabilities() != null
                    && capabilities.getWorkspaceCapabilities().getDidChangeConfiguration() == null &&
                    capabilities.getInitializationOptions().isEnableSemanticTokens()) {
                SemanticTokensUtils.registerSemanticTokensCapability(serverContext.get(ExtendedLanguageClient.class));
            }
        }
    }

    public CompletableFuture<Object> shutdown() {
        shutdown = 0;
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

    @Override
    public NotebookDocumentService getNotebookDocumentService() {
        return this.notebookService;
    }

    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    @Override
    public void connect(ExtendedLanguageClient languageClient) {
        this.client = languageClient;
        this.serverContext.put(ExtendedLanguageClient.class, client);
        LSClientLogger.getInstance(this.serverContext).initialize(this.client, this.serverContext);
    }

    @Override
    public CompletableFuture<BallerinaInitializeResult> initBalServices(BallerinaInitializeParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaInitializeResult balInitResult = new BallerinaInitializeResult();
            List<BallerinaClientCapability> balClientCapabilities =
                    ExtendedClientCapabilityBuilder.get(params.getBallerinaClientCapabilities());
            LSClientCapabilities capabilities = this.serverContext.get(LSClientCapabilities.class);
            capabilities.setBallerinaClientCapabilities(balClientCapabilities);
            balInitResult.setExtendedServerCapabilities(ExtendedServerCapabilityBuilder.get());

            return balInitResult;
        });
    }

    /**
     * Register a configuration listener to handle enabling/disabling semantic highlighting dynamically.
     */
    private void registerSemanticTokensConfigListener() {
        LSClientConfigHolder.getInstance(serverContext).register(new ClientConfigListener() {
            @Override
            public void didChangeConfig(LSClientConfig oldConfig, LSClientConfig newConfig) {
                ExtendedLanguageClient languageClient = serverContext.get(ExtendedLanguageClient.class);
                if (newConfig.isEnableSemanticHighlighting()) {
                    SemanticTokensUtils.registerSemanticTokensCapability(languageClient);
                } else {
                    SemanticTokensUtils.unRegisterSemanticTokensCapability(languageClient);
                }
            }
        });
    }

    private void startListeningFileChanges() {
        ExtendedLanguageClient languageClient = serverContext.get(ExtendedLanguageClient.class);
        List<FileSystemWatcher> watchers = new ArrayList<>();
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/*.bal"),
                WatchKind.Create + WatchKind.Delete + WatchKind.Change));
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/modules/*"), WatchKind.Create + WatchKind.Delete));
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/modules"), WatchKind.Delete));
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/generated/*"),
                WatchKind.Create + WatchKind.Delete));
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/generated"), WatchKind.Delete));
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/" + ProjectConstants.BALLERINA_TOML),
                WatchKind.Create + WatchKind.Delete));
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/" + ProjectConstants.CLOUD_TOML),
                WatchKind.Create + WatchKind.Delete));
        watchers.add(new FileSystemWatcher(Either.forLeft("/**/" + ProjectConstants.DEPENDENCIES_TOML),
                WatchKind.Create + WatchKind.Delete));
        DidChangeWatchedFilesRegistrationOptions opts = new DidChangeWatchedFilesRegistrationOptions(watchers);
        Registration registration = new Registration(UUID.randomUUID().toString(),
                "workspace/didChangeWatchedFiles", opts);
        languageClient.registerCapability(new RegistrationParams(Collections.singletonList(registration)));
    }

    private boolean enableBallerinaSemanticTokens(InitializeParams params) {
        if (params.getInitializationOptions() == null) {
            return true;
        }
        JsonObject initOptions = (JsonObject) params.getInitializationOptions();
        if (!initOptions.has(LS_ENABLE_SEMANTIC_HIGHLIGHTING)) {
            return true;
        }
        return initOptions.get(LS_ENABLE_SEMANTIC_HIGHLIGHTING).getAsBoolean();
    }

    private List<String> getCompletionTriggerCharacters() {
        return Arrays.asList(":", ".", ">", "@", "/", "\\", "?");
    }
}
