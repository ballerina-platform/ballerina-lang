package generated.facade;
import generated.internal.SyntaxNode;
public class BLLocalVariableDeclaration extends BLNonTerminalNode{
private  BLSyntaxToken typeName;
private  BLSyntaxToken variableName;
private  BLSyntaxToken equalsToken;
private  BLNode initializer;
private  BLSyntaxToken semicolonToken;

public BLLocalVariableDeclaration(SyntaxNode node, int position, BLNonTerminalNode parent) {
super(node, position, parent);
}
public BLSyntaxToken typeName() {
if (typeName != null) {
return typeName;
}
typeName = createToken(0);
return typeName;
}
public BLSyntaxToken variableName() {
if (variableName != null) {
return variableName;
}
variableName = createToken(1);
return variableName;
}
public BLSyntaxToken equalsToken() {
if (equalsToken != null) {
return equalsToken;
}
equalsToken = createToken(2);
return equalsToken;
}
public BLNode initializer() {
if (initializer != null) {
return initializer;
}
initializer = node.childInBucket(3).createFacade(getChildPosition(3), this);
return initializer;
}
public BLSyntaxToken semicolonToken() {
if (semicolonToken != null) {
return semicolonToken;
}
semicolonToken = createToken(4);
return semicolonToken;
}

public BLNode childInBucket(int bucket) {
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
