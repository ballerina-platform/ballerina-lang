package io.ballerina.multiservice;

import org.eclipse.lsp4j.TextDocumentIdentifier;

public class MultiServiceModelRequest {
    private TextDocumentIdentifier documentIdentifier;

    public TextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }
}
