/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonElement;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


/**
 * The extended service for the PartialParser endpoint.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("partialParser")
public class PartialParserService implements ExtendedLanguageServerService {

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForSingleStatement(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            STResponse response = new STResponse();
            NodeList<StatementNode> s = NodeParser.parseStatements(request.getCodeSnippet());

            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(s.get(0));

            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<STListResponse> getSTForStatements(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            STListResponse response = new STListResponse();
            NodeList<StatementNode> s = NodeParser.parseStatements(request.getCodeSnippet());

            ArrayList<JsonElement> elements = new ArrayList<>();
            s.forEach(e -> elements.add(DiagramUtil.getSyntaxTreeJSON(e)));

            response.setSyntaxTree(elements);
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForExpression(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            STResponse response = new STResponse();
            ExpressionNode s = NodeParser.parseExpression(request.getCodeSnippet());

            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(s);

            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }
}
