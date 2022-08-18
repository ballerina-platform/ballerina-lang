package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ReCapturingGroupsNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;


public class BLangReCapturingGroups extends BLangReAtom implements ReCapturingGroupsNode {
    public BLangExpression openParen;
    public BLangExpression flagExpr;
    public BLangReDisjunction reDisjunction;
    public BLangExpression closeParen;

    @Override
    public NodeKind getKind() {
        return NodeKind.REG_EXP_CAPTURING_GROUP;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }
}
