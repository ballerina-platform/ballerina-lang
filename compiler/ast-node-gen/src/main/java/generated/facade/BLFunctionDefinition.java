package generated.facade;
import generated.internal.SyntaxNode;
public class BLFunctionDefinition extends BLNonTerminalNode{
private  BLSyntaxToken visibilityQual;
private  BLSyntaxToken functionKeyword;
private  BLSyntaxToken functionName;
private  BLSyntaxToken openParenToken;
private  BLSyntaxToken closeParenToken;
private  BLNode functionBody;

public BLFunctionDefinition(SyntaxNode node, int position, BLNonTerminalNode parent) {
super(node, position, parent);
}
public BLSyntaxToken visibilityQual() {
if (visibilityQual != null) {
return visibilityQual;
}
visibilityQual = createToken(0);
return visibilityQual;
}
public BLSyntaxToken functionKeyword() {
if (functionKeyword != null) {
return functionKeyword;
}
functionKeyword = createToken(1);
return functionKeyword;
}
public BLSyntaxToken functionName() {
if (functionName != null) {
return functionName;
}
functionName = createToken(2);
return functionName;
}
public BLSyntaxToken openParenToken() {
if (openParenToken != null) {
return openParenToken;
}
openParenToken = createToken(3);
return openParenToken;
}
public BLSyntaxToken closeParenToken() {
if (closeParenToken != null) {
return closeParenToken;
}
closeParenToken = createToken(4);
return closeParenToken;
}
public BLNode functionBody() {
if (functionBody != null) {
return functionBody;
}
functionBody = node.childInBucket(5).createFacade(getChildPosition(5), this);
return functionBody;
}

public BLNode childInBucket(int bucket) {
switch (bucket) {
case 0:
return visibilityQual();
case 1:
return functionKeyword();
case 2:
return functionName();
case 3:
return openParenToken();
case 4:
return closeParenToken();
case 5:
return functionBody();

}
return null;
}
}
