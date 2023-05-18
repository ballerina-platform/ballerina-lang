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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.common.utils.PositionUtil;

import java.util.Optional;

/**
 * Node Transformer to find the container expression node for a given node.
 *
 * <strong>Note</strong>: Rather than doing {@code node.apply(expressionResolver)},
 * please use {@link #findExpression(Node)} since the {@code node.apply()} method may return {@code null}.
 *
 * @since 2.0.0
 */
public class MatchedExpressionNodeResolver extends NodeTransformer<Optional<ExpressionNode>> {

    Node matchedNode;

    public MatchedExpressionNodeResolver(Node matchedNode) {
        this.matchedNode = matchedNode;
    }

    /**
     * Given the node, this method returns the optional expression in which the provided node is located.
     *
     * @param node Node
     * @return Optional enclosing expression node
     */
    public Optional<ExpressionNode> findExpression(Node node) {
        if (node == null) {
            return Optional.empty();
        }

        Optional<ExpressionNode> exprNode = node.apply(this);
        // Due to the way apply() method is implemented in some cases, this can return null
        return exprNode == null ? Optional.empty() : exprNode;
    }

    @Override
    protected Optional<ExpressionNode> transformSyntaxNode(Node node) {
        if (node.parent() == null) {
            return Optional.empty();
        }
        return node.parent().apply(this);
    }

    @Override
    public Optional<ExpressionNode> transform(AssignmentStatementNode assignmentStatementNode) {
        return Optional.of(assignmentStatementNode.expression());
    }

    @Override
    public Optional<ExpressionNode> transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return moduleVariableDeclarationNode.initializer();
    }

    @Override
    public Optional<ExpressionNode> transform(VariableDeclarationNode variableDeclarationNode) {
        return variableDeclarationNode.initializer();
    }

    @Override
    public Optional<ExpressionNode> transform(PositionalArgumentNode positionalArgumentNode) {
        return Optional.of(positionalArgumentNode.expression());
    }

    @Override
    public Optional<ExpressionNode> transform(NamedArgumentNode namedArgumentNode) {
        return Optional.of(namedArgumentNode.expression());
    }

    @Override
    public Optional<ExpressionNode> transform(SpecificFieldNode specificFieldNode) {
        return specificFieldNode.valueExpr();
    }

    @Override
    public Optional<ExpressionNode> transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        return Optional.of(letVariableDeclarationNode.expression());
    }

    @Override
    public Optional<ExpressionNode> transform(FromClauseNode fromClauseNode) {
        return Optional.of(fromClauseNode.expression());
    }

    public Optional<ExpressionNode> transform(BracedExpressionNode node) {
        return Optional.of(node);
    }

    public Optional<ExpressionNode> transform(ImplicitNewExpressionNode implicitNewExpressionNode) {
        return Optional.of(implicitNewExpressionNode);
    }

    @Override
    public Optional<ExpressionNode> transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        return Optional.of(explicitNewExpressionNode);
    }

    @Override
    public Optional<ExpressionNode> transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        Optional<Node> expressionNode = listConstructorExpressionNode.expressions().stream()
                .filter(expression -> this.matchedNode == expression)
                .findFirst();
        if (expressionNode.isPresent() && expressionNode.get() instanceof ExpressionNode) {
            return Optional.of((ExpressionNode) expressionNode.get());
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<ExpressionNode> transform(BasicLiteralNode basicLiteralNode) {
        return Optional.of(basicLiteralNode);
    }

    @Override
    public Optional<ExpressionNode> transform(ReturnStatementNode returnStatementNode) {
        return returnStatementNode.expression();
    }

    @Override
    public Optional<ExpressionNode> transform(IndexedExpressionNode node) {
        if (PositionUtil.isWithinLineRange(matchedNode.lineRange(), node.containerExpression().lineRange())) {
            return Optional.of(node.containerExpression());
        }

        if (!node.keyExpression().isEmpty()) {
            for (ExpressionNode expressionNode : node.keyExpression()) {
                if (PositionUtil.isWithinLineRange(matchedNode.lineRange(), expressionNode.lineRange())) {
                    return Optional.of(expressionNode);
                }
            }
        }

        return Optional.of(node);
    }

    @Override
    public Optional<ExpressionNode> transform(ConditionalExpressionNode conditionalExpressionNode) {
        ExpressionNode lhsExpr = conditionalExpressionNode.lhsExpression();
        ExpressionNode middleExpr = conditionalExpressionNode.middleExpression();
        ExpressionNode endExpr = conditionalExpressionNode.endExpression();
        LineRange matchedLineRange = matchedNode.lineRange();

        if (PositionUtil.isWithinLineRange(matchedLineRange, lhsExpr.lineRange())) {
            return Optional.of(lhsExpr);
        }
        if (PositionUtil.isWithinLineRange(matchedLineRange, middleExpr.lineRange())) {
            return Optional.of(middleExpr);
        }
        if (PositionUtil.isWithinLineRange(matchedLineRange, endExpr.lineRange())) {
            return Optional.of(endExpr);
        }
        return Optional.of(conditionalExpressionNode);
    }

    @Override
    public Optional<ExpressionNode> transform(BinaryExpressionNode binaryExpressionNode) {
        return Optional.of(binaryExpressionNode);
    }
}
