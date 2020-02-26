package generated.internal;
import generated.facade.*;
public class SyntaxTrivia extends SyntaxNode{
public final String text;

public SyntaxTrivia(SyntaxKind kind ,String text){
super(kind );
this.text=text;
this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}
public SyntaxTrivia(SyntaxKind kind, int width ,String text) {
super(kind, width );
this.text=text;
this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}
public String toString(){
return text;
}
public BLNode createFacade(int position, BLNonTerminalNode parent) {
throw new UnsupportedOperationException();
}
}
