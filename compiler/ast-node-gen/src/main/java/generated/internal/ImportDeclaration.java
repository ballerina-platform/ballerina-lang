package generated.internal;
import generated.facade.*;
public class ImportDeclaration extends SyntaxNode{

public ImportDeclaration(SyntaxKind kind ){
super(kind );


}
public ImportDeclaration(SyntaxKind kind, int width ) {
super(kind, width );


}

public BLNode createFacade(int position, BLNonTerminalNode parent) {
return new BLImportDeclaration(this, position, parent);
}
}
