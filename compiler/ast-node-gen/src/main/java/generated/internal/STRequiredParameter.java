package generated.internal;
import generated.facade.*;

public  class STRequiredParameter extends STParameter{
public final STToken leadingComma;
public final STNode accessModifier;
public final STNode type;
public final STNode paramName;

public STRequiredParameter(SyntaxKind kind , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName){
super(kind );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
}

public STRequiredParameter(SyntaxKind kind, int width , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName) {
super(kind, width );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new RequiredParameter(this, position, parent);
}
}
