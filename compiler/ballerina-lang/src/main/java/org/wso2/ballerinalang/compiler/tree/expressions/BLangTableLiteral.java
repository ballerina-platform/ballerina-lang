package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.TableLiteralNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of {@link TableLiteralNode}.
 */
public class BLangTableLiteral extends BLangExpression implements TableLiteralNode {

    public BLangExpression configurationExpr;

    public BLangTableLiteral() {

    }

    @Override
    public ExpressionNode getConfigurationExpression() {
        return configurationExpr;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TABLE;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
}
