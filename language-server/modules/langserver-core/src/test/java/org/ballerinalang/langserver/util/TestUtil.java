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
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Common utils that are reused within test suits.
 */
public class TestUtil {

    /**
     * Get the definition response message as a string.
     *
     * @param position    hovering position to get the definition.
     * @param file        bal file path
     * @param fileContent bal file content
     * @param method      string name of the language feature method
     * @return json string value of the response
     */
    public static String getLanguageServerResponseMessageAsString(Position position, String file, String fileContent,
                                                                  String method, Endpoint serviceEndpoint)
            throws InterruptedException {
        Gson gson = new Gson();
        CompletableFuture result = null;
        switch (method) {
            case "textDocument/hover":
            case "textDocument/signatureHelp":
            case "textDocument/definition": {
                TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
                TextDocumentIdentifier identifier = new TextDocumentIdentifier();
                identifier.setUri(Paths.get(file).toUri().toString());
                positionParams.setTextDocument(identifier);

                positionParams.setPosition(new Position(position.getLine(), position.getCharacter()));
                result = serviceEndpoint.request(method, positionParams);
                break;
            }
            case "textDocument/references": {
                ReferenceParams referenceParams = new ReferenceParams();

                TextDocumentIdentifier documentIdentifier = new TextDocumentIdentifier();
                documentIdentifier.setUri(Paths.get(file).toUri().toString());

                ReferenceContext referenceContext = new ReferenceContext();
                referenceContext.setIncludeDeclaration(true);

                referenceParams.setPosition(new Position(position.getLine(), position.getCharacter()));
                referenceParams.setTextDocument(documentIdentifier);
                referenceParams.setContext(referenceContext);

                result = serviceEndpoint.request(method, referenceParams);
                break;
            }
            case "textDocument/formatting": {
                DocumentFormattingParams documentFormattingParams = new DocumentFormattingParams();

                TextDocumentIdentifier textDocumentIdentifier1 = new TextDocumentIdentifier();
                textDocumentIdentifier1.setUri(Paths.get(file).toUri().toString());

                FormattingOptions formattingOptions = new FormattingOptions();
                formattingOptions.setInsertSpaces(true);
                formattingOptions.setTabSize(4);

                documentFormattingParams.setOptions(formattingOptions);
                documentFormattingParams.setTextDocument(textDocumentIdentifier1);

                DidOpenTextDocumentParams didOpenTextDocumentParams1 = new DidOpenTextDocumentParams();
                TextDocumentItem textDocument1 = new TextDocumentItem();
                textDocument1.setUri(textDocumentIdentifier1.getUri());
                textDocument1.setText(fileContent);
                didOpenTextDocumentParams1.setTextDocument(textDocument1);

                serviceEndpoint.notify("textDocument/didOpen", didOpenTextDocumentParams1);
                result = serviceEndpoint.request(method, documentFormattingParams);
                break;
            }
            default: {
                break;
            }
        }

        ResponseMessage jsonrpcResponse = new ResponseMessage();
        try {
            jsonrpcResponse.setId("324");
            jsonrpcResponse.setResult(result.get());
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

        return gson.toJson(jsonrpcResponse);
    }

    /**
     * Open a document.
     * 
     * @param serviceEndpoint   Language Server Service Endpoint
     * @param filePath          Path of the document to open
     * @throws IOException      Exception while reading the file content
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
        return ServiceEndpoints.toEndpoint(new BallerinaLanguageServer());
    }
    
    public static void shutdownLanguageServer(Endpoint serviceEndpoint) {
        serviceEndpoint.notify("shutdown", null);
    }
}
