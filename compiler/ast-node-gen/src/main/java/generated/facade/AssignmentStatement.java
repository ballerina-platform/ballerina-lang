package generated.facade;
import generated.internal.STNode;

public  class AssignmentStatement extends NonTerminalNode{
private Node varRef;
private Node equalsToken;
private Node expr;
private Node semicolonToken;

public AssignmentStatement(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Node varRef() {
if (varRef != null) {
return varRef;
}
varRef = node.childInBucket(0).createFacade(getChildPosition(0), this);
return varRef;
}
public Node equalsToken() {
if (equalsToken != null) {
return equalsToken;
}
equalsToken = node.childInBucket(1).createFacade(getChildPosition(1), this);
return equalsToken;
}
public Node expr() {
if (expr != null) {
return expr;
}
expr = node.childInBucket(2).createFacade(getChildPosition(2), this);
return expr;
}
public Node semicolonToken() {
if (semicolonToken != null) {
return semicolonToken;
}
semicolonToken = node.childInBucket(3).createFacade(getChildPosition(3), this);
return semicolonToken;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return varRef();
case 1:
return equalsToken();
case 2:
return expr();
case 3:
return semicolonToken();
}
return null;
}
}
