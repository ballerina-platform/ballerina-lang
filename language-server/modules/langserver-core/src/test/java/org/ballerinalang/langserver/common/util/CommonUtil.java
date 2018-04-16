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
package org.ballerinalang.langserver.common.util;

import com.google.gson.Gson;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
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

import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Common utils that are reused within test suits.
 */
public class CommonUtil {

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
                                                                  String method) throws InterruptedException {
        Gson gson = new Gson();
        BallerinaLanguageServer ballerinaLanguageServer = new BallerinaLanguageServer();
        Endpoint serviceEndpoint = ServiceEndpoints.toEndpoint(ballerinaLanguageServer);
        CompletableFuture result = null;
        switch (method) {
            case "textDocument/hover":
            case "textDocument/definition":
                TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
                TextDocumentIdentifier identifier = new TextDocumentIdentifier();
                identifier.setUri(Paths.get(file).toUri().toString());
                positionParams.setTextDocument(identifier);
                positionParams.setPosition(position);

                DidOpenTextDocumentParams documentParams = new DidOpenTextDocumentParams();
                TextDocumentItem textDocumentItem = new TextDocumentItem();
                textDocumentItem.setUri(identifier.getUri());
                textDocumentItem.setText(fileContent);
                documentParams.setTextDocument(textDocumentItem);

                serviceEndpoint.notify("textDocument/didOpen", documentParams);
                result = serviceEndpoint.request(method, positionParams);
                break;
            case "textDocument/references":
                ReferenceParams referenceParams = new ReferenceParams();

                TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier();
                textDocumentIdentifier.setUri(Paths.get(file).toUri().toString());

                ReferenceContext referenceContext = new ReferenceContext();
                referenceContext.setIncludeDeclaration(true);

                referenceParams.setPosition(position);
                referenceParams.setTextDocument(textDocumentIdentifier);
                referenceParams.setContext(referenceContext);

                DidOpenTextDocumentParams didOpenTextDocumentParams = new DidOpenTextDocumentParams();
                TextDocumentItem textDocument = new TextDocumentItem();
                textDocument.setUri(textDocumentIdentifier.getUri());
                textDocument.setText(fileContent);
                didOpenTextDocumentParams.setTextDocument(textDocument);

                serviceEndpoint.notify("textDocument/didOpen", didOpenTextDocumentParams);
                result = serviceEndpoint.request(method, referenceParams);
                break;
            default:
                break;
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
}
