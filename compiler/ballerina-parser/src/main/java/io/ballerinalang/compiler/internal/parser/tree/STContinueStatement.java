package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

public class STContinueStatement extends STStatement {

    public final STNode continueKeyword;
    public final STNode semicolonToken;


    STContinueStatement(STNode continueKeyword,
                     STNode semicolonToken) {
        super(SyntaxKind.CONTINUE_STATEMENT);
        this.continueKeyword = continueKeyword;
        this.semicolonToken = semicolonToken;


        this.bucketCount = 2;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(continueKeyword, 0);
        this.addChildNode(semicolonToken, 1);

    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return null;
    }

}
