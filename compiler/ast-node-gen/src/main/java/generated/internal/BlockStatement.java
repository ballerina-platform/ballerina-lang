package generated.internal;
import generated.facade.*;
public class BlockStatement extends StatementNode{
public final SyntaxToken openBraceToken;
public final SyntaxNode statementList;
public final SyntaxToken closeBraceToken;

public BlockStatement(SyntaxKind kind ,SyntaxToken openBraceToken,SyntaxNode statementList,SyntaxToken closeBraceToken){
super(kind );
this.openBraceToken=openBraceToken;
this.statementList=statementList;
this.closeBraceToken=closeBraceToken;
this.bucketCount = 3;
this.childBuckets = new SyntaxNode[3];
this.addChildNode(openBraceToken,0);
this.addChildNode(statementList,1);
this.addChildNode(closeBraceToken,2);

}
public BlockStatement(SyntaxKind kind, int width ,SyntaxToken openBraceToken,SyntaxNode statementList,SyntaxToken closeBraceToken) {
super(kind, width );
this.openBraceToken=openBraceToken;
this.statementList=statementList;
this.closeBraceToken=closeBraceToken;
this.bucketCount = 3;
this.childBuckets = new SyntaxNode[3];
this.addChildNode(openBraceToken,0);
this.addChildNode(statementList,1);
this.addChildNode(closeBraceToken,2);

}

public BLNode createFacade(int position, BLNonTerminalNode parent) {
return new BLBlockStatement(this, position, parent);
}
}
