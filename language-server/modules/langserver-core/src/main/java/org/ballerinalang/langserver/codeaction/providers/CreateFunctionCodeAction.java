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

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.command.executors.CreateFunctionExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;

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
public class CreateFunctionCodeAction extends AbstractCodeActionProvider {

    private static final String UNDEFINED_FUNCTION = "undefined function";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(diagnostic.message().startsWith(UNDEFINED_FUNCTION))) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.message();
        Position position = CommonUtil.toRange(diagnostic.location().lineRange()).getStart();
        String uri = context.fileUri();
        CommandArgument posArg = CommandArgument.from(CommandConstants.ARG_KEY_NODE_POS, position);
        CommandArgument uriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, uri);

        List<Object> args = Arrays.asList(posArg, uriArg);
        Matcher matcher = CommandConstants.UNDEFINED_FUNCTION_PATTERN.matcher(diagnosticMessage);
        String functionName = (matcher.find() && matcher.groupCount() > 0) ? matcher.group(1) + "(...)" : "";
        Node cursorNode = positionDetails.matchedNode();

        if (cursorNode == null) {
            return Collections.emptyList();
        }

        Optional<FunctionCallExpressionNode> callExpr = getFunctionCallExpressionNodeAtCursor(cursorNode);

        if (callExpr.isEmpty()) {
            return Collections.emptyList();
        }

        boolean isWithinFile = callExpr.get().functionName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE;
        if (isWithinFile) {
            String commandTitle = String.format(CommandConstants.CREATE_FUNCTION_TITLE, functionName);
            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setCommand(new Command(commandTitle, CreateFunctionExecutor.COMMAND, args));
            action.setDiagnostics(CodeActionUtil.toDiagnostics(Collections.singletonList((diagnostic))));
            return Collections.singletonList(action);
        }

        return Collections.emptyList();
    }

    /**
     * Tries to get the function call expression at the cursor.
     *
     * @param cursorNode Node at the cursor
     * @return Optional function call expression at the cursor
     */
    public static Optional<FunctionCallExpressionNode> getFunctionCallExpressionNodeAtCursor(Node cursorNode) {
        Optional<FunctionCallExpressionNode> fnCallExprNode = checkAndGetFunctionCallExpressionNode(cursorNode);
        if (fnCallExprNode.isEmpty()) {
            if (cursorNode.kind() == SyntaxKind.LOCAL_VAR_DECL) {
                VariableDeclarationNode varNode = (VariableDeclarationNode) cursorNode;
                Optional<ExpressionNode> initializer = varNode.initializer();
                if (initializer.isPresent()) {
                    fnCallExprNode = checkAndGetFunctionCallExpressionNode(initializer.get());
                }
            } else if (cursorNode.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
                AssignmentStatementNode assignmentNode = (AssignmentStatementNode) cursorNode;
                fnCallExprNode = checkAndGetFunctionCallExpressionNode(assignmentNode.expression());
            } else if (cursorNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                fnCallExprNode = checkAndGetFunctionCallExpressionNode(cursorNode.parent());
            }
        }

        return fnCallExprNode;
    }

    /**
     * Get the function call expression node if the provided node is a function call.
     *
     * @param node Node to be checked if it's a function call
     * @return Optional function call expression node
     */
    public static Optional<FunctionCallExpressionNode> checkAndGetFunctionCallExpressionNode(Node node) {
        FunctionCallExpressionNode functionCallExpressionNode = null;
        if (node.kind() == SyntaxKind.FUNCTION_CALL) {
            functionCallExpressionNode = (FunctionCallExpressionNode) node;
        }

        return Optional.ofNullable(functionCallExpressionNode);
    }
}
