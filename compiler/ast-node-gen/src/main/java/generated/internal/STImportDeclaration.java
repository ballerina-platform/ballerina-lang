package generated.internal;
import generated.facade.*;

public  class STImportDeclaration extends STNode{

public STImportDeclaration(SyntaxKind kind ){
super(kind );
}

public STImportDeclaration(SyntaxKind kind, int width ) {
super(kind, width );
}


public Node createFacade(int position, NonTerminalNode parent) {
return new ImportDeclaration(this, position, parent);
}
}
