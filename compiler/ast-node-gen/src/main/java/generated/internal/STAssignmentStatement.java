package generated.internal;
import generated.facade.*;

public  class STAssignmentStatement extends STStatement{
public final STNode varRef;
public final STNode equalsToken;
public final STNode expr;
public final STNode semicolonToken;

public STAssignmentStatement(SyntaxKind kind , STNode varRef, STNode equalsToken, STNode expr, STNode semicolonToken){
super(kind );
this.varRef = varRef;
this.equalsToken = equalsToken;
this.expr = expr;
this.semicolonToken = semicolonToken;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(varRef, 0);
this.addChildNode(equalsToken, 1);
this.addChildNode(expr, 2);
this.addChildNode(semicolonToken, 3);
}

public STAssignmentStatement(SyntaxKind kind, int width , STNode varRef, STNode equalsToken, STNode expr, STNode semicolonToken) {
super(kind, width );
this.varRef = varRef;
this.equalsToken = equalsToken;
this.expr = expr;
this.semicolonToken = semicolonToken;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(varRef, 0);
this.addChildNode(equalsToken, 1);
this.addChildNode(expr, 2);
this.addChildNode(semicolonToken, 3);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new AssignmentStatement(this, position, parent);
}
}
