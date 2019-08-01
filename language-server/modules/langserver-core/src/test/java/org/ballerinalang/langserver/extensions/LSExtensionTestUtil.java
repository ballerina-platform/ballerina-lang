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
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaASTRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaASTResponse;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;

import java.util.concurrent.CompletableFuture;

/**
 * Provides util methods for testing lang-server extension apis.
 */
public class LSExtensionTestUtil {

    private static final String GET_AST = "ballerinaDocument/ast";
    private static final Gson GSON = new Gson();
    private static final JsonParser parser = new JsonParser();
    /**
     * Get the ballerinaDocument/ast response.
     *
     * @param filePath        Path of the Bal file
     * @param serviceEndpoint Service Endpoint to Language Server
     * @return {@link String}   Response as String
     */
    public static BallerinaASTResponse getBallerinaDocumentAST(String filePath, Endpoint serviceEndpoint) {
        BallerinaASTRequest astRequest = new BallerinaASTRequest(TestUtil.getTextDocumentIdentifier(filePath));
        CompletableFuture result = serviceEndpoint.request(GET_AST, astRequest);
        return GSON.fromJson(getResult(result), BallerinaASTResponse.class);
    }

    private static JsonObject getResult(CompletableFuture result) {
        return parser.parse(TestUtil.getResponseString(result)).getAsJsonObject().getAsJsonObject("result");
    }
}
