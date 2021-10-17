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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.codeaction.providers.CreateFunctionCodeAction;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.command.visitors.FunctionCallExpressionTypeFinder;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
        Range range = null;
        for (CommandArgument arg : context.getArguments()) {
            switch (arg.key()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    uri = arg.valueAs(String.class);
                    break;
                case CommandConstants.ARG_KEY_NODE_RANGE:
                    range = arg.valueAs(Range.class);
                    break;
                default:
            }
        }

        Optional<Path> filePath = CommonUtil.getPathFromURI(uri);
        if (range == null || filePath.isEmpty()) {
            throw new UserErrorException("Invalid parameters received for the create function command!");
        }

        SyntaxTree syntaxTree = context.workspace().syntaxTree(filePath.get()).orElseThrow();
        NonTerminalNode cursorNode = CommonUtil.findNode(range, syntaxTree);

        if (cursorNode == null) {
            return Collections.emptyList();
        }

        Optional<FunctionCallExpressionNode> fnCallExprNode =
                CreateFunctionCodeAction.checkAndGetFunctionCallExpressionNode(cursorNode);
        if (fnCallExprNode.isEmpty()) {
            return new UserErrorException("Couldn't find a matching node");
        }

        SemanticModel semanticModel = context.workspace().semanticModel(filePath.get()).orElseThrow();

        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        Set<String> visibleSymbolNames = context.visibleSymbols(filePath.get(), new Position(endLine, endCol))
                .stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        DocumentServiceContext docServiceContext = ContextBuilder.buildDocumentServiceContext(
                uri,
                context.workspace(),
                LSContextOperation.WS_EXEC_CMD,
                context.languageServercontext());

        List<String> args = new ArrayList<>();
        int argIndex = 1;
        List<SymbolKind> argumentKindList = Arrays.asList(SymbolKind.VARIABLE, SymbolKind.CONSTANT);
        for (FunctionArgumentNode fnArgNode : fnCallExprNode.get().arguments()) {
            Optional<TypeSymbol> type = semanticModel.typeOf(fnArgNode);
            Optional<Symbol> symbol;
            if (fnArgNode.kind() == SyntaxKind.POSITIONAL_ARG) {
                symbol = semanticModel.symbol(((PositionalArgumentNode) fnArgNode).expression());
            } else {
                symbol = semanticModel.symbol(fnArgNode);
            }
            String varName = "";
            if (symbol.isPresent() && argumentKindList.contains(symbol.get().kind())) {
                varName = symbol.get().getName().orElse("");
            }

            if (type.isPresent() && type.get().typeKind() != TypeDescKind.COMPILATION_ERROR) {
                varName = CommonUtil.generateParameterName(varName, argIndex,
                        CommonUtil.getRawType(type.get()), visibleSymbolNames);
                args.add(FunctionGenerator.getParameterTypeAsString(docServiceContext, type.get()) + " " + varName);
            } else {
                varName = CommonUtil.generateParameterName(varName, argIndex, null, visibleSymbolNames);
                args.add(FunctionGenerator.getParameterTypeAsString(docServiceContext, null) + " " + varName);
            }
            visibleSymbolNames.add(varName);
            argIndex++;
        }

        if (fnCallExprNode.get().functionName().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Collections.emptyList();
        }

        String functionName = ((SimpleNameReferenceNode) fnCallExprNode.get().functionName()).name().text();
        if (functionName.isEmpty()) {
            return Collections.emptyList();
        }

        LanguageClient client = context.getLanguageClient();
        List<TextEdit> edits = new ArrayList<>();
        Optional<NonTerminalNode> enclosingNode = findEnclosingModulePartNode(fnCallExprNode.get());
        Range insertRange;
        if (enclosingNode.isPresent()) {
            LineRange endLineRange = enclosingNode.get().lineRange();
            newLineAtEnd = false;
            insertRange = new Range(new Position(endLineRange.endLine().line(), endLineRange.endLine().offset()),
                    new Position(endLineRange.endLine().line(), endLineRange.endLine().offset()));

        } else {
            insertRange = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        }

        FunctionCallExpressionTypeFinder typeFinder = new FunctionCallExpressionTypeFinder(semanticModel);
        typeFinder.findTypeOf(fnCallExprNode.get());
        Optional<TypeSymbol> returnTypeSymbol = typeFinder.getReturnTypeSymbol();
        Optional<TypeDescKind> returnTypeDescKind = typeFinder.getReturnTypeDescKind();

        // Generate function. We have to check if we have a return type symbol or a return type desc kind. Depending
        // on that, we need to use separate APIs.
        String function;
        if (returnTypeSymbol.isPresent()) {
            function = FunctionGenerator.generateFunction(docServiceContext, !newLineAtEnd, functionName,
                    args, returnTypeSymbol.get());
        } else if (returnTypeDescKind.isPresent()) {
            function = FunctionGenerator.generateFunction(docServiceContext, !newLineAtEnd, functionName,
                    args, returnTypeDescKind.get());
        } else {
            return Collections.emptyList();
        }

        edits.add(new TextEdit(insertRange, function));
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

    private Optional<NonTerminalNode> findEnclosingModulePartNode(NonTerminalNode node) {
        NonTerminalNode reference = node;
        while (reference != null && reference.parent() != null) {
            if (reference.parent().kind() == SyntaxKind.MODULE_PART) {
                return Optional.of(reference);
            }
            reference = reference.parent();
        }
        return Optional.empty();
    }
}
