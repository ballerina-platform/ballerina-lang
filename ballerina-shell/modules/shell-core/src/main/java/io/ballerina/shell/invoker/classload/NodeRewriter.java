package io.ballerina.shell.invoker.classload;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.StatementNode;

import java.util.ArrayList;
import java.util.List;

/**
 * NodeRewriter to update syntax tree nodes.
 *
 * @since 2.0.0
 */
public class NodeRewriter extends NodeTransformer {

    public List<Node> accept(Node node) {
        Node newNode = (Node) node.apply(this);
        List<Node> nodes = new ArrayList<>();
        nodes.add(newNode);
        return nodes;
    }

    @Override
    public Node transform(AssignmentStatementNode assignmentStatementNode) {
        String varRef = assignmentStatementNode.varRef().toString().trim();
        String expression = assignmentStatementNode.expression().toString().trim();
        StatementNode node = NodeParser.parseStatements(
                String.format(" _ = __memorize(\"%s\",%s);", varRef, expression)).get(0);
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

    protected <T extends Node> T modifyNode(T node) {
        if (node == null) {
            return null;
        }
        // TODO
        return (T) node.apply(this);
    }
}
