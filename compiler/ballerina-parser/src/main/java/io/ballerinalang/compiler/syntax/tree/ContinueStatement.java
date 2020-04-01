package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

public class ContinueStatement extends Statement{
    public ContinueStatement(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }
}
