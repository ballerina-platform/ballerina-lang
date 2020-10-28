package org.wso2.ballerinalang.compiler.tree.bindingpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.bindingpattern.WildCardBindingPatternNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

public class BLangWildCardBindingPattern extends BLangBindingPattern implements WildCardBindingPatternNode {

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.WILDCARD_BINDING_PATTERN;
    }
}
