package org.ballerinalang.model.tree.matchpatterns;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.expressions.ExpressionNode;

public interface ConstPattern extends Node {

    ExpressionNode getExpresion();

    void setExpression(ExpressionNode expression);
}
