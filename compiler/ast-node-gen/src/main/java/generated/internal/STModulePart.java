package generated.internal;
import generated.facade.*;

public  class STModulePart extends STNode{
public final STNode importList;
public final STNode memberList;
public final STToken eofToken;

public STModulePart(SyntaxKind kind , STNode importList, STNode memberList, STToken eofToken){
super(kind );
this.importList = importList;
this.memberList = memberList;
this.eofToken = eofToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(importList, 0);
this.addChildNode(memberList, 1);
this.addChildNode(eofToken, 2);
}

public STModulePart(SyntaxKind kind, int width , STNode importList, STNode memberList, STToken eofToken) {
super(kind, width );
this.importList = importList;
this.memberList = memberList;
this.eofToken = eofToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(importList, 0);
this.addChildNode(memberList, 1);
this.addChildNode(eofToken, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new ModulePart(this, position, parent);
}
}
