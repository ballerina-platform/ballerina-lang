package io.ballerina;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class PerformanceAnalyzerGraphRequest {
    private TextDocumentIdentifier documentIdentifier;
    private Range range;
    private String choreoToken;
    private String choreoCookie;

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

    public String getChoreoToken() {

        return choreoToken;
    }

    public void setChoreoToken(String choreoToken) {

        this.choreoToken = choreoToken;
    }

    public String getChoreoCookie() {

        return choreoCookie;
    }

    public void setChoreoCookie(String choreoCookie) {

        this.choreoCookie = choreoCookie;
    }
}
