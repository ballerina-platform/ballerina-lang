package generated.internal;
import generated.facade.*;

public  class STBlockStatement extends STStatement{
public final STToken openBraceToken;
public final STNode statementList;
public final STToken closeBraceToken;

public STBlockStatement(SyntaxKind kind , STToken openBraceToken, STNode statementList, STToken closeBraceToken){
super(kind );
this.openBraceToken = openBraceToken;
this.statementList = statementList;
this.closeBraceToken = closeBraceToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(openBraceToken, 0);
this.addChildNode(statementList, 1);
this.addChildNode(closeBraceToken, 2);
}

public STBlockStatement(SyntaxKind kind, int width , STToken openBraceToken, STNode statementList, STToken closeBraceToken) {
super(kind, width );
this.openBraceToken = openBraceToken;
this.statementList = statementList;
this.closeBraceToken = closeBraceToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(openBraceToken, 0);
this.addChildNode(statementList, 1);
this.addChildNode(closeBraceToken, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new BlockStatement(this, position, parent);
}
}
