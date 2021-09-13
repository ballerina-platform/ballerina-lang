/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation.validator.impl;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.parser.ExpressionParser;
import org.ballerinalang.debugadapter.evaluation.validator.ValidatorException;

import java.util.Optional;
import java.util.StringJoiner;

import static org.ballerinalang.debugadapter.evaluation.validator.ValidatorException.UNSUPPORTED_INPUT_STATEMENT;

/**
 * Validator implementation for ballerina expressions.
 *
 * @since 2.0.0
 */
public class ExpressionValidator extends StatementValidator {

    public ExpressionValidator() {
        this(new ExpressionParser());
    }

    public ExpressionValidator(ExpressionParser parser) {
        super(parser);
    }

    @Override
    public void validate(String source) throws Exception {
        SyntaxTree syntaxTree = debugParser.getSyntaxTreeFor(source);
        NodeList<StatementNode> statements = getStatementsFrom(syntaxTree);
        StatementNode exprStatement = statements.get(0);
        failIf(exprStatement.kind() != SyntaxKind.RETURN_STATEMENT, UNSUPPORTED_INPUT_STATEMENT);
        Optional<ExpressionNode> expression = ((ReturnStatementNode) exprStatement).expression();
        failIf(expression.isEmpty(), "Failed to derive the expression due to a parsing error.");

        // Validate for syntax errors.
        if (syntaxTree.hasDiagnostics()) {
            final StringJoiner errors = new StringJoiner(System.lineSeparator());
            syntaxTree.diagnostics().forEach(diagnostic -> {
                if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                    errors.add(diagnostic.message());
                }
            });
            failIf(errors.length() > 0, String.format(EvaluationExceptionKind.SYNTAX_ERROR.getReason(), errors));
        }
    }

    public ExpressionNode getExpressionNodeFrom(SyntaxTree syntaxTree) throws ValidatorException {
        NodeList<StatementNode> statements = getStatementsFrom(syntaxTree);
        StatementNode exprStatement = statements.get(0);
        return ((ReturnStatementNode) exprStatement).expression().get();
    }
}
