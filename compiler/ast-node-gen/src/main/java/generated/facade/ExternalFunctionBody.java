package generated.facade;
import generated.internal.STNode;

public  class ExternalFunctionBody extends NonTerminalNode{
private Node assign;
private Node annotation;
private Token externalKeyword;
private Token semicolon;

public ExternalFunctionBody(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Node assign() {
if (assign != null) {
return assign;
}
assign = node.childInBucket(0).createFacade(getChildPosition(0), this);
return assign;
}
public Node annotation() {
if (annotation != null) {
return annotation;
}
annotation = node.childInBucket(1).createFacade(getChildPosition(1), this);
return annotation;
}
public Token externalKeyword() {
if (externalKeyword != null) {
return externalKeyword;
}
externalKeyword = createToken(2);
return externalKeyword;
}
public Token semicolon() {
if (semicolon != null) {
return semicolon;
}
semicolon = createToken(3);
return semicolon;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return assign();
case 1:
return annotation();
case 2:
return externalKeyword();
case 3:
return semicolon();
}
return null;
}
}
