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
package org.ballerinalang.langserver.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaProjectParams;
import org.ballerinalang.langserver.extensions.ballerina.document.SyntaxTreeNodeRequest;
import org.ballerinalang.langserver.extensions.ballerina.packages.PackageComponentsRequest;
import org.ballerinalang.langserver.extensions.ballerina.packages.PackageMetadataRequest;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionContext;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.CompletionTriggerKind;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ExecuteCommandCapabilities;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Common utils that are reused within test suits.
 */
public class TestUtil {

    private static final String HOVER = "textDocument/hover";

    private static final String CODELENS = "textDocument/codeLens";

    private static final String COMPLETION = "textDocument/completion";

    private static final String SIGNATURE_HELP = "textDocument/signatureHelp";

    private static final String DEFINITION = "textDocument/definition";

    private static final String REFERENCES = "textDocument/references";

    private static final String PREPARE_RENAME = "textDocument/prepareRename";

    private static final String RENAME = "textDocument/rename";

    private static final String EXECUTE_COMMAND = "workspace/executeCommand";

    private static final String CODE_ACTION = "textDocument/codeAction";

    private static final String FORMATTING = "textDocument/formatting";

    private static final String RANGE_FORMATTING = "textDocument/rangeFormatting";

    private static final String IMPLEMENTATION = "textDocument/implementation";

    private static final String DOCUMENT_SYMBOL = "textDocument/documentSymbol";

    private static final String FOLDING_RANGE = "textDocument/foldingRange";

    private static final String WORKSPACE_SYMBOL_COMMAND = "workspace/symbol";

    private static final String PACKAGE_METADATA = "ballerinaPackage/metadata";

    private static final String PACKAGE_COMPONENTS = "ballerinaPackage/components";

    private static final String DOCUMENT_SYNTAX_TREE_NODE = "ballerinaDocument/syntaxTreeNode";

    private static final String DOCUMENT_EXEC_POSITIONS = "ballerinaDocument/executorPositions";

    private static final Gson GSON = new Gson();

    private TestUtil() {
    }

    /**
     * Get the textDocument/hover response.
     *
     * @param filePath        Path of the Bal file
     * @param position        Cursor Position
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static String getHoverResponse(String filePath, Position position, Endpoint serviceEndpoint) {
        CompletableFuture<?> result = serviceEndpoint.request(HOVER, getHoverParams(filePath, position));
        return getResponseString(result);
    }

    /**
     * Get the textDocument/codeLens response.
     *
     * @param filePath        Path of the Bal file
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static String getCodeLensesResponse(String filePath, Endpoint serviceEndpoint) {
        TextDocumentIdentifier identifier = getTextDocumentIdentifier(filePath);
        CodeLensParams codeLensParams = new CodeLensParams(identifier);
        CompletableFuture<?> result = serviceEndpoint.request(CODELENS, codeLensParams);
        return getResponseString(result);
    }

    /**
     * Get the textDocument/completion response.
     *
     * @param filePath    Path of the Bal file
     * @param position    Cursor Position
     * @param endpoint    Service Endpoint to Language Server
     * @param triggerChar trigger character
     * @return {@link String}   Response as String
     */
    public static String getCompletionResponse(String filePath, Position position, Endpoint endpoint,
                                               String triggerChar) {
        CompletableFuture<?> result =
                endpoint.request(COMPLETION, getCompletionParams(filePath, position, triggerChar));
        return getResponseString(result);
    }

