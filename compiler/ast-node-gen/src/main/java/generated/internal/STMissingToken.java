package generated.internal;
import generated.facade.*;

public  class STMissingToken extends STToken{
public final boolean IS_MISSING;

public STMissingToken(SyntaxKind kind , boolean IS_MISSING){
super(kind ,null,null);
this.IS_MISSING = IS_MISSING;
this.bucketCount = 1;
this.childBuckets = new STNode[1];
}

public STMissingToken(SyntaxKind kind, int width , boolean IS_MISSING) {
super(kind, width ,null,null);
this.IS_MISSING = IS_MISSING;
this.bucketCount = 1;
this.childBuckets = new STNode[1];
}


}
