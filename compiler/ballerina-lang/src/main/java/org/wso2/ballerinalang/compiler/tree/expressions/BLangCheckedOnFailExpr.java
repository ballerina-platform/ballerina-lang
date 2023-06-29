package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.CheckedExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 2.0.0
 */
public class BLangCheckedOnFailExpr extends BLangExpression implements CheckedExpressionNode {

    public BLangCheckedExpr bLangCheckedExpr;
    public BLangIdentifier bLangIdentifier;
    public BLangExpression bLangErrorConstructorExpr;

    @Override
    public NodeKind getKind() {
        return null;
    }

    public BLangCheckedExpr getbLangCheckedExpr() {
        return bLangCheckedExpr;
    }

    public BLangIdentifier getbLangIdentifier() {
        return bLangIdentifier;
    }

    public BLangExpression bLangErrorConstructorExpr() {
        return bLangErrorConstructorExpr;
    }


    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public ExpressionNode getExpression() {
        return null;
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.CHECK;
    }
}
