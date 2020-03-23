package generated.facade;
import generated.internal.STNode;

public  class BinaryExpression extends NonTerminalNode{
private Node lhsExpr;
private Node operator;
private Node rhsExpr;

public BinaryExpression(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Node lhsExpr() {
if (lhsExpr != null) {
return lhsExpr;
}
lhsExpr = node.childInBucket(0).createFacade(getChildPosition(0), this);
return lhsExpr;
}
public Node operator() {
if (operator != null) {
return operator;
}
operator = node.childInBucket(1).createFacade(getChildPosition(1), this);
return operator;
}
public Node rhsExpr() {
if (rhsExpr != null) {
return rhsExpr;
}
rhsExpr = node.childInBucket(2).createFacade(getChildPosition(2), this);
return rhsExpr;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return lhsExpr();
case 1:
return operator();
case 2:
return rhsExpr();
}
return null;
}
}
