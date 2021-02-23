package org.ballerinalang.langserver.extensions.ballerina.document;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Represents a request for a Ballerina SyntaxTree by Range.
 *
 */

public class BallerinaSyntaxTreeByRangeRequest {

    private TextDocumentIdentifier documentIdentifier;
    private Range lineRange;

    public BallerinaSyntaxTreeByRangeRequest() {
    }

    public BallerinaSyntaxTreeByRangeRequest(TextDocumentIdentifier documentIdentifier, Range lineRange) {
        this.documentIdentifier = documentIdentifier;
        this.lineRange = lineRange;
    }

    public TextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public Range getLineRange() {
        return lineRange;
    }

    public void setLineRange(Range lineRange) {
        this.lineRange = lineRange;
    }

}
