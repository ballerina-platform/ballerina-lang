package org.wso2.ballerinalang.compiler.tree.bindingpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.bindingpattern.BindingPattern;
import org.ballerinalang.model.tree.bindingpattern.ListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

public class BLangListBindingPattern extends BLangBindingPattern implements ListBindingPattern {
    public List<BLangBindingPattern> bindingPatterns = new ArrayList<>();

    @Override
    public List<? extends BindingPattern> getBindingPatterns() {
        return bindingPatterns;
    }

    @Override
    public void addBindingPattern(BindingPattern bindingPattern) {
        bindingPatterns.add((BLangBindingPattern) bindingPattern);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {

    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LIST_BINDING_PATTERN;
    }
}
