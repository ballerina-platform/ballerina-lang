package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

public class ContinueStatement extends Statement{
    private Token continueToken;
    private Token semicolonToken;
    public ContinueStatement(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token continueToken() {
        if (continueToken != null) {
            return continueToken;
        }

        continueToken = createToken(0);
        return continueToken;
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
                return continueToken();
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
