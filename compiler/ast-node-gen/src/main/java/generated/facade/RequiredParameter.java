package generated.facade;
import generated.internal.STNode;

public  class RequiredParameter extends NonTerminalNode{
private Token leadingComma;
private Node accessModifier;
private Node type;
private Node paramName;

public RequiredParameter(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Token leadingComma() {
if (leadingComma != null) {
return leadingComma;
}
leadingComma = createToken(0);
return leadingComma;
}
public Node accessModifier() {
if (accessModifier != null) {
return accessModifier;
}
accessModifier = node.childInBucket(1).createFacade(getChildPosition(1), this);
return accessModifier;
}
public Node type() {
if (type != null) {
return type;
}
type = node.childInBucket(2).createFacade(getChildPosition(2), this);
return type;
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
return accessModifier();
case 2:
return type();
case 3:
return paramName();
}
return null;
}
}
