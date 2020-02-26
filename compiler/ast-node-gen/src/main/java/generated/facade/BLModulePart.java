package generated.facade;

import generated.internal.SyntaxNode;

public class BLModulePart extends BLNonTerminalNode {
    private BLNodeList<BLNode> importList;
    private BLNodeList<BLNode> memberList;
    private BLSyntaxToken eofToken;

    public BLModulePart(SyntaxNode node, int position, BLNonTerminalNode parent) {
        super(node, position, parent);
    }

    public BLNodeList<BLNode> importList() {
        if (importList != null) {
            return importList;
        }
        importList = createListNode(0);
        return importList;
    }

    public BLNodeList<BLNode> memberList() {
        if (memberList != null) {
            return memberList;
        }
        memberList = createListNode(1);
        return memberList;
    }

    public BLSyntaxToken eofToken() {
        if (eofToken != null) {
            return eofToken;
        }
        eofToken = createToken(2);
        return eofToken;
    }

    public BLNode childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return importList();
            case 1:
                return memberList();
            case 2:
                return eofToken();

        }
        return null;
    }
}
