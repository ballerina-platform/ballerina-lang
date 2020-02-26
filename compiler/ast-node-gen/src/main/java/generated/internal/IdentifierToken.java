package generated.internal;
import generated.facade.*;
public class IdentifierToken extends SyntaxToken{
public final String text;

public IdentifierToken(SyntaxKind kind ,String text){
super(kind ,null,null);
this.text=text;
this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}
public IdentifierToken(SyntaxKind kind, int width ,String text) {
super(kind, width ,null,null);
this.text=text;
this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}
public String toString(){
return leadingTrivia+text+trailingTrivia;
}

}