    /**
     * Get the textDocument/signatureHelp response.
     *
     * @param filePath        Path of the Bal file
     * @param position        Cursor Position
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static String getSignatureHelpResponse(String filePath, Position position, Endpoint serviceEndpoint) {
        CompletableFuture<?> result =
                serviceEndpoint.request(SIGNATURE_HELP, getSignatureParams(filePath, position));
        return getResponseString(result);
    }

    /**
     * Get the textDocument/definition response.
     *
     * @param filePath        Path of the Bal file
     * @param position        Cursor Position
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static String getDefinitionResponse(String filePath, Position position, Endpoint serviceEndpoint) {
        CompletableFuture<?> result = serviceEndpoint.request(DEFINITION, getDefinitionParams(filePath, position));
        return getResponseString(result);
    }

    /**
     * Get the textDocument/reference response.
     *
     * @param filePath        Path of the Bal file
     * @param position        Cursor Position
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static String getReferencesResponse(String filePath, Position position, Endpoint serviceEndpoint) {
        ReferenceParams referenceParams = new ReferenceParams();

        ReferenceContext referenceContext = new ReferenceContext();
        referenceContext.setIncludeDeclaration(true);

        referenceParams.setPosition(new Position(position.getLine(), position.getCharacter()));
        referenceParams.setTextDocument(getTextDocumentIdentifier(filePath));
        referenceParams.setContext(referenceContext);

        CompletableFuture<?> result = serviceEndpoint.request(REFERENCES, referenceParams);
        return getResponseString(result);
    }

    /**
     * Get the textDocument/prepareRename response.
     *
     * @param filePath        Path of the Bal file
     * @param position        Cursor Position
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static String getPrepareRenameResponse(String filePath, Position position, Endpoint serviceEndpoint) {
        PrepareRenameParams renameParams = new PrepareRenameParams();

        renameParams.setTextDocument(getTextDocumentIdentifier(filePath));
        renameParams.setPosition(new Position(position.getLine(), position.getCharacter()));

        CompletableFuture<?> result = serviceEndpoint.request(PREPARE_RENAME, renameParams);
        return getResponseString(result);
    }

    /**
     * Get the textDocument/rename response.
     *
     * @param filePath        Path of the Bal file
     * @param position        Cursor Position
     * @param serviceEndpoint Service Endpoint to Language Server
     * @param newName         newName
     * @return {@link String}   Response as String
     */
    public static String getRenameResponse(String filePath, Position position, String newName,
                                           Endpoint serviceEndpoint) {
        RenameParams renameParams = new RenameParams();

        renameParams.setTextDocument(getTextDocumentIdentifier(filePath));
        renameParams.setNewName(newName);
        renameParams.setPosition(new Position(position.getLine(), position.getCharacter()));

        CompletableFuture<?> result = serviceEndpoint.request(RENAME, renameParams);
        return getResponseString(result);
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
        CompletableFuture<?> result = serviceEndpoint.request(CODE_ACTION, codeActionParams);
        return getResponseString(result);
    }

    /**
     * Get the workspace/executeCommand response.
     *
     * @param params          Execute command parameters
     * @param serviceEndpoint Service endpoint to language server
     * @return {@link String}   Lang server Response as String
     */
    public static String getExecuteCommandResponse(ExecuteCommandParams params, Endpoint serviceEndpoint) {
        CompletableFuture<?> result = serviceEndpoint.request(EXECUTE_COMMAND, params);
        return getResponseString(result);
    }

    /**
     * Get the document symbol Response as String.
     *
     * @param serviceEndpoint Language Server Service Endpoint
     * @param filePath        File Path
     * @return {@link String}       Response string
     */
    public static String getDocumentSymbolResponse(Endpoint serviceEndpoint, String filePath) {
        DocumentSymbolParams params = new DocumentSymbolParams(getTextDocumentIdentifier(filePath));
        CompletableFuture<?> result = serviceEndpoint.request(DOCUMENT_SYMBOL, params);
        return getResponseString(result);
    }

    /**
     * Get formatting response.
     *
     * @param params          Document formatting parameters
     * @param serviceEndpoint Service endpoint to language server
     * @return {@link String} Language server response as String
     */
    public static String getFormattingResponse(DocumentFormattingParams params, Endpoint serviceEndpoint) {
        CompletableFuture<?> result = serviceEndpoint.request(FORMATTING, params);
        return getResponseString(result);
    }

    /**
     * Get formatting response.
     *
     * @param params          Document range formatting parameters
     * @param serviceEndpoint Service endpoint to language server
     * @return {@link String} Language server response as String
     */
    public static String getRangeFormatResponse(DocumentRangeFormattingParams params, Endpoint serviceEndpoint) {
        CompletableFuture<?> result = serviceEndpoint.request(RANGE_FORMATTING, params);
        return getResponseString(result);
    }

    /**
     * Get the Goto implementation response.
     *
     * @param serviceEndpoint Language Server Service endpoint
     * @param filePath        File path to evaluate
     * @param position        Cursor position
     * @return {@link CompletableFuture}    Response completable future
     */
    public static String getGotoImplementationResponse(Endpoint serviceEndpoint, String filePath, Position position) {
        TextDocumentPositionParams positionParams = getTextDocumentPositionParams(filePath, position);
        CompletableFuture<?> completableFuture = serviceEndpoint.request(IMPLEMENTATION, positionParams);
        return getResponseString(completableFuture);
    }

