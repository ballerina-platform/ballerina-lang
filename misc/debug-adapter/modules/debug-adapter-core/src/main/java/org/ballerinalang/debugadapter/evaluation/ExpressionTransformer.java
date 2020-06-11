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

import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeTransformer;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A {@code NodeTransformer} implementation used to traverse + transform ballerina expressions into corresponding java
 * expressions.
 * <br><br>
 * Supported expression types. (Language specification v2020R1)
 * <ul>
 * <li>x + y
 * <li>x - y
 * <li>x * y
 * <li>x / y
 * </ul><ul>
 * <li>x < y
 * <li> x > y
 * <li>x <= y
 * <li>x >= y
 * </ul>
 * <br>
 * To be Implemented.
 * <ul>
 * <li>x % y
 * </ul><ul>
 * <li>x.k
 * <li>x.@a
 * <li>f(x)
 * <li>x.f(y)
 * <li>x[y]
 * <li>new T(x)
 * </ul><ul>
 * <li>+x
 * <li>-x
 * <li>~x
 * <li>!x
 * <li>typeof x
 * </ul><ul>
 * <li>x << y
 * <li>x >> y
 * <li>x >>> y
 * </ul><ul>
 * <li>x ... y
 * <li>x ..< y
 * </ul><ul>
 * <li>x is y
 * </ul><ul>
 * <li>x == y
 * <li>x != y
 * <li>x === y
 * <li>x !== y
 * </ul><ul>
 * <li>x & y
 * <li>x ^ y
 * <li>x | y
 * <li>x && y
 * <li>x || y
 * <li>x ?: y
 * <li>x ? y : z
 * <li>(x) => y
 * <li>let x = y in z
 * </ul>
 *
 * @since 2.0.0
 */
public class ExpressionTransformer extends NodeTransformer<Node> {

    private final Set<SyntaxKind> supportedSyntax = new HashSet<>();
    private final Set<SyntaxKind> capturedSyntax = new HashSet<>();
    private final List<Node> unsupportedNodes = new ArrayList<>();

    public ExpressionTransformer() {
        // expressions
        supportedSyntax.add(SyntaxKind.BINARY_EXPRESSION);
        supportedSyntax.add(SyntaxKind.BRACED_EXPRESSION);
        // arithmetic operators
        supportedSyntax.add(SyntaxKind.PLUS_TOKEN);
        supportedSyntax.add(SyntaxKind.MINUS_TOKEN);
        supportedSyntax.add(SyntaxKind.ASTERISK_TOKEN);
        supportedSyntax.add(SyntaxKind.SLASH_TOKEN);
        // relational operators
        supportedSyntax.add(SyntaxKind.LT_TOKEN);
        supportedSyntax.add(SyntaxKind.LT_EQUAL_TOKEN);
        supportedSyntax.add(SyntaxKind.GT_TOKEN);
        supportedSyntax.add(SyntaxKind.GT_EQUAL_TOKEN);
        // variable identifiers
        supportedSyntax.add(SyntaxKind.BASIC_LITERAL);
        supportedSyntax.add(SyntaxKind.SIMPLE_NAME_REFERENCE);
        supportedSyntax.add(SyntaxKind.IDENTIFIER_TOKEN);
        // numeric literals
        supportedSyntax.add(SyntaxKind.DECIMAL_INTEGER_LITERAL);
        supportedSyntax.add(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL);
        // misc
        supportedSyntax.add(SyntaxKind.OPEN_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.CLOSE_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.NONE);
        supportedSyntax.add(SyntaxKind.EOF_TOKEN);
    }

    /**
     * Parses a given ballerina expression and transforms into its corresponding java expression.
     *
     * @param expression Ballerina expression(user input).
     * @throws EvaluationException If validation/parsing is failed.
     */
    public String transform(String expression) throws EvaluationException {
        // Validates and converts the expression into a parsed syntax-tree node.
        ExpressionNode parsedExpr = DebugExpressionParser.validateAndParse(expression);
        // transforms the parsed ballerina expression into a java expression using a node transformer implementation.
        Node jExpression = parsedExpr.apply(this);
        if (unsupportedSyntaxDetected()) {
            final StringJoiner errors = new StringJoiner(System.lineSeparator());
            unsupportedNodes.forEach(node -> errors.add(String.format("%s - %s", node.toString(), node.kind())));
            throw new EvaluationException(String.format(EvaluationExceptionKind.UNSUPPORTED.getString(), errors));
        }
        return jExpression.toString();
    }

    private boolean unsupportedSyntaxDetected() {
        return !unsupportedNodes.isEmpty();
    }

    @Override
    public Node transform(Token token) {
        return transformSyntaxNode(token);
    }

    @Override
    protected Node transformSyntaxNode(Node node) {
        capturedSyntax.add(node.kind());
        if (!supportedSyntax.contains(node.kind())) {
            unsupportedNodes.add(node);
        }
        if (node instanceof NonTerminalNode) {
            NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
            nonTerminalNode.children().forEach(child -> child.apply(this));
        }
        return node;
    }
}
