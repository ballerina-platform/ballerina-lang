package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.Optional;

/**
 * Node Transformer to find the container expression node for a given node. 
 * 
 * @since 2.0.0
 */
public class ExpressionNodeResolver extends NodeTransformer<Optional<ExpressionNode>> {

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
}
