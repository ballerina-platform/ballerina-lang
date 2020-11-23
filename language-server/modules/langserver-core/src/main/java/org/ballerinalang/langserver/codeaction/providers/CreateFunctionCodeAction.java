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

import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.executors.CreateFunctionExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
                                                    CodeActionContext context) {
        if (!(diagnostic.getMessage().startsWith(UNDEFINED_FUNCTION))) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.getMessage();
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = context.fileUri();
        CommandArgument lineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + line);
        CommandArgument colArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + column);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
        Matcher matcher = CommandConstants.UNDEFINED_FUNCTION_PATTERN.matcher(diagnosticMessage);
        String functionName = (matcher.find() && matcher.groupCount() > 0) ? matcher.group(1) + "(...)" : "";
        NonTerminalNode cursorNode = context.positionDetails().matchedNode();
        if (cursorNode != null && cursorNode.kind() == SyntaxKind.FUNCTION_CALL) {
            FunctionCallExpressionNode callExpr = (FunctionCallExpressionNode) cursorNode;
            boolean isWithinFile = callExpr.functionName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE;
            if (isWithinFile) {
                String commandTitle = CommandConstants.CREATE_FUNCTION_TITLE + functionName;
                CodeAction action = new CodeAction(commandTitle);
                action.setKind(CodeActionKind.QuickFix);
                action.setCommand(new Command(commandTitle, CreateFunctionExecutor.COMMAND, args));
                action.setDiagnostics(diagnostics);
                return Collections.singletonList(action);
            }
        }
        return new ArrayList<>();
    }
}
