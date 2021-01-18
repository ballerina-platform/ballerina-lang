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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleCompilation;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.util.Optional;
import java.util.StringJoiner;

/**
 * A ballerina expression-specific implementation for validating, parsing and compiling ballerina expressions.
 *
 * @since 2.0.0
 */
public class DebugExpressionCompiler {

    private final SuspendedContext context;
    private static final String BAL_FUNCTION_DEF_TEMPLATE = "function evaluate() returns (any|error) {return %s}";
    Document document;

    public DebugExpressionCompiler(SuspendedContext context) {
        this.context = context;
    }

    /**
     * Validate, parse a given ballerina expression and returns the parsed syntax node for to the expression.
     *
     * @param expression Ballerina expression.
     * @return A {@code ExpressionStatementNode} instance for the given expression.
     * @throws EvaluationException If cannot be evaluated further.
     */
    ExpressionNode validateAndCompile(String expression) throws EvaluationException {
        // validates for empty inputs.
        if (expression.isBlank()) {
            throw new EvaluationException(EvaluationExceptionKind.EMPTY.getString());
        }

        // Todo - enable
        // validates for syntax and semantic errors.
        // validateForCompilationErrors(expression);

        // returns parsed expression node.
        return getParsedExpressionNode(expression);
    }

    /**
     * Returns module compilation for a given expression by injecting it into the given debugger source.
     *
     * @param expr expression string
     * @return module compilation
     */
    public ModuleCompilation getModuleCompilation(String expr) {
        if (document == null) {
            document = context.getDocument();
        }

        if (expr == null || expr.isBlank()) {
            return document.module().getCompilation();
        }

        // As expressions cannot be compiled standalone, coverts into a compilable statement.
        String exprStatement = getExpressionStatement(expr);
        // Injects the expression into the source file content, at the end of the current debug point line.
        int startOffset = document.textDocument().line(context.getLineNumber() - 1).endOffset();
        TextEdit[] textEdit = {TextEdit.from(TextRange.from(startOffset, 0), exprStatement)};
        String newContent = new String(document.textDocument().apply(TextDocumentChange.from(textEdit)).toCharArray());
        Document newDocument = document.modify().withContent(newContent).apply();
        return newDocument.module().getCompilation();
    }

    public SemanticModel getSemanticInfo() {
        if (document == null) {
            document = context.getDocument();
        }
        return document.module().getCompilation().getSemanticModel();
    }

    /**
     * Validates for syntactic and semantic correctness.
     */
    private void validateForCompilationErrors(String expr) throws EvaluationException {
        try {
            ModuleCompilation compilation = getModuleCompilation(expr);
            DiagnosticResult diagnostics = compilation.diagnostics();
            if (diagnostics.hasErrors()) {
                final StringJoiner errors = new StringJoiner(System.lineSeparator());
                diagnostics.errors().forEach(diagnostic -> {
                    if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                        errors.add(diagnostic.message());
                    }
                });
                throw new EvaluationException(String.format(EvaluationExceptionKind.COMPILATION_ERRORS.getString(),
                        errors.toString()));
            }
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception ignored) {
            // Todo - should abort the evaluation?
        }
    }

    /**
     * Returns a parsed expression node instance for a given string expression.
     */
    private ExpressionNode getParsedExpressionNode(String expression) throws EvaluationException {
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
            if (expressionNode.isEmpty()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID.getString(), expression));
            }
            return expressionNode.get();
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID.getString(), expression));
        }
    }

    /**
     * Returns the expression input wrapped inside a statement.
     *
     * @param expression user expression input.
     * @return parsable unit which wraps the expression.
     */
    private static String getExpressionStatement(String expression) {
        // Adds missing semicolon, if required.
        if (!expression.trim().endsWith(SyntaxKind.SEMICOLON_TOKEN.stringValue())) {
            expression += SyntaxKind.SEMICOLON_TOKEN.stringValue();
        }
        // Creates an statement in the format of "_ = <EXPRESSION>" and injects into the existing source, in order to
        // be compilable.
        return "_ = " + expression;
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