    /**
     * Get folding range response.
     *
     * @param serviceEndpoint Language Server Service endpoint
     * @param filePath        File path to evaluate
     * @return {@link String} Folding range response
     */
    public static String getFoldingRangeResponse(Endpoint serviceEndpoint, String filePath) {
        FoldingRangeRequestParams foldingRangeParams = new
                FoldingRangeRequestParams(getTextDocumentIdentifier(filePath));
        return getResponseString(serviceEndpoint.request(FOLDING_RANGE, foldingRangeParams));
    }

    /**
     * Get package service's metadata response.
     *
     * @param serviceEndpoint Language Server Service endpoint
     * @param filePath        File path to evaluate
     * @return {@link String} Package metadata response
     */
    public static String getPackageMetadataResponse(Endpoint serviceEndpoint, String filePath) {
        PackageMetadataRequest packageMetadataRequest = new PackageMetadataRequest();
        packageMetadataRequest.setDocumentIdentifier(getTextDocumentIdentifier(filePath));
        return getResponseString(serviceEndpoint.request(PACKAGE_METADATA, packageMetadataRequest));
    }

    /**
     * Get package service's components response.
     *
     * @param serviceEndpoint Language Server Service endpoint
     * @param filePaths       List of filePaths to evaluate
     * @return {@link String} Package components response
     */
    public static String getPackageComponentsResponse(Endpoint serviceEndpoint, Iterator<String> filePaths) {
        PackageComponentsRequest packageComponentsRequest = new PackageComponentsRequest();
        List<TextDocumentIdentifier> documentIdentifiers = new ArrayList<>();
        filePaths.forEachRemaining(filePath -> {
            documentIdentifiers.add(getTextDocumentIdentifier(filePath));
        });
        packageComponentsRequest.setDocumentIdentifiers(documentIdentifiers.toArray(new TextDocumentIdentifier[0]));
        return getResponseString(serviceEndpoint.request(PACKAGE_COMPONENTS, packageComponentsRequest));
    }

    /**
     * Returns syntaxTreeNode API response.
     *
     * @param serviceEndpoint Language Server Service endpoint
     * @param filePath        File path to evaluate
     * @param range           Document position
     * @return {@link String} Document syntaxTree node response
     */
    public static String getSyntaxTreeNodeResponse(Endpoint serviceEndpoint, String filePath, Range range) {
        SyntaxTreeNodeRequest request = new SyntaxTreeNodeRequest();
        request.setDocumentIdentifiers(getTextDocumentIdentifier(filePath));
        request.setRange(range);
        return getResponseString(serviceEndpoint.request(DOCUMENT_SYNTAX_TREE_NODE, request));
    }

