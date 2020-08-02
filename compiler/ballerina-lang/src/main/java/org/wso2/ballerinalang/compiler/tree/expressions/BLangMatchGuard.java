package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.MatchGuard;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

public class BLangMatchGuard extends BLangExpression implements MatchGuard {

    public BLangExpression expr;

    @Override
    public ExpressionNode getExpression() {
        return expr;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expr = (BLangExpression) expression;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return null;
    }
}
