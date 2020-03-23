package generated.facade;
import generated.internal.STNode;

public  class BlockStatement extends NonTerminalNode{
private Token openBraceToken;
private NodeList<Node> statementList;
private Token closeBraceToken;

public BlockStatement(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Token openBraceToken() {
if (openBraceToken != null) {
return openBraceToken;
}
openBraceToken = createToken(0);
return openBraceToken;
}
public NodeList<Node> statementList() {
if (statementList != null) {
return statementList;
}
statementList = createListNode(1);
return statementList;
}
public Token closeBraceToken() {
if (closeBraceToken != null) {
return closeBraceToken;
}
closeBraceToken = createToken(2);
return closeBraceToken;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return openBraceToken();
case 1:
return statementList();
case 2:
return closeBraceToken();
}
return null;
}
}
