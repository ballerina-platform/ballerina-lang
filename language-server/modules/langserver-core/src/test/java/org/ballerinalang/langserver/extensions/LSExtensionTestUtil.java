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
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorListRequest;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorListResponse;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.ASTModification;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeByNameRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeByRangeRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeModifyRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeResponse;
import org.ballerinalang.langserver.extensions.ballerina.document.SyntaxApiCallsRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.SyntaxApiCallsResponse;
import org.ballerinalang.langserver.extensions.ballerina.symbol.SymbolInfoRequest;
import org.ballerinalang.langserver.extensions.ballerina.symbol.SymbolInfoResponse;
import org.ballerinalang.langserver.extensions.ballerina.symbol.TypeFromExpressionRequest;
import org.ballerinalang.langserver.extensions.ballerina.symbol.TypeFromSymbolRequest;
import org.ballerinalang.langserver.extensions.ballerina.symbol.TypesFromExpressionResponse;
import org.ballerinalang.langserver.extensions.ballerina.symbol.TypesFromFnDefinitionRequest;
import org.ballerinalang.langserver.extensions.ballerina.symbol.TypesFromSymbolResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    private static final String GET_SYMBOL = "ballerinaSymbol/getSymbol";
    private static final String SYNTAX_TREE_BY_NAME = "ballerinaDocument/syntaxTreeByName";
    private static final String GET_TYPE_FROM_SYMBOL = "ballerinaSymbol/getTypeFromSymbol";
    private static final String GET_TYPE_FROM_EXPRESSION = "ballerinaSymbol/getTypeFromExpression";
    private static final String GET_TYPE_FROM_FN_DEFINITION = "ballerinaSymbol/getTypesFromFnDefinition";
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

    /**
     * Get the ballerinaDocument/syntaxTreeByName response.
     *
     * @param filePath        Path of the Bal file
     * @param range           Range for which the function should be retrieved
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static BallerinaSyntaxTreeResponse getBallerinaSyntaxTreeByName(String filePath,
                                                                            Range range,
                                                                            Endpoint serviceEndpoint) {
        BallerinaSyntaxTreeByNameRequest request = new BallerinaSyntaxTreeByNameRequest();
        request.setDocumentIdentifier(TestUtil.getTextDocumentIdentifier(filePath));
        request.setLineRange(range);
        CompletableFuture result = serviceEndpoint.request(SYNTAX_TREE_BY_NAME, request);
        return GSON.fromJson(getResult(result), BallerinaSyntaxTreeResponse.class);
    }

    private static JsonObject getResult(CompletableFuture result) {
        return parser.parse(TestUtil.getResponseString(result)).getAsJsonObject().getAsJsonObject("result");
    }

    public static BallerinaConnectorListResponse getConnectors(BallerinaConnectorListRequest request,
                                                               Endpoint serviceEndpoint) {
        CompletableFuture result = serviceEndpoint.request(GET_CONNECTORS, request);
        return GSON.fromJson(getResult(result), BallerinaConnectorListResponse.class);
    }

    public static JsonObject getConnectorById(String id, Endpoint serviceEndpoint) {
        BallerinaConnectorRequest connectorRequest = new BallerinaConnectorRequest(id);
        CompletableFuture result = serviceEndpoint.request(GET_CONNECTOR, connectorRequest);
        return getResult(result);
    }

    public static JsonObject getConnectorByFqn(String org, String packageName, String module, String version,
                                          String name, Endpoint serviceEndpoint) {
        BallerinaConnectorRequest connectorRequest = new BallerinaConnectorRequest(org, packageName, module,
                version, name);
        CompletableFuture result = serviceEndpoint.request(GET_CONNECTOR, connectorRequest);
        return getResult(result);
    }


    public static Path createTempFile(Path filePath) throws IOException {
        Path tempFilePath = FileUtils.BUILD_DIR.resolve("tmp")
                .resolve(UUID.randomUUID() + ".bal");
        Files.copy(filePath, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath;
    }

    /**
     * Get the ballerinaDocument/getSymbol response.
     *
     * @param filePath        Path of the Bal file
     * @param serviceEndpoint Service Endpoint to Language Server
     * @param position        Position of the function to get documentation
     * @return {@link String}   Response as String
     */
    public static SymbolInfoResponse getSymbolDocumentation(String filePath, Position position,
                                                            Endpoint serviceEndpoint) {
        SymbolInfoRequest symbolInfoRequest = new SymbolInfoRequest();
        symbolInfoRequest.setPosition(position);
        symbolInfoRequest.setTextDocumentIdentifier(TestUtil.getTextDocumentIdentifier(filePath));
        CompletableFuture result = serviceEndpoint.request(GET_SYMBOL, symbolInfoRequest);
        return GSON.fromJson(getResult(result), SymbolInfoResponse.class);
    }

    /**
     * Get the ballerinaDocument/getTypeFromSymbol response.
     *
     * @param filePath          Path of the Bal file
     * @param positions         Positions of the symbols to get associated types
     * @param serviceEndpoint   Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static TypesFromSymbolResponse getTypeFromSymbol(URI filePath, LinePosition[] positions,
                                                            Endpoint serviceEndpoint
                                                            ) throws ExecutionException, InterruptedException {
        TypeFromSymbolRequest typeFromSymbolRequest = new TypeFromSymbolRequest();
        typeFromSymbolRequest.setPositions(positions);
        typeFromSymbolRequest.setDocumentIdentifier(TestUtil.getTextDocumentIdentifier(filePath));
        CompletableFuture<?> result = serviceEndpoint.request(GET_TYPE_FROM_SYMBOL, typeFromSymbolRequest);
        return (TypesFromSymbolResponse) result.get();
    }

    /**
     * Get the ballerinaDocument/getTypeFromExpression response.
     *
     * @param filePath          Path of the Bal file
     * @param ranges            Ranges of the expressions to get associated types
     * @param serviceEndpoint   Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static TypesFromExpressionResponse getTypeFromExpression(URI filePath, LineRange[] ranges,
                                                                    Endpoint serviceEndpoint
                                                                    ) throws ExecutionException, InterruptedException {
        TypeFromExpressionRequest typeFromExpressionRequest = new TypeFromExpressionRequest();
        typeFromExpressionRequest.setExpressionRanges(ranges);
        typeFromExpressionRequest.setDocumentIdentifier(TestUtil.getTextDocumentIdentifier(filePath));
        CompletableFuture<?> result = serviceEndpoint.request(GET_TYPE_FROM_EXPRESSION, typeFromExpressionRequest);
        return (TypesFromExpressionResponse) result.get();
    }

    /**
     * Get the ballerinaDocument/getTypesFromFnDefinition response.
     *
     * @param filePath      Path of the Bal file
     * @param fnPosition    Ranges of the expressions to get associated types
     * @param returnTypeDescPosition    Service Endpoint to Language Server
     * @param serviceEndpoint   Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static TypesFromSymbolResponse getTypesFromFnDefinition(URI filePath,
                                                                   LinePosition fnPosition,
                                                                   LinePosition returnTypeDescPosition,
                                                                   Endpoint serviceEndpoint)
            throws ExecutionException, InterruptedException {
        TypesFromFnDefinitionRequest typesFromFnDefRequest = new TypesFromFnDefinitionRequest();
        typesFromFnDefRequest.setFnPosition(fnPosition);
        typesFromFnDefRequest.setReturnTypeDescPosition(returnTypeDescPosition);
        typesFromFnDefRequest.setDocumentIdentifier(TestUtil.getTextDocumentIdentifier(filePath));
        CompletableFuture<?> result = serviceEndpoint.request(GET_TYPE_FROM_FN_DEFINITION, typesFromFnDefRequest);
        return (TypesFromSymbolResponse) result.get();
    }
}
