package generated.facade;
import generated.internal.STNode;

public  class Token extends NonTerminalNode{
private Node leadingTrivia;
private Node trailingTrivia;

public Token(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Node leadingTrivia() {
if (leadingTrivia != null) {
return leadingTrivia;
}
leadingTrivia = node.childInBucket(0).createFacade(getChildPosition(0), this);
return leadingTrivia;
}
public Node trailingTrivia() {
if (trailingTrivia != null) {
return trailingTrivia;
}
trailingTrivia = node.childInBucket(1).createFacade(getChildPosition(1), this);
return trailingTrivia;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return leadingTrivia();
case 1:
return trailingTrivia();
}
return null;
}
}
