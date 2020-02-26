package generated.internal;
import generated.facade.*;
public class MissingToken extends SyntaxToken{
public final boolean IS_MISSING=true;

public MissingToken(SyntaxKind kind ){
super(kind ,null,null);

this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}
public MissingToken(SyntaxKind kind, int width ) {
super(kind, width ,null,null);

this.bucketCount = 1;
this.childBuckets = new SyntaxNode[1];

}


}
