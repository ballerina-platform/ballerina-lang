package org.wso2.ballerinalang.compiler.tree.matchpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.bindingpattern.BindingPattern;
import org.ballerinalang.model.tree.matchpatterns.VarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangBindingPattern;

public class BLangVarBindingPatternMatchPattern extends BLangMatchPattern implements VarBindingPatternMatchPattern {

    BLangBindingPattern bindingPattern;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {

        return NodeKind.VAR_BINDING_PATTERN_MATCH_PATTERN;
    }

    @Override
    public BLangBindingPattern getBindingPattern() {
        return bindingPattern;
    }

    @Override
    public void setBindingPattern(BindingPattern bindingPattern) {
        this.bindingPattern = (BLangBindingPattern) bindingPattern;
    }
}
