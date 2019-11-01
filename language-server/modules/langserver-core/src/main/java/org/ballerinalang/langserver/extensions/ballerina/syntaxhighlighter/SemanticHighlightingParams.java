package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

public class SemanticHighlightingParams {
    private VersionedTextDocumentIdentifier textDocument;
    private SemanticHighlightingInformation[] lines;

    public VersionedTextDocumentIdentifier getTextDocument() {
        return textDocument;
    }

    public void setTextDocument(VersionedTextDocumentIdentifier textDocument) {
        this.textDocument = textDocument;
    }

    public SemanticHighlightingInformation[] getLines() {
        return lines;
    }

    public void setLines(SemanticHighlightingInformation[] lines) {
        this.lines = lines;
    }
}
