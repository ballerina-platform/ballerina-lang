package generated.internal;
import generated.facade.*;
public class LiteralValueToken extends SyntaxToken{
public final String text;

public LiteralValueToken(SyntaxKind kind ,String text){
super(kind ,null,null);
this.text=text;
this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}
public LiteralValueToken(SyntaxKind kind, int width ,String text) {
super(kind, width ,null,null);
this.text=text;
this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}
public String toString(){
return leadingTrivia+text+trailingTrivia;
}

}
