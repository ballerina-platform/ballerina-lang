package generated.facade;
import generated.internal.STNode;

public  class LocalVariableDeclaration extends NonTerminalNode{
private Token typeName;
private Token variableName;
private Token equalsToken;
private Node initializer;
private Token semicolonToken;

public LocalVariableDeclaration(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Token typeName() {
if (typeName != null) {
return typeName;
}
typeName = createToken(0);
return typeName;
}
public Token variableName() {
if (variableName != null) {
return variableName;
}
variableName = createToken(1);
return variableName;
}
public Token equalsToken() {
if (equalsToken != null) {
return equalsToken;
}
equalsToken = createToken(2);
return equalsToken;
}
public Node initializer() {
if (initializer != null) {
return initializer;
}
initializer = node.childInBucket(3).createFacade(getChildPosition(3), this);
return initializer;
}
public Token semicolonToken() {
if (semicolonToken != null) {
return semicolonToken;
}
semicolonToken = createToken(4);
return semicolonToken;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return typeName();
case 1:
return variableName();
case 2:
return equalsToken();
case 3:
return initializer();
case 4:
return semicolonToken();
}
return null;
}
}
