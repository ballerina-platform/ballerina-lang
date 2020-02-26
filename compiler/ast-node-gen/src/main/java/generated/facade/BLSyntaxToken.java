package generated.facade;
import generated.internal.SyntaxNode;
public class BLSyntaxToken extends BLNonTerminalNode{
private  BLNode leadingTrivia;
private  BLNode trailingTrivia;

public BLSyntaxToken(SyntaxNode node, int position, BLNonTerminalNode parent) {
super(node, position, parent);
}
public BLNode leadingTrivia() {
if (leadingTrivia != null) {
return leadingTrivia;
}
leadingTrivia = node.childInBucket(0).createFacade(getChildPosition(0), this);
return leadingTrivia;
}
public BLNode trailingTrivia() {
if (trailingTrivia != null) {
return trailingTrivia;
}
trailingTrivia = node.childInBucket(1).createFacade(getChildPosition(1), this);
return trailingTrivia;
}

public BLNode childInBucket(int bucket) {
switch (bucket) {
case 0:
return leadingTrivia();
case 1:
return trailingTrivia();

}
return null;
}
}
