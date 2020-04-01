package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

public class BreakStatement extends Statement{
    private Token breakToken;
    private Token semicolonToken;

    public BreakStatement(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token breakToken() {
        if (breakToken != null) {
            return breakToken;
        }

        breakToken = createToken(0);
        return breakToken;
    }

    public Token semicolonToken() {
        if (semicolonToken != null) {
            return semicolonToken;
        }

        semicolonToken = createToken(2);
        return semicolonToken;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return breakToken();
            case 1:
                return semicolonToken();
        }
        return null;
    }
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SyntaxNodeTransformer<T> visitor) {
        return visitor.transform(this);
    }
}
