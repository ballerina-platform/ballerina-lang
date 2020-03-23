package generated.internal;
import generated.facade.*;

public  class STRestParameter extends STParameter{
public final STToken leadingComma;
public final STNode type;
public final STNode ellipsis;
public final STNode paramName;

public STRestParameter(SyntaxKind kind , STToken leadingComma, STNode type, STNode ellipsis, STNode paramName){
super(kind );
this.leadingComma = leadingComma;
this.type = type;
this.ellipsis = ellipsis;
this.paramName = paramName;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(leadingComma, 0);
this.addChildNode(type, 1);
this.addChildNode(ellipsis, 2);
this.addChildNode(paramName, 3);
}

public STRestParameter(SyntaxKind kind, int width , STToken leadingComma, STNode type, STNode ellipsis, STNode paramName) {
super(kind, width );
this.leadingComma = leadingComma;
this.type = type;
this.ellipsis = ellipsis;
this.paramName = paramName;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(leadingComma, 0);
this.addChildNode(type, 1);
this.addChildNode(ellipsis, 2);
this.addChildNode(paramName, 3);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new RestParameter(this, position, parent);
}
}
