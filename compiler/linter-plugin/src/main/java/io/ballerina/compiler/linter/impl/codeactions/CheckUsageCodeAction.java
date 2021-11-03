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
package io.ballerina.compiler.linter.impl.codeactions;

import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LineRange;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.linter.impl.codeactions.Constants.BCE_INVALID_CHECK;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.CA_CHECK_USAGE;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.LINE_RANGE;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.REMOVE_S_KEYWORD;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.VALUE;

/**
 * Code action to handle invalid usage of `check` expression.
 * <p>
 *
 * <code>
 * int i = 10;
 * int x = check i; // invalid code
 * </code>
 *
 * @since 2.0.0
 */
public class CheckUsageCodeAction extends LinterCodeAction {

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of(BCE_INVALID_CHECK);
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext context) {

        final NonTerminalNode expr = getNodeFromDiagnostics(context);
        final NonTerminalNode node = expr.parent();

        final SyntaxKind syntaxKind = node.kind();
        final String updatedText;
        final String checkingKeyword;
        if (syntaxKind == SyntaxKind.CHECK_ACTION || syntaxKind == SyntaxKind.CHECK_EXPRESSION) {
            CheckExpressionNode checkExpressionNode = (CheckExpressionNode) node;
            checkingKeyword = checkExpressionNode.checkKeyword().text();
            updatedText = expr.toSourceCode();
        } else {
            return Optional.empty();
        }

        List<CodeActionArgument> args = List.of(CodeActionArgument.from(LINE_RANGE, node.lineRange()),
                CodeActionArgument.from(VALUE, updatedText));
        return Optional.of(CodeActionInfo.from(String.format(REMOVE_S_KEYWORD, checkingKeyword), args));
    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext context) {
        LineRange lineRange = context.arguments().get(0).valueAs(LineRange.class);
        String updatedText = context.arguments().get(1).valueAs(String.class);

        DocumentEdit documentEdit = getDocumentEdit(context, lineRange, updatedText);
        return List.of(documentEdit);
    }

    @Override
    public String name() {
        return CA_CHECK_USAGE;
    }
}
