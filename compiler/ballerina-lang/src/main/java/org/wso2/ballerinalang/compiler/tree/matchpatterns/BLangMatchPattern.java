package org.wso2.ballerinalang.compiler.tree.matchpatterns;

import org.ballerinalang.model.tree.matchpatterns.MatchPattern;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

public abstract class BLangMatchPattern extends BLangNode implements MatchPattern {
    public BLangExpression matchExpr; // TODO : should changed as action or expr

    public boolean matchGuardIsAvailable;
}
