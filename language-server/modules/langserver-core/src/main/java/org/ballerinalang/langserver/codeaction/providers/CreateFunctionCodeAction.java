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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.command.executors.CreateFunctionExecutor;
import org.ballerinalang.langserver.command.visitors.FunctionCallExpressionTypeFinder;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Code Action for creating undefined function.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateFunctionCodeAction implements DiagnosticBasedCodeActionProvider {

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
        CommandArgument uriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, uri);

        List<Object> args = Arrays.asList(posArg, uriArg);
        Matcher matcher = CommandConstants.UNDEFINED_FUNCTION_PATTERN.matcher(diagnosticMessage);
        String functionName = (matcher.find() && matcher.groupCount() > 0) ? matcher.group(1) + "(...)" : "";

        boolean isWithinFile = callExpr.get().functionName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE;
        if (isWithinFile) {
            String commandTitle = String.format(CommandConstants.CREATE_FUNCTION_TITLE, functionName);
            Command command = new Command(commandTitle, CreateFunctionExecutor.COMMAND, args);
            CodeAction action = CodeActionUtil.createCodeAction(commandTitle, command, CodeActionKind.QuickFix);
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
        FunctionCallExpressionTypeFinder typeFinder = new FunctionCallExpressionTypeFinder(semanticModel);
        typeFinder.findTypeOf(callExpr);
        Optional<TypeSymbol> returnTypeSymbol = typeFinder.getReturnTypeSymbol();
        Optional<TypeDescKind> returnTypeDescKind = typeFinder.getReturnTypeDescKind();
        
        /*
        Check for the parent being `CALL_STATEMENT` to suggest the code action for the following
        eg: 
            function testF() {
                addTwoIntegers(a, b);
            }
         */
        return callExpr.parent().kind() != SyntaxKind.CALL_STATEMENT
                && ((returnTypeSymbol.isPresent()
                && returnTypeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR)
                || (returnTypeDescKind.isPresent() && (returnTypeDescKind.get() == TypeDescKind.COMPILATION_ERROR
                || returnTypeDescKind.get() == TypeDescKind.NONE)));
    }
}
