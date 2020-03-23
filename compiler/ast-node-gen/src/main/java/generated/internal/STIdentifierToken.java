package generated.internal;
import generated.facade.*;

public  class STIdentifierToken extends STToken{
public final String text;

public STIdentifierToken(SyntaxKind kind , String text){
super(kind ,null,null);
this.text = text;
this.bucketCount = 1;
this.childBuckets = new STNode[1];
}

public STIdentifierToken(SyntaxKind kind, int width , String text) {
super(kind, width ,null,null);
this.text = text;
this.bucketCount = 1;
this.childBuckets = new STNode[1];
}

public String toString() {
return leadingTrivia + text + trailingTrivia;
}

}