    /**
     * Returns executorPositions API response.
     *
     * @param serviceEndpoint Language Server Service endpoint
     * @param filePath        File path to evaluate
     * @return {@link String} Document executor positions response
     */
    public static String getExecutorPositionsResponse(Endpoint serviceEndpoint, String filePath) {
        BallerinaProjectParams executorPositionsRequest = new BallerinaProjectParams();
        executorPositionsRequest.setDocumentIdentifier(getTextDocumentIdentifier(filePath));
        return getResponseString(serviceEndpoint.request(DOCUMENT_EXEC_POSITIONS, executorPositionsRequest));
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
     * Initialize the language server instance with given FoldingRangeCapabilities.
     *
     * @return {@link Endpoint}     Service Endpoint
     */
    public static Endpoint initializeLanguageSever() {
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
        Launcher<ExtendedLanguageClient> launcher = Launcher.createLauncher(languageServer,
                ExtendedLanguageClient.class, System.in, OutputStream.nullOutputStream());
        ExtendedLanguageClient client = launcher.getRemoteProxy();
        languageServer.connect(client);
        
        Endpoint endpoint = ServiceEndpoints.toEndpoint(languageServer);
        endpoint.request("initialize", getInitializeParams());
        endpoint.request("initialized", new InitializedParams());

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

    /**
     * Check whether the evalArray is a sublist of checkAgainst Array.
     *
     * @param checkAgainst JsonArray to check against
     * @param evalArray    JsonArray to evaluate
     * @return {@link Boolean}      is Sub array status
     */
    public static boolean isArgumentsSubArray(JsonArray checkAgainst, JsonArray evalArray) {
        for (JsonElement jsonElement : evalArray) {
            if (!checkAgainst.contains(jsonElement)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the workspace symbol response as String.
     *
     * @param serviceEndpoint Language Server Service Endpoint
     * @param query           Symbol query
     * @return {@link String}       Response string
     */
    public static String getWorkspaceSymbolResponse(Endpoint serviceEndpoint, String query) {
        WorkspaceSymbolParams parms = new WorkspaceSymbolParams(query);
        CompletableFuture<?> result = serviceEndpoint.request(WORKSPACE_SYMBOL_COMMAND, parms);
        return getResponseString(result);
    }

    public static TextDocumentIdentifier getTextDocumentIdentifier(String filePath) {
        TextDocumentIdentifier identifier = new TextDocumentIdentifier();
        identifier.setUri(Paths.get(filePath).toUri().toString());

        return identifier;
    }

    private static TextDocumentPositionParams getTextDocumentPositionParams(String filePath, Position position) {
        TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
        positionParams.setTextDocument(getTextDocumentIdentifier(filePath));
        positionParams.setPosition(new Position(position.getLine(), position.getCharacter()));

        return positionParams;
    }

    private static HoverParams getHoverParams(String filePath, Position position) {
        HoverParams hoverParams = new HoverParams();
        hoverParams.setTextDocument(getTextDocumentIdentifier(filePath));
        hoverParams.setPosition(new Position(position.getLine(), position.getCharacter()));

        return hoverParams;
    }

    private static DefinitionParams getDefinitionParams(String filePath, Position position) {
        DefinitionParams definitionParams = new DefinitionParams();
        definitionParams.setTextDocument(getTextDocumentIdentifier(filePath));
        definitionParams.setPosition(new Position(position.getLine(), position.getCharacter()));

        return definitionParams;
    }

    private static SignatureHelpParams getSignatureParams(String filePath, Position position) {
        SignatureHelpParams signatureHelpParams = new SignatureHelpParams();
        signatureHelpParams.setTextDocument(getTextDocumentIdentifier(filePath));
        signatureHelpParams.setPosition(new Position(position.getLine(), position.getCharacter()));

        return signatureHelpParams;
    }

    private static CompletionParams getCompletionParams(String filePath, Position position, String triggerChar) {
        CompletionParams completionParams = new CompletionParams();
        completionParams.setTextDocument(getTextDocumentIdentifier(filePath));
        completionParams.setPosition(new Position(position.getLine(), position.getCharacter()));
        CompletionContext context = new CompletionContext();
        if (triggerChar != null && !triggerChar.isEmpty()) {
            context.setTriggerCharacter(triggerChar);
            context.setTriggerKind(CompletionTriggerKind.TriggerCharacter);
        } else {
            context.setTriggerKind(CompletionTriggerKind.Invoked);
        }
        completionParams.setContext(context);

        return completionParams;
    }

    /**
     * Creates an InitializeParams instance.
     *
     * @return {@link InitializeParams} Params for Language Server initialization
     */
    private static InitializeParams getInitializeParams() {
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
        FoldingRangeCapabilities foldingRangeCapabilities = new FoldingRangeCapabilities();
        foldingRangeCapabilities.setLineFoldingOnly(true);
        textDocumentClientCapabilities.setFoldingRange(foldingRangeCapabilities);
        RenameCapabilities renameCapabilities = new RenameCapabilities();
        renameCapabilities.setPrepareSupport(true);
        renameCapabilities.setHonorsChangeAnnotations(true);
        textDocumentClientCapabilities.setRename(renameCapabilities);

        capabilities.setTextDocument(textDocumentClientCapabilities);

        WorkspaceClientCapabilities workspaceCapabilities = new WorkspaceClientCapabilities();
        workspaceCapabilities.setExecuteCommand(new ExecuteCommandCapabilities(true));

        capabilities.setWorkspace(workspaceCapabilities);
        
        params.setCapabilities(capabilities);
        return params;
    }

    public static String getResponseString(CompletableFuture<?> completableFuture) {
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

        DocumentServiceContext context = ContextBuilder.buildBaseContext(sourcePath.toUri().toString(),
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
}
