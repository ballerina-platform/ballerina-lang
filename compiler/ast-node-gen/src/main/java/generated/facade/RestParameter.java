package generated.facade;
import generated.internal.STNode;

public  class RestParameter extends NonTerminalNode{
private Token leadingComma;
private Node type;
private Node ellipsis;
private Node paramName;

public RestParameter(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Token leadingComma() {
if (leadingComma != null) {
return leadingComma;
}
leadingComma = createToken(0);
return leadingComma;
}
public Node type() {
if (type != null) {
return type;
}
type = node.childInBucket(1).createFacade(getChildPosition(1), this);
return type;
}
public Node ellipsis() {
if (ellipsis != null) {
return ellipsis;
}
ellipsis = node.childInBucket(2).createFacade(getChildPosition(2), this);
return ellipsis;
}
public Node paramName() {
if (paramName != null) {
return paramName;
}
paramName = node.childInBucket(3).createFacade(getChildPosition(3), this);
return paramName;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return leadingComma();
case 1:
return type();
case 2:
return ellipsis();
case 3:
return paramName();
}
return null;
}
}
