/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.central.client.model.connector.BalConnector;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorListRequest;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorListResponse;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.ASTModification;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeByRangeRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeModifyRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeResponse;
import org.ballerinalang.langserver.extensions.ballerina.document.SyntaxApiCallsRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.SyntaxApiCallsResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides util methods for testing lang-server extension apis.
 */
public class LSExtensionTestUtil {

    private static final String AST = "ballerinaDocument/syntaxTree";
    private static final String SYNTAX_TREE_MODIFY = "ballerinaDocument/syntaxTreeModify";
    private static final String SYNTAX_TREE_BY_RANGE = "ballerinaDocument/syntaxTreeByRange";
    private static final String SYNTAX_TREE_LOCATE = "ballerinaDocument/syntaxTreeLocate";
    private static final String SYNTAX_API_QUOTE = "ballerinaDocument/syntaxApiCalls";
    private static final String GET_CONNECTORS = "ballerinaConnector/connectors";
    private static final String GET_CONNECTOR = "ballerinaConnector/connector";
    private static final Gson GSON = new Gson();
    private static final JsonParser parser = new JsonParser();

    /**
     * Get the ballerinaDocument/syntaxTree modification response.
     *
     * @param filePath         Path of the Bal file
     * @param astModifications modification to the ast
     * @param serviceEndpoint  Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static BallerinaSyntaxTreeResponse modifyAndGetBallerinaSyntaxTree(String filePath,
                                                                              ASTModification[] astModifications,
                                                                              Endpoint serviceEndpoint) {
        BallerinaSyntaxTreeModifyRequest astModifyRequest = new BallerinaSyntaxTreeModifyRequest(
                TestUtil.getTextDocumentIdentifier(filePath), astModifications);
        CompletableFuture result = serviceEndpoint.request(SYNTAX_TREE_MODIFY, astModifyRequest);
        return GSON.fromJson(getResult(result), BallerinaSyntaxTreeResponse.class);
    }

    /**
     * Get the ballerinaDocument/ast response.
     *
     * @param filePath        Path of the Bal file
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static BallerinaSyntaxTreeResponse getBallerinaSyntaxTree(String filePath, Endpoint serviceEndpoint) {
        BallerinaSyntaxTreeRequest astRequest = new BallerinaSyntaxTreeRequest(
                TestUtil.getTextDocumentIdentifier(filePath));
        CompletableFuture result = serviceEndpoint.request(AST, astRequest);
        return GSON.fromJson(getResult(result), BallerinaSyntaxTreeResponse.class);
    }

    /**
     * Get the ballerinaDocument/syntaxTreeByRange response.
     *
     * @param filePath        Path of the Bal file
     * @param range           Range for which the subtree should be retrieved
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static BallerinaSyntaxTreeResponse getBallerinaSyntaxTreeByRange(String filePath,
                                                                            Range range,
                                                                            Endpoint serviceEndpoint) {
        BallerinaSyntaxTreeByRangeRequest request = new BallerinaSyntaxTreeByRangeRequest();
        request.setDocumentIdentifier(TestUtil.getTextDocumentIdentifier(filePath));
        request.setLineRange(range);
        CompletableFuture result = serviceEndpoint.request(SYNTAX_TREE_BY_RANGE, request);
        return GSON.fromJson(getResult(result), BallerinaSyntaxTreeResponse.class);
    }

    /**
     * Get the ballerinaDocument/syntaxTreeByRange response.
     *
     * @param filePath        Path of the Bal file
     * @param range           Range of the node that should be located
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static BallerinaSyntaxTreeResponse getBallerinaSyntaxTreeLocate(String filePath,
                                                                           Range range,
                                                                           Endpoint serviceEndpoint) {
        BallerinaSyntaxTreeByRangeRequest request = new BallerinaSyntaxTreeByRangeRequest(
                TestUtil.getTextDocumentIdentifier(filePath), range);
        CompletableFuture result = serviceEndpoint.request(SYNTAX_TREE_LOCATE, request);
        return GSON.fromJson(getResult(result), BallerinaSyntaxTreeResponse.class);
    }

    /**
     * Get the ballerinaDocument/syntaxApiCalls response.
     *
     * @param filePath        Path of the Bal file
     * @param ignoreMinutiae  Whether to ignore minutiae in source
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static SyntaxApiCallsResponse getBallerinaSyntaxApiCalls(String filePath, boolean ignoreMinutiae,
                                                                    Endpoint serviceEndpoint) {
        SyntaxApiCallsRequest request = new SyntaxApiCallsRequest(
                TestUtil.getTextDocumentIdentifier(filePath), ignoreMinutiae);
        CompletableFuture<?> result = serviceEndpoint.request(SYNTAX_API_QUOTE, request);
        return GSON.fromJson(getResult(result), SyntaxApiCallsResponse.class);
    }

    private static JsonObject getResult(CompletableFuture result) {
        return parser.parse(TestUtil.getResponseString(result)).getAsJsonObject().getAsJsonObject("result");
    }

    public static BallerinaConnectorListResponse getConnectors(String filePath, String connector,
                                                               Endpoint serviceEndpoint) {
        BallerinaConnectorListRequest connectorListRequest = new BallerinaConnectorListRequest(filePath, connector);
        CompletableFuture result = serviceEndpoint.request(GET_CONNECTORS, connectorListRequest);
        return GSON.fromJson(getResult(result), BallerinaConnectorListResponse.class);
    }

    public static BalConnector getConnector(String id, String org, String module, String version,
                                            String name, Endpoint serviceEndpoint) {
        BallerinaConnectorRequest connectorRequest = new BallerinaConnectorRequest(id, org, module, version, name);
        CompletableFuture result = serviceEndpoint.request(GET_CONNECTOR, connectorRequest);
        return GSON.fromJson(getResult(result), BalConnector.class);
    }

    public static Path createTempFile(Path filePath) throws IOException {
        Path tempFilePath = FileUtils.BUILD_DIR.resolve("tmp")
                .resolve(UUID.randomUUID() + ".bal");
        Files.copy(filePath, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath;
    }

}
