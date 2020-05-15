package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

public class BLangJoinClause extends BLangInputClause {

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
