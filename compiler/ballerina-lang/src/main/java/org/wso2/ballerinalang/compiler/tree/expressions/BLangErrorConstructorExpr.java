package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ErrorConstructorExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.List;

public class BLangErrorConstructorExpr extends BLangExpression implements ErrorConstructorExpressionNode {
    public BLangUserDefinedType errorTypeRef;
    public List<BLangExpression> positionalArgs;
    public List<BLangNamedArgsExpression> namedArgs;
    // This is added to store namedArgs.
    public BLangExpression errorDetail;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ERROR_CONSTRUCTOR_EXPRESSION;
    }
}
