package generated.internal;
import generated.facade.*;

public  class STFunctionDefinition extends STNode{
public final STNode visibilityQual;
public final STToken functionKeyword;
public final STNode functionName;
public final STToken openParenToken;
public final STNode parameters;
public final STToken closeParenToken;
public final STNode returnTypeDescriptor;
public final STNode functionBody;

public STFunctionDefinition(SyntaxKind kind , STNode visibilityQual, STToken functionKeyword, STNode functionName, STToken openParenToken, STNode parameters, STToken closeParenToken, STNode returnTypeDescriptor, STNode functionBody){
super(kind );
this.visibilityQual = visibilityQual;
this.functionKeyword = functionKeyword;
this.functionName = functionName;
this.openParenToken = openParenToken;
this.parameters = parameters;
this.closeParenToken = closeParenToken;
this.returnTypeDescriptor = returnTypeDescriptor;
this.functionBody = functionBody;
this.bucketCount = 8;
this.childBuckets = new STNode[8];
this.addChildNode(visibilityQual, 0);
this.addChildNode(functionKeyword, 1);
this.addChildNode(functionName, 2);
this.addChildNode(openParenToken, 3);
this.addChildNode(parameters, 4);
this.addChildNode(closeParenToken, 5);
this.addChildNode(returnTypeDescriptor, 6);
this.addChildNode(functionBody, 7);
}

public STFunctionDefinition(SyntaxKind kind, int width , STNode visibilityQual, STToken functionKeyword, STNode functionName, STToken openParenToken, STNode parameters, STToken closeParenToken, STNode returnTypeDescriptor, STNode functionBody) {
super(kind, width );
this.visibilityQual = visibilityQual;
this.functionKeyword = functionKeyword;
this.functionName = functionName;
this.openParenToken = openParenToken;
this.parameters = parameters;
this.closeParenToken = closeParenToken;
this.returnTypeDescriptor = returnTypeDescriptor;
this.functionBody = functionBody;
this.bucketCount = 8;
this.childBuckets = new STNode[8];
this.addChildNode(visibilityQual, 0);
this.addChildNode(functionKeyword, 1);
this.addChildNode(functionName, 2);
this.addChildNode(openParenToken, 3);
this.addChildNode(parameters, 4);
this.addChildNode(closeParenToken, 5);
this.addChildNode(returnTypeDescriptor, 6);
this.addChildNode(functionBody, 7);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new FunctionDefinition(this, position, parent);
}
}
