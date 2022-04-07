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
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.concurrent.CompletableFuture;


/**
 * The extended service for the PartialParser endpoint.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("partialParser")
public class PartialParserService implements ExtendedLanguageServerService {
    private LSClientLogger clientLogger;

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.clientLogger = LSClientLogger.getInstance(serverContext);
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForSingleStatement(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {

            String statement = STModificationUtil.getModifiedStatement(request.getCodeSnippet(),
                    request.getStModification());
            String formattedSourceCode = getFormattedSource(statement);

            StatementNode statementNode = NodeParser.parseStatement(formattedSourceCode);

            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(statementNode);
            STResponse response = new STResponse();
            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForExpression(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ExpressionNode expressionNode = NodeParser.parseExpression(request.getCodeSnippet());
            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(expressionNode);
            STResponse response = new STResponse();
            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForModuleMembers(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {

            String statement = STModificationUtil.getModifiedStatement(request.getCodeSnippet(),
                    request.getStModification());
            String formattedSourceCode = getFormattedSource(statement);

            ModuleMemberDeclarationNode expressionNode = NodeParser.parseModuleMemberDeclaration(formattedSourceCode);
            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(expressionNode);
            STResponse response = new STResponse();
            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }

    private String getFormattedSource(String statement) {

        String formattedSourceCode = statement;

        try {
            formattedSourceCode = Formatter.format(statement);
        } catch (FormatterException e) {
            String msg = "[Warn] Failed to format the statement: " + statement + ", Cause: " + e.getMessage();
            this.clientLogger.logMessage(msg);
        }

        return formattedSourceCode;
    }
}
