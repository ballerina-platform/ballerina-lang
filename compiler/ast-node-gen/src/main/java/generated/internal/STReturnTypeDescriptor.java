package generated.internal;
import generated.facade.*;

public  class STReturnTypeDescriptor extends STNode{
public final STToken returnsKeyword;
public final STNode annotation;
public final STNode type;

public STReturnTypeDescriptor(SyntaxKind kind , STToken returnsKeyword, STNode annotation, STNode type){
super(kind );
this.returnsKeyword = returnsKeyword;
this.annotation = annotation;
this.type = type;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(returnsKeyword, 0);
this.addChildNode(annotation, 1);
this.addChildNode(type, 2);
}

public STReturnTypeDescriptor(SyntaxKind kind, int width , STToken returnsKeyword, STNode annotation, STNode type) {
super(kind, width );
this.returnsKeyword = returnsKeyword;
this.annotation = annotation;
this.type = type;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(returnsKeyword, 0);
this.addChildNode(annotation, 1);
this.addChildNode(type, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new ReturnTypeDescriptor(this, position, parent);
}
}
