/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.Optional;
import java.util.StringJoiner;

import static org.ballerinalang.debugadapter.utils.PackageUtils.isBlank;

/**
 * A simple ballerina expression-specific parsing implementation, used to validate, parse and transform ballerina
 * expressions into corresponding java expressions.
 *
 * @since 2.0.0
 */
class DebugExpressionParser {

    private static final String BAL_FUNCTION_DEF_TEMPLATE = "function evaluate() returns (any|error) {return %s}";

    /**
     * Validate, parse a given ballerina expression and returns the parsed syntax node for to the expression.
     *
     * @param expression Ballerina expression.
     * @return A {@code ExpressionStatementNode} instance for the given expression.
     * @throws EvaluationException If cannot be evaluated further.
     */
    static ExpressionNode validateAndParse(String expression) throws EvaluationException {
        // Validates for empty inputs.
        if (isBlank(expression)) {
            throw new EvaluationException(EvaluationExceptionKind.EMPTY.getString());
        }
        // As single expressions cannot be parsed standalone, coverts into a parsable unit.
        String parsableExpr = getParsableExpression(expression);
        TextDocument sourceText = TextDocuments.from(parsableExpr);
        SyntaxTree syntaxTree = SyntaxTree.from(sourceText);
        // Validates for syntactical correctness.
        if (syntaxTree.hasDiagnostics()) {
            final StringJoiner errors = new StringJoiner(System.lineSeparator());
            syntaxTree.diagnostics().forEach(diagnostic -> {
                if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                    errors.add(diagnostic.message());
                }
            });
            throw new EvaluationException(String.format(EvaluationExceptionKind.SYNTAX_ERROR.getString(),
                    errors.toString()));
        }
        // Extracts expression node from the parsed syntax-tree.
        try {
            Optional<ExpressionNode> expressionNode = ((ReturnStatementNode) (((FunctionBodyBlockNode)
                    ((FunctionDefinitionNode) ((ModulePartNode) syntaxTree.rootNode()).members().get(0)).functionBody())
                    .statements().get(0))).expression();
            if (!expressionNode.isPresent()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID.getString(), expression));
            }
            return expressionNode.get();
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID.getString(), expression));
        }
    }

    /**
     * Returns expression input in a parsable format.
     *
     * @param expression user expression input.
     * @return parsable unit which wraps the expression.
     */
    private static String getParsableExpression(String expression) {
        // Adds missing semicolon, if required.
        if (!expression.trim().endsWith(SyntaxKind.SEMICOLON_TOKEN.stringValue())) {
            expression += SyntaxKind.SEMICOLON_TOKEN.stringValue();
        }
        // As expressions cannot be parsed standalone, wraps it inside a function template, as the return value;
        return String.format(BAL_FUNCTION_DEF_TEMPLATE, expression);
    }
}
