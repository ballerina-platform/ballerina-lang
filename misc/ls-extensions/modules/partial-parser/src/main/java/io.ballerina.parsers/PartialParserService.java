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
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static io.ballerina.parsers.Constants.SPACE_COUNT_FOR_ST_TAB;


/**
 * The extended service for the PartialParser endpoint.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("partialParser")
public class PartialParserService implements ExtendedLanguageServerService {
    private LanguageServerContext serverContext;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForSingleStatement(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {

            String statement = STModificationUtil.getModifiedStatement(request.getCodeSnippet(),
                    request.getStModification());
            String formattedSourceCode = getFunctionBodiedFormattedSource(statement);
            String sourceToBeParsed = getLinesWithoutLeadingTab(formattedSourceCode);

            StatementNode statementNode = NodeParser.parseStatement(sourceToBeParsed);

            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(statementNode);
            STResponse response = new STResponse();
            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForExpression(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String statement = STModificationUtil.getModifiedStatement(request.getCodeSnippet(),
                    request.getStModification());
            String formattedSourceCode = getFunctionBodiedFormattedSource(statement);
            String sourceToBeParsed = getLinesWithoutLeadingTab(formattedSourceCode);

            ExpressionNode expressionNode = NodeParser.parseExpression(sourceToBeParsed);
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
            String formattedSourceCode = getModuleMemberFormattedSource(statement);

            ModuleMemberDeclarationNode expressionNode = NodeParser.parseModuleMemberDeclaration(formattedSourceCode);
            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(expressionNode);
            STResponse response = new STResponse();
            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForModulePart(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String statement = STModificationUtil.getModifiedStatement(request.getCodeSnippet(),
                    request.getStModification());
            String formattedSourceCode = getModuleMemberFormattedSource(statement);

            ModulePartNode modulePartNode = SyntaxTree.from(TextDocuments.from(formattedSourceCode)).rootNode();
            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(modulePartNode);
            STResponse response = new STResponse();
            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @JsonRequest
    public CompletableFuture<STResponse> getSTForResource(PartialSTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String serviceMemberSource = "service / on new http:Listener(9090) {" + request.getCodeSnippet() + "}";
            String statement = STModificationUtil.getModifiedStatement(serviceMemberSource,
                    request.getStModification());
            String formattedSourceCode = getModuleMemberFormattedSource(statement);

            ModuleMemberDeclarationNode moduleMemberDeclaration = NodeParser.
                    parseModuleMemberDeclaration(formattedSourceCode);
            ServiceDeclarationNode serviceDeclaration = (ServiceDeclarationNode) moduleMemberDeclaration;

            JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON((FunctionDefinitionNode) (serviceDeclaration.
                    members().get(0)));
            STResponse response = new STResponse();
            response.setSyntaxTree(syntaxTreeJSON);
            return response;
        });
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }

    private String getModuleMemberFormattedSource(String statement) {

        String formattedSourceCode = statement;

        try {
            formattedSourceCode = Formatter.format(statement);
        } catch (FormatterException e) {
            String msg = "[Warn] Failed to apply formatting before parsing module member";
            this.serverContext.get(ExtendedLanguageClient.class).logMessage(new MessageParams(MessageType.Error, msg));
        }

        return formattedSourceCode;
    }

    private String getFunctionBodiedFormattedSource(String statement) {

        SyntaxTree syntaxTree = getSTForSourceWithinMain(statement);
        String formattedSourceCode = statement;

        try {
            SyntaxTree formattedTree = Formatter.format(syntaxTree);
            ModulePartNode modulePartNode = formattedTree.rootNode();
            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) modulePartNode.members().get(0);
            FunctionBodyBlockNode functionBodyBlockNode = (FunctionBodyBlockNode) functionDefinitionNode.functionBody();
            formattedSourceCode = functionBodyBlockNode.statements().get(0).toSourceCode();
        } catch (FormatterException | NullPointerException e) {
            String msg = "[Warn] Failed to apply formatting before parsing statement";
            this.serverContext.get(ExtendedLanguageClient.class).logMessage(new MessageParams(MessageType.Error, msg));
        }

        return formattedSourceCode;
    }

    private String getLinesWithoutLeadingTab(String statement) {

        StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
        Stream<String> lines = statement.lines();
        lines.iterator().forEachRemaining(line -> {
            // Drop the first four whitespaces
            sj.add(line.replaceFirst(String.format("^ {%d}", SPACE_COUNT_FOR_ST_TAB), ""));
        });

        return sj.toString();
    }

    private SyntaxTree getSTForSourceWithinMain(String statement) {
        String source = String.format("public function main() { %s };", statement);
        TextDocument textDocument = TextDocuments.from(source);
        return SyntaxTree.from(textDocument);
    }
}
