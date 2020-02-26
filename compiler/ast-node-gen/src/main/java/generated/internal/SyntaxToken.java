package generated.internal;
import generated.facade.*;
public class SyntaxToken extends SyntaxNode{
public final SyntaxNode leadingTrivia;
public final SyntaxNode trailingTrivia;

public SyntaxToken(SyntaxKind kind ,SyntaxNode leadingTrivia,SyntaxNode trailingTrivia){
super(kind );
this.leadingTrivia=leadingTrivia;
this.trailingTrivia=trailingTrivia;
this.bucketCount = 2;
this.childBuckets = new SyntaxNode[2];
this.addChildNode(leadingTrivia,0);
this.addChildNode(trailingTrivia,1);

}
public SyntaxToken(SyntaxKind kind, int width ,SyntaxNode leadingTrivia,SyntaxNode trailingTrivia) {
super(kind, width );
this.leadingTrivia=leadingTrivia;
this.trailingTrivia=trailingTrivia;
this.bucketCount = 2;
this.childBuckets = new SyntaxNode[2];
this.addChildNode(leadingTrivia,0);
this.addChildNode(trailingTrivia,1);

}
public String toString(){
return leadingTrivia+kind.strValue+trailingTrivia;
}
public BLNode createFacade(int position, BLNonTerminalNode parent) {
return new BLSyntaxToken(this, position, parent);
}
}
