/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * NodeRewriter to update syntax tree nodes.
 *
 * @since 2.0.0
 */
public class NodeRewriter extends NodeTransformer {

    private final HashMap<String, String> symbolTypes;

    public NodeRewriter(HashMap<String, String> symbolTypes) {
        this.symbolTypes = symbolTypes;
    }

    public List<Node> accept(Node node) {
        Node newNode = (Node) node.apply(this);
        List<Node> nodes = new ArrayList<>();
        nodes.add(newNode);
        return nodes;
    }

    @Override
    public Node transform(AssignmentStatementNode assignmentStatementNode) {
        String varRef = assignmentStatementNode.varRef().toString().trim();
        Node expression = (Node) assignmentStatementNode.expression().apply(this);
        StatementNode node = NodeParser.parseStatements(String.format(" _ = __memorize(\"%s\",%s);",
                varRef, expression)).get(0);
        return node;
    }

    @Override
    public Node transform(BracedExpressionNode bracedExpressionNode) {
        return bracedExpressionNode.modify(
                bracedExpressionNode.kind(),
                bracedExpressionNode.openParen(),
                (ExpressionNode) bracedExpressionNode.expression().apply(this),
                bracedExpressionNode.closeParen());
    }

    @Override
    public Node transform(BinaryExpressionNode binaryExpressionNode) {
        Node nodeLhs = (Node) binaryExpressionNode.lhsExpr().apply(this);
        Node nodeRhs = (Node) binaryExpressionNode.rhsExpr().apply(this);

        Node node = NodeParser.parseExpression(nodeLhs.toString() + binaryExpressionNode.operator().toString() +
                nodeRhs.toString());
        return node;
    }

    @Override
    public Node transform(BasicLiteralNode basicLiteralNode) {
        return basicLiteralNode;
    }

    @Override
    public Node transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        String type;
        String nameReference = simpleNameReferenceNode.name().toString().trim();
        if (symbolTypes.get(nameReference) != null) {
            type = "<" + symbolTypes.get(nameReference) + ">";
        } else {
            type = "<" + symbolTypes.get("\'" + nameReference) + ">";
        }
        ExpressionNode node = NodeParser.parseExpression(
                String.format(type + "__recall_any(\"%s\")", nameReference));
        return node;
    }

    @Override
    public Node transform(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        String lhs = compoundAssignmentStatementNode.lhsExpression().toString().trim();
        String rhs = compoundAssignmentStatementNode.rhsExpression().toString().trim();
        String binaryOperator = compoundAssignmentStatementNode.binaryOperator().toString();
        StatementNode node = NodeParser.parseStatements(
                String.format(" _ = __memorize(\"%s\", %s %s %s);", lhs, lhs, binaryOperator, rhs)).get(0);
        return node;
    }

    @Override
    protected Object transformSyntaxNode(Node node) {
        return null;
    }

    @Override
    public Node transform(FunctionBodyBlockNode functionBodyBlockNode) {
        NodeList<StatementNode> statementNodes = functionBodyBlockNode.statements();
        for (int i = 0; i < statementNodes.size(); i++) {
            statementNodes = statementNodes.set(i, (StatementNode) statementNodes.get(i).apply(this));
        }
        NamedWorkerDeclarator namedWorkerDeclarator =
                modifyNode(functionBodyBlockNode.namedWorkerDeclarator().orElse(null));
        return functionBodyBlockNode.modify(
                functionBodyBlockNode.openBraceToken(),
                namedWorkerDeclarator,
                statementNodes,
                functionBodyBlockNode.closeBraceToken());
    }

    @Override
    public Node transform(FunctionCallExpressionNode functionCallExpressionNode) {
        return functionCallExpressionNode;
    }

    @Override
    public Node transform(BlockStatementNode blockStatementNode) {
        NodeList<StatementNode> statementNodes = blockStatementNode.statements();
        for (int i = 0; i < statementNodes.size(); i++) {
            statementNodes = statementNodes.set(i, (StatementNode) statementNodes.get(i).apply(this));
        }
        return blockStatementNode.modify(
                blockStatementNode.openBraceToken(),
                statementNodes,
                blockStatementNode.closeBraceToken());
    }

    @Override
    public Node transform(ReturnStatementNode returnStatementNode) {
        String type;
        if (symbolTypes.get(returnStatementNode.expression().get().toString()) != null) {
             type = "<" + symbolTypes.get(returnStatementNode.expression().get().toString()) + ">";
        } else {
            type = "<" + symbolTypes.get("\'" + returnStatementNode.expression().get().toString()) + ">";
        }

        ExpressionNode expressionNode = NodeParser.parseExpression(type + " __recall_any(\"" +
                returnStatementNode.expression().get() + "\")");
        return returnStatementNode.modify(
                returnStatementNode.returnKeyword(),
                expressionNode,
                returnStatementNode.semicolonToken()
        );
    }

    protected <T extends Node> T modifyNode(T node) {
        if (node == null) {
            return null;
        }
        // TODO
        return (T) node.apply(this);
    }

}
