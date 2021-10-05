package io.ballerina;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class PerformanceAnalyzerGraphRequest {
    private TextDocumentIdentifier documentIdentifier;
    private Range range;

    public TextDocumentIdentifier getDocumentIdentifier() {

        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {

        this.documentIdentifier = documentIdentifier;
    }

    public Range getRange() {

        return range;
    }

    public void setRange(Range range) {

        this.range = range;
    }
}
