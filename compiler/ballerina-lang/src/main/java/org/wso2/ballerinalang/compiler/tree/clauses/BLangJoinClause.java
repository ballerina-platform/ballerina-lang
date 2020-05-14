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

    public BType varType;
    public BType resultType;
    public BType nillableResultType;
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
    public NodeKind getKind() {
        return NodeKind.JOIN;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "join " + variableDefinitionNode + " in " + collection;
    }
}
