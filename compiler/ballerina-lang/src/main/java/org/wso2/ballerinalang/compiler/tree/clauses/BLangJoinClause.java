package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.JoinClauseNode;
import org.ballerinalang.model.clauses.OnClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

public class BLangJoinClause extends BLangNode implements JoinClauseNode {

    public BLangExpression collection;
    public BLangOnClause onClause;
    public BLangExpression expression;

    public VariableDefinitionNode variableDefinitionNode;
    public boolean isDeclaredWithVar;

    public BLangJoinClause() {

    }

    @Override
    public ExpressionNode getCollection() {
        return collection;
    }

    @Override
    public void setCollection(ExpressionNode collection) {
        this.collection = (BLangExpression) collection;
    }

    @Override
    public boolean setDeclaredWithVar() {
        return false;
    }

    @Override
    public boolean isDeclaredWithVar() {
        return isDeclaredWithVar;
    }

    @Override
    public VariableDefinitionNode getVariableDefinitionNode() {
        return variableDefinitionNode;
    }

    @Override
    public void setVariableDefinitionNode(VariableDefinitionNode variableDefinitionNode) {
        this.variableDefinitionNode = variableDefinitionNode;
    }

    @Override
    public OnClauseNode getOnClauseNode() {
        return onClause;
    }

    @Override
    public void setOnClauseNode(OnClauseNode onClauseNode) {
        this.onClause = (BLangOnClause) onClauseNode;
    }

    @Override
    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expression = (BLangExpression) expression;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.JOIN;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "join " + variableDefinitionNode + " in " + collection +
                " " + onClause.toString() + " equals " + expression;
    }
}
