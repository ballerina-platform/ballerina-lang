package org.wso2.ballerinalang.compiler.tree.matchpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.matchpatterns.ConstPattern;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

public class BLangConstPattern extends BLangMatchPattern implements ConstPattern {

    public BLangExpression expr;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CONST_MATCH_PATTERN;
    }

    @Override
    public ExpressionNode getExpresion() {
        return expr;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expr = (BLangExpression) expression;
    }
}
