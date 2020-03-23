package generated.facade;
import generated.internal.STNode;

public  class ModulePart extends NonTerminalNode{
private NodeList<Node> importList;
private NodeList<Node> memberList;
private Token eofToken;

public ModulePart(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public NodeList<Node> importList() {
if (importList != null) {
return importList;
}
importList = createListNode(0);
return importList;
}
public NodeList<Node> memberList() {
if (memberList != null) {
return memberList;
}
memberList = createListNode(1);
return memberList;
}
public Token eofToken() {
if (eofToken != null) {
return eofToken;
}
eofToken = createToken(2);
return eofToken;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return importList();
case 1:
return memberList();
case 2:
return eofToken();
}
return null;
}
}
