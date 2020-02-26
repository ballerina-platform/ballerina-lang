package generated.facade;
import generated.internal.SyntaxNode;
public class BLBlockStatement extends BLNonTerminalNode{
private  BLSyntaxToken openBraceToken;
private  BLNodeList<BLNode> statementList;
private  BLSyntaxToken closeBraceToken;

public BLBlockStatement(SyntaxNode node, int position, BLNonTerminalNode parent) {
super(node, position, parent);
}
public BLSyntaxToken openBraceToken() {
if (openBraceToken != null) {
return openBraceToken;
}
openBraceToken = createToken(0);
return openBraceToken;
}
public BLNodeList<BLNode> statementList() {
if (statementList != null) {
return statementList;
}
statementList = createListNode(1);
return statementList;
}
public BLSyntaxToken closeBraceToken() {
if (closeBraceToken != null) {
return closeBraceToken;
}
closeBraceToken = createToken(2);
return closeBraceToken;
}

public BLNode childInBucket(int bucket) {
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
