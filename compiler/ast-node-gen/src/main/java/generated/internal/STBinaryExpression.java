package generated.internal;
import generated.facade.*;

public  class STBinaryExpression extends STExpression{
public final STNode lhsExpr;
public final STNode operator;
public final STNode rhsExpr;

public STBinaryExpression(SyntaxKind kind , STNode lhsExpr, STNode operator, STNode rhsExpr){
super(kind );
this.lhsExpr = lhsExpr;
this.operator = operator;
this.rhsExpr = rhsExpr;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(lhsExpr, 0);
this.addChildNode(operator, 1);
this.addChildNode(rhsExpr, 2);
}

public STBinaryExpression(SyntaxKind kind, int width , STNode lhsExpr, STNode operator, STNode rhsExpr) {
super(kind, width );
this.lhsExpr = lhsExpr;
this.operator = operator;
this.rhsExpr = rhsExpr;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(lhsExpr, 0);
this.addChildNode(operator, 1);
this.addChildNode(rhsExpr, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new BinaryExpression(this, position, parent);
}
}
