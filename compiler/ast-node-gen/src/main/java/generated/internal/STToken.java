package generated.internal;
import generated.facade.*;

public  class STToken extends STNode{
public final STNode leadingTrivia;
public final STNode trailingTrivia;

public STToken(SyntaxKind kind , STNode leadingTrivia, STNode trailingTrivia){
super(kind );
this.leadingTrivia = leadingTrivia;
this.trailingTrivia = trailingTrivia;
this.bucketCount = 2;
this.childBuckets = new STNode[2];
this.addChildNode(leadingTrivia, 0);
this.addChildNode(trailingTrivia, 1);
}

public STToken(SyntaxKind kind, int width , STNode leadingTrivia, STNode trailingTrivia) {
super(kind, width );
this.leadingTrivia = leadingTrivia;
this.trailingTrivia = trailingTrivia;
this.bucketCount = 2;
this.childBuckets = new STNode[2];
this.addChildNode(leadingTrivia, 0);
this.addChildNode(trailingTrivia, 1);
}

public String toString() {
return leadingTrivia + kind.strValue + trailingTrivia;
}

public Node createFacade(int position, NonTerminalNode parent) {
return new Token(this, position, parent);
}
}
