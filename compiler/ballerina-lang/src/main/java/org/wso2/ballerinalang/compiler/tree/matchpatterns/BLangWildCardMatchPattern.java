package org.wso2.ballerinalang.compiler.tree.matchpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.matchpatterns.WildCardMatchPattern;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

public class BLangWildCardMatchPattern extends BLangMatchPattern implements WildCardMatchPattern {

    public boolean matchesAll;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.WILDCARD_MATCH_PATTERN;
    }
}
