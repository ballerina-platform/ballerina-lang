package generated.internal;
import generated.facade.*;
public class ModulePart extends SyntaxNode{
public final SyntaxNode importList;
public final SyntaxNode memberList;
public final SyntaxToken eofToken;

public ModulePart(SyntaxKind kind ,SyntaxNode importList,SyntaxNode memberList,SyntaxToken eofToken){
super(kind );
this.importList=importList;
this.memberList=memberList;
this.eofToken=eofToken;
this.bucketCount = 3;
this.childBuckets = new SyntaxNode[3];
this.addChildNode(importList,0);
this.addChildNode(memberList,1);
this.addChildNode(eofToken,2);

}
public ModulePart(SyntaxKind kind, int width ,SyntaxNode importList,SyntaxNode memberList,SyntaxToken eofToken) {
super(kind, width );
this.importList=importList;
this.memberList=memberList;
this.eofToken=eofToken;
this.bucketCount = 3;
this.childBuckets = new SyntaxNode[3];
this.addChildNode(importList,0);
this.addChildNode(memberList,1);
this.addChildNode(eofToken,2);

}

public BLNode createFacade(int position, BLNonTerminalNode parent) {
return new BLModulePart(this, position, parent);
}
}
