package org.ballerinalang.langserver.command.visitors;

import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;

import java.util.Optional;

/**
 * Finds if a provided {@link FunctionCallExpressionNode} node is contained within an isolated block.
 *
 * @since 2.0.0
 */
public class IsolatedBlockFinder extends NodeVisitor {

    private boolean resultFound = false;
    private Node nodeWithQualifier;

    public void findIsolatedBlock(FunctionCallExpressionNode functionCallExpressionNode) {
        functionCallExpressionNode.accept(this);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        checkForIsolatedQualifier(functionDefinitionNode.qualifierList(), functionDefinitionNode);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (node.parent() != null) {
            node.parent().accept(this);
        }
    }

    private void checkForIsolatedQualifier(NodeList<Token> tokens, Node node) {
        Optional<Token> isolatedQualifier = tokens.stream().filter(qualifier ->
                qualifier.text().equals(SyntaxKind.ISOLATED_KEYWORD.stringValue())).findAny();
        if (isolatedQualifier.isEmpty()) {
            return;
        }
        resultFound = true;
        nodeWithQualifier = node;
    }

    public boolean isResultFound() {
        return resultFound;
    }

    public Node getNodeWithQualifier() {
        return nodeWithQualifier;
    }
}
