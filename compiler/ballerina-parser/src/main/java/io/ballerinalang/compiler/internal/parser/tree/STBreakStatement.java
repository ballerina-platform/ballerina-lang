package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.BreakStatement;

public class STBreakStatement extends STStatement{
    public final STNode breakKeyword;
    public final STNode semicolonToken;


    STBreakStatement(STNode breakKeyword,
                        STNode semicolonToken) {
        super(SyntaxKind.BREAK_STATEMENT);
        this.breakKeyword = breakKeyword;
        this.semicolonToken = semicolonToken;


        this.bucketCount = 2;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(breakKeyword, 0);
        this.addChildNode(semicolonToken, 1);

    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {

        return new BreakStatement(this,position,parent);
    }
}
