package io.ballerina.projects;

import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

public class PackageMdContext {
    private TextDocument textDocument;
    private DocumentId documentId;
    private String name;
    private String content;

    private PackageMdContext(DocumentId documentId, String name, String content) {
        this.documentId = documentId;
        this.name = name;
        this.content = content;
    }

    static PackageMdContext from(DocumentConfig documentConfig) {
        return new PackageMdContext(documentConfig.documentId(), documentConfig.name(), documentConfig.content());
    }

    DocumentId documentId() {
        return this.documentId;
    }

    String name() {
        return this.name;
    }

    TextDocument textDocument() {
        if (this.textDocument == null) {
            this.textDocument = TextDocuments.from(this.content);
        }
        return this.textDocument;
    }

}
