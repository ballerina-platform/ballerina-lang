package generated.internal;
import generated.facade.*;

public  class STExternalFunctionBody extends STStatement{
public final STNode assign;
public final STNode annotation;
public final STToken externalKeyword;
public final STToken semicolon;

public STExternalFunctionBody(SyntaxKind kind , STNode assign, STNode annotation, STToken externalKeyword, STToken semicolon){
super(kind );
this.assign = assign;
this.annotation = annotation;
this.externalKeyword = externalKeyword;
this.semicolon = semicolon;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(assign, 0);
this.addChildNode(annotation, 1);
this.addChildNode(externalKeyword, 2);
this.addChildNode(semicolon, 3);
}

public STExternalFunctionBody(SyntaxKind kind, int width , STNode assign, STNode annotation, STToken externalKeyword, STToken semicolon) {
super(kind, width );
this.assign = assign;
this.annotation = annotation;
this.externalKeyword = externalKeyword;
this.semicolon = semicolon;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(assign, 0);
this.addChildNode(annotation, 1);
this.addChildNode(externalKeyword, 2);
this.addChildNode(semicolon, 3);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new ExternalFunctionBody(this, position, parent);
}
}
