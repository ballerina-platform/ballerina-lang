package org.ballerinalang.model.clauses;

import org.ballerinalang.model.tree.expressions.ExpressionNode;

public interface WhereClauseNode {

    ExpressionNode getExpression();

    void setExpression(ExpressionNode expression);
}
