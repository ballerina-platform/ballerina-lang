package org.ballerinalang.model.tree.bindingpattern;

import org.ballerinalang.model.tree.Node;

import java.util.List;

public interface ListBindingPattern extends Node {
    List<? extends BindingPattern> getBindingPatterns();

    void addBindingPattern(BindingPattern bindingPattern);
}
