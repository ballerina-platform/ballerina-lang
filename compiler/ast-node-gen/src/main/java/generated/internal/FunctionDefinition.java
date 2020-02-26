package generated.internal;
import generated.facade.*;
public class FunctionDefinition extends SyntaxNode{
public final SyntaxToken visibilityQual;
public final SyntaxToken functionKeyword;
public final SyntaxToken functionName;
public final SyntaxToken openParenToken;
public final SyntaxToken closeParenToken;
public final SyntaxNode functionBody;

public FunctionDefinition(SyntaxKind kind ,SyntaxToken visibilityQual,SyntaxToken functionKeyword,SyntaxToken functionName,SyntaxToken openParenToken,SyntaxToken closeParenToken,SyntaxNode functionBody){
super(kind );
this.visibilityQual=visibilityQual;
this.functionKeyword=functionKeyword;
this.functionName=functionName;
this.openParenToken=openParenToken;
this.closeParenToken=closeParenToken;
this.functionBody=functionBody;
this.bucketCount = 6;
this.childBuckets = new SyntaxNode[6];
this.addChildNode(visibilityQual,0);
this.addChildNode(functionKeyword,1);
this.addChildNode(functionName,2);
this.addChildNode(openParenToken,3);
this.addChildNode(closeParenToken,4);
this.addChildNode(functionBody,5);

}
public FunctionDefinition(SyntaxKind kind, int width ,SyntaxToken visibilityQual,SyntaxToken functionKeyword,SyntaxToken functionName,SyntaxToken openParenToken,SyntaxToken closeParenToken,SyntaxNode functionBody) {
super(kind, width );
this.visibilityQual=visibilityQual;
this.functionKeyword=functionKeyword;
this.functionName=functionName;
this.openParenToken=openParenToken;
this.closeParenToken=closeParenToken;
this.functionBody=functionBody;
this.bucketCount = 6;
this.childBuckets = new SyntaxNode[6];
this.addChildNode(visibilityQual,0);
this.addChildNode(functionKeyword,1);
this.addChildNode(functionName,2);
this.addChildNode(openParenToken,3);
this.addChildNode(closeParenToken,4);
this.addChildNode(functionBody,5);

}

public BLNode createFacade(int position, BLNonTerminalNode parent) {
return new BLFunctionDefinition(this, position, parent);
}
}
