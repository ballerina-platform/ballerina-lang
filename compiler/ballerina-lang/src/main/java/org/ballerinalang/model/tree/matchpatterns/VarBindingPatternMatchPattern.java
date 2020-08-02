package org.ballerinalang.model.tree.matchpatterns;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.bindingpattern.BindingPattern;

public interface VarBindingPatternMatchPattern extends Node {

    BindingPattern getBindingPattern();

    void setBindingPattern(BindingPattern bindingPattern);
}
