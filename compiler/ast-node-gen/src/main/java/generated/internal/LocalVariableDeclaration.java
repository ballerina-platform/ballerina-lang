package generated.internal;
import generated.facade.*;
public class LocalVariableDeclaration extends SyntaxNode{
public final SyntaxToken typeName;
public final SyntaxToken variableName;
public final SyntaxToken equalsToken;
public final SyntaxNode initializer;
public final SyntaxToken semicolonToken;

public LocalVariableDeclaration(SyntaxKind kind ,SyntaxToken typeName,SyntaxToken variableName,SyntaxToken equalsToken,SyntaxNode initializer,SyntaxToken semicolonToken){
super(kind );
this.typeName=typeName;
this.variableName=variableName;
this.equalsToken=equalsToken;
this.initializer=initializer;
this.semicolonToken=semicolonToken;
this.bucketCount = 5;
this.childBuckets = new SyntaxNode[5];
this.addChildNode(typeName,0);
this.addChildNode(variableName,1);
this.addChildNode(equalsToken,2);
this.addChildNode(initializer,3);
this.addChildNode(semicolonToken,4);

}
public LocalVariableDeclaration(SyntaxKind kind, int width ,SyntaxToken typeName,SyntaxToken variableName,SyntaxToken equalsToken,SyntaxNode initializer,SyntaxToken semicolonToken) {
super(kind, width );
this.typeName=typeName;
this.variableName=variableName;
this.equalsToken=equalsToken;
this.initializer=initializer;
this.semicolonToken=semicolonToken;
this.bucketCount = 5;
this.childBuckets = new SyntaxNode[5];
this.addChildNode(typeName,0);
this.addChildNode(variableName,1);
this.addChildNode(equalsToken,2);
this.addChildNode(initializer,3);
this.addChildNode(semicolonToken,4);

}

public BLNode createFacade(int position, BLNonTerminalNode parent) {
return new BLLocalVariableDeclaration(this, position, parent);
}
}
