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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.command.visitors.FunctionCallExpressionTypeFinder;
import org.ballerinalang.langserver.command.visitors.IsolatedBlockResolver;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionData;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.ResolvableCodeActionProvider;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Code Action for creating undefined function.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateFunctionCodeAction implements DiagnosticBasedCodeActionProvider, ResolvableCodeActionProvider {

    public static final String NAME = "Create Function";

    private static final String UNDEFINED_FUNCTION = "undefined function";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Optional<FunctionCallExpressionNode> callExpr =
                checkAndGetFunctionCallExpressionNode(positionDetails.matchedNode());

        if (callExpr.isEmpty() || isInvalidReturnType(context, callExpr.get())) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.message();
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        String uri = context.fileUri();
        CommandArgument posArg = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE, range);

        Matcher matcher = CommandConstants.UNDEFINED_FUNCTION_PATTERN.matcher(diagnosticMessage);
        String functionName = (matcher.find() && matcher.groupCount() > 0) ? matcher.group(1) + "(...)" : "";

        boolean isWithinFile = callExpr.get().functionName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE;
        if (isWithinFile) {
            String commandTitle = String.format(CommandConstants.CREATE_FUNCTION_TITLE, functionName);
            CodeActionData codeActionData = new CodeActionData(getName(), uri, range, posArg);
            ResolvableCodeAction action = CodeActionUtil.createResolvableCodeAction(
                    commandTitle, CodeActionKind.QuickFix, codeActionData);
            action.setDiagnostics(CodeActionUtil.toDiagnostics(Collections.singletonList((diagnostic))));
            return Collections.singletonList(action);
        }

        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean validate(Diagnostic diagnostic,
                            DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        if (!diagnostic.message().startsWith(UNDEFINED_FUNCTION) || positionDetails.matchedNode() == null) {
            return false;
        }
        return CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * Get the function call expression node if the provided node is a function call.
     *
     * @param node Node to be checked if it's a function call
     * @return Optional function call expression node
     */
    public static Optional<FunctionCallExpressionNode> checkAndGetFunctionCallExpressionNode(NonTerminalNode node) {
        FunctionCallExpressionNode functionCallExpressionNode = null;
        if (node.kind() == SyntaxKind.FUNCTION_CALL) {
            functionCallExpressionNode = (FunctionCallExpressionNode) node;
        }

        if (functionCallExpressionNode != null) {
            return Optional.of(functionCallExpressionNode);
        }

        // Else, a function call can be within a start action
        if (node.kind() == SyntaxKind.START_ACTION) {
            StartActionNode startActionNode = (StartActionNode) node;
            if (startActionNode.expression().kind() == SyntaxKind.FUNCTION_CALL) {
                functionCallExpressionNode = (FunctionCallExpressionNode) startActionNode.expression();
            }
        }

        return Optional.ofNullable(functionCallExpressionNode);
    }

    private boolean isInvalidReturnType(CodeActionContext context, FunctionCallExpressionNode callExpr) {
        SemanticModel semanticModel = context.currentSemanticModel().get();
        FunctionCallExpressionTypeFinder typeFinder = new FunctionCallExpressionTypeFinder(semanticModel, callExpr);
        callExpr.accept(typeFinder);
        Optional<TypeSymbol> returnTypeSymbol = typeFinder.getReturnTypeSymbol();
        
        /*
        Check for the parent being `CALL_STATEMENT` to suggest the code action for the following
        eg: 
            function testF() {
                addTwoIntegers(a, b);
            }
         */
        return callExpr.parent().kind() != SyntaxKind.CALL_STATEMENT
                && (returnTypeSymbol.isPresent()
                && (returnTypeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR
                || returnTypeSymbol.get().typeKind() == TypeDescKind.NONE));
    }

    @Override
    public CodeAction resolve(ResolvableCodeAction codeAction, CodeActionResolveContext resolveContext) {
        String uri = codeAction.getData().getFileUri();
        Optional<Path> filePath = PathUtil.getPathFromURI(uri);
        if (filePath.isEmpty()) {
            throw new UserErrorException("Invalid file URI provided for the create function code action!");
        }

        SyntaxTree syntaxTree = resolveContext.workspace().syntaxTree(filePath.get()).orElseThrow();
        NonTerminalNode cursorNode = CommonUtil.findNode(codeAction.getData().getRange(), syntaxTree);
        if (cursorNode == null) {
            throw new UserErrorException("Failed to resolve code action");
        }

        Optional<FunctionCallExpressionNode> fnCallExprNode =
                CreateFunctionCodeAction.checkAndGetFunctionCallExpressionNode(cursorNode);
        if (fnCallExprNode.isEmpty()) {
            throw new UserErrorException("Couldn't find a matching function call expression");
        }

        SemanticModel semanticModel = resolveContext.workspace().semanticModel(filePath.get()).orElseThrow();

        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        Set<String> visibleSymbolNames = resolveContext.visibleSymbols(new Position(endLine, endCol))
                .stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        DocumentServiceContext docServiceContext = ContextBuilder.buildDocumentServiceContext(
                uri,
                resolveContext.workspace(),
                LSContextOperation.WS_EXEC_CMD,
                resolveContext.languageServercontext());

        List<String> args = new ArrayList<>();
        int argIndex = 1;
        List<io.ballerina.compiler.api.symbols.SymbolKind> argumentKindList = Arrays.
                asList(io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE, SymbolKind.CONSTANT);

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

            // If arg is a named arg, type and varName should be derived differently
            if (fnArgNode.kind() == SyntaxKind.NAMED_ARG) {
                NamedArgumentNode namedArgumentNode = (NamedArgumentNode) fnArgNode;
                type = semanticModel.typeOf(namedArgumentNode.expression());
                varName = namedArgumentNode.argumentName().name().text();
            }

            if (type.isPresent() && type.get().typeKind() != TypeDescKind.COMPILATION_ERROR) {
                varName = NameUtil.generateParameterName(varName, argIndex,
                        CommonUtil.getRawType(type.get()), visibleSymbolNames);
                args.add(FunctionGenerator.getParameterTypeAsString(docServiceContext, type.get()) + " " + varName);
            } else {
                varName = NameUtil.generateParameterName(varName, argIndex, null, visibleSymbolNames);
                args.add(FunctionGenerator.getParameterTypeAsString(docServiceContext, null) + " " + varName);
            }
            visibleSymbolNames.add(varName);
            argIndex++;
        }

        if (fnCallExprNode.get().functionName().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            throw new UserErrorException("Failed to resolve code action");
        }

        String functionName = ((SimpleNameReferenceNode) fnCallExprNode.get().functionName()).name().text();
        if (functionName.isEmpty()) {
            throw new UserErrorException("Failed to resolve code action");
        }

        List<TextEdit> edits = new ArrayList<>();
        Optional<NonTerminalNode> enclosingNode = findEnclosingModulePartNode(fnCallExprNode.get());
        Range insertRange;
        if (enclosingNode.isPresent()) {
            newLineAtEnd = CodeActionUtil.addNewLineAtEnd(enclosingNode.get());
            insertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());
        } else {
            insertRange = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        }

        FunctionCallExpressionTypeFinder typeFinder =
                new FunctionCallExpressionTypeFinder(semanticModel, fnCallExprNode.get());
        fnCallExprNode.get().accept(typeFinder);
        Optional<TypeSymbol> returnTypeSymbol = typeFinder.getReturnTypeSymbol();

        //Check if the function call is invoked from an isolated context.
        IsolatedBlockResolver isolatedBlockResolver = new IsolatedBlockResolver();
        Boolean isIsolated = isolatedBlockResolver.findIsolatedBlock(fnCallExprNode.get());

        // Generate function. We have to check if we have a return type symbol or a return type desc kind. Depending
        // on that, we need to use separate APIs.
        String function;
        if (returnTypeSymbol.isPresent()) {
            function = FunctionGenerator.generateFunction(docServiceContext, newLineAtEnd, functionName,
                    args, returnTypeSymbol.get(), isIsolated);
        } else {
            throw new UserErrorException("Failed to resolve code action");
        }

        edits.add(new TextEdit(insertRange, function));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(new VersionedTextDocumentIdentifier(
                uri, null), edits);
        codeAction.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit))));

        return codeAction;
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
