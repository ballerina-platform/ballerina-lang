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
package io.ballerina.parsers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.BallerinaLanguageServer;

import com.google.gson.Gson;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Common utils that are reused within test suits.
 */
public class TestUtil {

    private static final String SINGLE_STATEMENT = "partialParser/getSTForSingleStatement";

    private static final JsonParser parser = new JsonParser();

    private static final Gson GSON = new Gson();

    private TestUtil() {
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
     * Get the partialParser/getSTForSingleStatement response.
     *
     * @param serviceEndpoint Service Endpoint to Language Server
     * @param statement Statement as string
     * @return {@link String}   Response as String
     */
    public static STResponse getSTForSingleStatement(Endpoint serviceEndpoint, String statement) {
        PartialSTRequest request = new PartialSTRequest(statement);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        return GSON.fromJson(getResult(result), STResponse.class);
    }

    private static JsonObject getResult(CompletableFuture result) {
        return parser.parse(org.ballerinalang.langserver.util.TestUtil.getResponseString(result)).getAsJsonObject().getAsJsonObject("result");
    }
}
