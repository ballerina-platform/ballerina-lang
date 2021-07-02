package org.ballerinalang.langserver.extensions.ballerina.document;

import org.eclipse.lsp4j.TextDocumentIdentifier;

public class TypeSymbolRequest {
    private String variableName;
    private TextDocumentIdentifier documentIdentifier;
    private ASTModification[] astModifications;

    public TypeSymbolRequest() {
    }

    public TypeSymbolRequest(String variableName,TextDocumentIdentifier documentIdentifier,
                                            ASTModification[] astModifications) {
        this.variableName = variableName;
        this.documentIdentifier = documentIdentifier;
        this.astModifications = astModifications;
    }

    public ASTModification[] getAstModifications() {
        return astModifications;
    }

    public void setAstModifications(ASTModification[] astModifications) {
        this.astModifications = astModifications;
    }

    public TextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
