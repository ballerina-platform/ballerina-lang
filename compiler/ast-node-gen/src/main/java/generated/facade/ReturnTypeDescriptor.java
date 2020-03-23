package generated.facade;
import generated.internal.STNode;

public  class ReturnTypeDescriptor extends NonTerminalNode{
private Token returnsKeyword;
private Node annotation;
private Node type;

public ReturnTypeDescriptor(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Token returnsKeyword() {
if (returnsKeyword != null) {
return returnsKeyword;
}
returnsKeyword = createToken(0);
return returnsKeyword;
}
public Node annotation() {
if (annotation != null) {
return annotation;
}
annotation = node.childInBucket(1).createFacade(getChildPosition(1), this);
return annotation;
}
public Node type() {
if (type != null) {
return type;
}
type = node.childInBucket(2).createFacade(getChildPosition(2), this);
return type;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return returnsKeyword();
case 1:
return annotation();
case 2:
return type();
}
return null;
}
}
