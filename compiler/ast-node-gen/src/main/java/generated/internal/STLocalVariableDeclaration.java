package generated.internal;
import generated.facade.*;

public  class STLocalVariableDeclaration extends STNode{
public final STToken typeName;
public final STToken variableName;
public final STToken equalsToken;
public final STNode initializer;
public final STToken semicolonToken;

public STLocalVariableDeclaration(SyntaxKind kind , STToken typeName, STToken variableName, STToken equalsToken, STNode initializer, STToken semicolonToken){
super(kind );
this.typeName = typeName;
this.variableName = variableName;
this.equalsToken = equalsToken;
this.initializer = initializer;
this.semicolonToken = semicolonToken;
this.bucketCount = 5;
this.childBuckets = new STNode[5];
this.addChildNode(typeName, 0);
this.addChildNode(variableName, 1);
this.addChildNode(equalsToken, 2);
this.addChildNode(initializer, 3);
this.addChildNode(semicolonToken, 4);
}

public STLocalVariableDeclaration(SyntaxKind kind, int width , STToken typeName, STToken variableName, STToken equalsToken, STNode initializer, STToken semicolonToken) {
super(kind, width );
this.typeName = typeName;
this.variableName = variableName;
this.equalsToken = equalsToken;
this.initializer = initializer;
this.semicolonToken = semicolonToken;
this.bucketCount = 5;
this.childBuckets = new STNode[5];
this.addChildNode(typeName, 0);
this.addChildNode(variableName, 1);
this.addChildNode(equalsToken, 2);
this.addChildNode(initializer, 3);
this.addChildNode(semicolonToken, 4);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new LocalVariableDeclaration(this, position, parent);
}
}
