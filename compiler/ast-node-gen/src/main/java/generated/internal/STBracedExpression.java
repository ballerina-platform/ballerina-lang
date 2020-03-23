package generated.internal;
import generated.facade.*;

public  class STBracedExpression extends STExpression{
public final STToken leadingComma;
public final STNode accessModifier;
public final STNode type;
public final STNode paramName;
public final STToken equal;
public final STNode expr;

public STBracedExpression(SyntaxKind kind , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName, STToken equal, STNode expr){
super(kind );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.equal = equal;
this.expr = expr;
this.bucketCount = 6;
this.childBuckets = new STNode[6];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
this.addChildNode(equal, 4);
this.addChildNode(expr, 5);
}

public STBracedExpression(SyntaxKind kind, int width , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName, STToken equal, STNode expr) {
super(kind, width );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.equal = equal;
this.expr = expr;
this.bucketCount = 6;
this.childBuckets = new STNode[6];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
this.addChildNode(equal, 4);
this.addChildNode(expr, 5);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new BracedExpression(this, position, parent);
}
}
