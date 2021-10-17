/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.datamapper.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.ExecuteCommandCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.SemanticTokensCapabilities;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Common utils that are reused within test suits.
 */
public class TestUtil {

    private static final String CODE_ACTION = "textDocument/codeAction";

    private static final String WORKSPACE_CONFIG_CHANGE = "workspace/didChangeConfiguration";

    private static final Gson GSON = new Gson();

    private TestUtil() {
    }

    /**
     * Get Code Action Response as String.
     *
     * @param serviceEndpoint Language Server Service endpoint
     * @param filePath        File path for the current file
     * @param range           Cursor range
     * @param context         Code Action Context
     * @return {@link String}       code action response as a string
     */
    public static String getCodeActionResponse(Endpoint serviceEndpoint, String filePath, Range range,
                                               CodeActionContext context) {
        TextDocumentIdentifier identifier = getTextDocumentIdentifier(filePath);
        CodeActionParams codeActionParams = new CodeActionParams(identifier, range, context);
        CompletableFuture result = serviceEndpoint.request(CODE_ACTION, codeActionParams);
        return getResponseString(result);
    }

    /**
     * Open a document.
     *
     * @param serviceEndpoint Language Server Service Endpoint
     * @param filePath        Path of the document to open
     * @throws IOException Exception while reading the file content
     */
    public static void openDocument(Endpoint serviceEndpoint, Path filePath) throws IOException {
        DidOpenTextDocumentParams documentParams = new DidOpenTextDocumentParams();
        TextDocumentItem textDocumentItem = new TextDocumentItem();
        TextDocumentIdentifier identifier = new TextDocumentIdentifier();

        byte[] encodedContent = Files.readAllBytes(filePath);
        identifier.setUri(filePath.toUri().toString());
        textDocumentItem.setUri(identifier.getUri());
        textDocumentItem.setText(new String(encodedContent));
        documentParams.setTextDocument(textDocumentItem);

        serviceEndpoint.notify("textDocument/didOpen", documentParams);
    }

    /**
     * Close an already opened document.
     *
     * @param serviceEndpoint Service Endpoint to Language Server
     * @param filePath        File path of the file to be closed
     */
    public static void closeDocument(Endpoint serviceEndpoint, Path filePath) {
        TextDocumentIdentifier documentIdentifier = new TextDocumentIdentifier();
        documentIdentifier.setUri(filePath.toUri().toString());
        serviceEndpoint.notify("textDocument/didClose", new DidCloseTextDocumentParams(documentIdentifier));
    }

    /**
     * Initialize the language server instance to use.
     *
     * @return {@link Endpoint}     Service Endpoint
     */
    public static Endpoint initializeLanguageSever() {
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
        Endpoint endpoint = ServiceEndpoints.toEndpoint(languageServer);
        InitializeParams params = new InitializeParams();
        ClientCapabilities capabilities = new ClientCapabilities();
        TextDocumentClientCapabilities textDocumentClientCapabilities = new TextDocumentClientCapabilities();
        CompletionCapabilities completionCapabilities = new CompletionCapabilities();
        SignatureHelpCapabilities signatureHelpCapabilities = new SignatureHelpCapabilities();
        SignatureInformationCapabilities sigInfoCapabilities =
                new SignatureInformationCapabilities(Arrays.asList("markdown", "plaintext"));

        signatureHelpCapabilities.setSignatureInformation(sigInfoCapabilities);
        completionCapabilities.setCompletionItem(new CompletionItemCapabilities(true));

        textDocumentClientCapabilities.setCompletion(completionCapabilities);
        textDocumentClientCapabilities.setSignatureHelp(signatureHelpCapabilities);
        textDocumentClientCapabilities.setSemanticTokens(new SemanticTokensCapabilities(true));
        RenameCapabilities renameCapabilities = new RenameCapabilities();
        renameCapabilities.setPrepareSupport(true);
        textDocumentClientCapabilities.setRename(renameCapabilities);

        capabilities.setTextDocument(textDocumentClientCapabilities);

        WorkspaceClientCapabilities workspaceCapabilities = new WorkspaceClientCapabilities();
        workspaceCapabilities.setExecuteCommand(new ExecuteCommandCapabilities(true));
        capabilities.setWorkspace(workspaceCapabilities);
        
        params.setCapabilities(capabilities);
        endpoint.request("initialize", params);

        return endpoint;
    }

    /**
     * Shutdown an already running language server.
     *
     * @param serviceEndpoint Language server Service Endpoint
     */
    public static void shutdownLanguageServer(Endpoint serviceEndpoint) {
        serviceEndpoint.notify("shutdown", null);
    }

    public static TextDocumentIdentifier getTextDocumentIdentifier(String filePath) {
        TextDocumentIdentifier identifier = new TextDocumentIdentifier();
        identifier.setUri(Paths.get(filePath).toUri().toString());

        return identifier;
    }

    public static String getResponseString(CompletableFuture completableFuture) {
        ResponseMessage jsonrpcResponse = new ResponseMessage();
        try {
            jsonrpcResponse.setId("324");
            jsonrpcResponse.setResult(completableFuture.get());
        } catch (InterruptedException e) {
            ResponseError responseError = new ResponseError();
            responseError.setCode(-32002);
            responseError.setMessage("Attempted to retrieve the result of a task/s" +
                    "that was aborted by throwing an exception");
            jsonrpcResponse.setError(responseError);
        } catch (ExecutionException e) {
            ResponseError responseError = new ResponseError();
            responseError.setCode(-32001);
            responseError.setMessage("Current thread was interrupted");
            jsonrpcResponse.setError(responseError);
        }

        return GSON.toJson(jsonrpcResponse).replace("\r\n", "\n").replace("\\r\\n", "\\n");
    }

    public static List<Diagnostic> compileAndGetDiagnostics(Path sourcePath,
                                                            WorkspaceManager workspaceManager,
                                                            LanguageServerContext serverContext)
                                                            throws IOException, WorkspaceDocumentException {
        List<Diagnostic> diagnostics = new ArrayList<>();

        DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(sourcePath.toUri().toString(),
                workspaceManager,
                LSContextOperation.TXT_DID_OPEN,
                serverContext);
        DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
        TextDocumentItem textDocument = new TextDocumentItem();
        textDocument.setUri(sourcePath.toUri().toString());
        textDocument.setText(new String(Files.readAllBytes(sourcePath)));
        params.setTextDocument(textDocument);
        context.workspace().didOpen(sourcePath, params);
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return diagnostics;
        }
        DiagnosticResult diagnosticResult = project.get().currentPackage().getCompilation().diagnosticResult();
        diagnostics.addAll(diagnosticResult.diagnostics());
        return diagnostics;
    }

    /**
     * Get the workspace symbol response as String.
     *
     * @param serviceEndpoint Language Server Service Endpoint
     * @param query           JsonObject query
     */
    public static void setWorkspaceConfig(Endpoint serviceEndpoint, JsonObject query) {
        DidChangeConfigurationParams params = new DidChangeConfigurationParams(query);
        serviceEndpoint.request(WORKSPACE_CONFIG_CHANGE, params);
    }

}
