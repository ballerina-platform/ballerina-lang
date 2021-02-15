/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.command.visitors.FunctionCallExpressionTypeFinder;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the command executor for creating a function.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class CreateFunctionExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CREATE_FUNC";

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    @Override
    public Object execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        String uri = null;
        Position position = null;
        for (CommandArgument arg : context.getArguments()) {
            switch (arg.key()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    uri = arg.valueAs(String.class);
                    break;
                case CommandConstants.ARG_KEY_NODE_POS:
                    position = arg.valueAs(Position.class);
                    break;
                default:
            }
        }

        Optional<Path> filePath = CommonUtil.getPathFromURI(uri);
        if (position == null || filePath.isEmpty()) {
            throw new LSCommandExecutorException("Invalid parameters received for the create function command!");
        }

        SyntaxTree syntaxTree = context.workspace().syntaxTree(filePath.get()).orElseThrow();
        NonTerminalNode cursorNode = CommonUtil.findNode(new Range(position, position), syntaxTree);
        // TODO: We can replace the following with a visitor later since the same logic is used in the code action
        FunctionCallExpressionNode fnCallExprNode = null;
        if (cursorNode != null) {
            if (cursorNode.kind() == SyntaxKind.FUNCTION_CALL) {
                fnCallExprNode = (FunctionCallExpressionNode) cursorNode;
            } else if (cursorNode.kind() == SyntaxKind.LOCAL_VAR_DECL) {
                VariableDeclarationNode varNode = (VariableDeclarationNode) cursorNode;
                Optional<ExpressionNode> initializer = varNode.initializer();
                if (initializer.isPresent() && initializer.get().kind() == SyntaxKind.FUNCTION_CALL) {
                    fnCallExprNode = (FunctionCallExpressionNode) initializer.get();
                }
            } else if (cursorNode.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
                AssignmentStatementNode assignmentNode = (AssignmentStatementNode) cursorNode;
                if (assignmentNode.expression().kind() == SyntaxKind.FUNCTION_CALL) {
                    fnCallExprNode = (FunctionCallExpressionNode) assignmentNode.expression();
                }
            } else if (cursorNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) cursorNode;
                if (nameReferenceNode.parent().kind() == SyntaxKind.FUNCTION_CALL) {
                    fnCallExprNode = (FunctionCallExpressionNode) nameReferenceNode.parent();
                }
            }
        }

        if (fnCallExprNode == null) {
            return new LSCommandExecutorException("Couldn't find a matching node");
        }

        SemanticModel semanticModel = context.workspace().semanticModel(filePath.get()).orElseThrow();

        FunctionCallExpressionTypeFinder typeFinder = new FunctionCallExpressionTypeFinder(semanticModel);
        TypeSymbol returnTypeSymbol = typeFinder.typeOf(fnCallExprNode).orElse(null);

        // Return type symbol of kind compilation error is treated as void
        if (returnTypeSymbol == null) {
            return Collections.emptyList();
        }

        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        Document document = context.workspace().document(filePath.get()).orElseThrow();
        LinePosition linePosition = LinePosition.from(endLine, endCol);
        Set<String> visibleSymbolNames = semanticModel.visibleSymbols(document, linePosition).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        CodeActionContext codeActionContext = ContextBuilder.buildCodeActionContext(uri, context.workspace(),
                context.languageServercontext(), new CodeActionParams());

        List<String> args = new ArrayList<>();
        int argIndex = 1;
        for (FunctionArgumentNode fnArgNode : fnCallExprNode.arguments()) {
            TypeSymbol type = semanticModel.type(fnArgNode).orElse(null);

            String varName = CommonUtil.generateName(argIndex, visibleSymbolNames);
            if (type != null && type.typeKind() != TypeDescKind.COMPILATION_ERROR) {
                args.add(FunctionGenerator.getParameterTypeAsString(codeActionContext, type) + " " + varName);
            } else {
                args.add(FunctionGenerator.getParameterTypeAsString(codeActionContext, null) + " " + varName);
            }

            visibleSymbolNames.add(varName);
            argIndex++;
        }

        if (fnCallExprNode.functionName().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Collections.emptyList();
        }

        String functionName = ((SimpleNameReferenceNode) fnCallExprNode.functionName()).name().text();
        if (functionName == null || functionName.isEmpty()) {
            return Collections.emptyList();
        }

        // return
        String function = FunctionGenerator.generateFunction(codeActionContext, !newLineAtEnd, functionName,
                args, returnTypeSymbol);

        LanguageClient client = context.getLanguageClient();
        List<TextEdit> edits = new ArrayList<>();
        Range range = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        edits.add(new TextEdit(range, function));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits);
        return CommandUtil.applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
