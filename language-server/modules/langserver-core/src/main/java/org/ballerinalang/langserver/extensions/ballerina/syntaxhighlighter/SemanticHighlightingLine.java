package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

public class SemanticHighlightingLine {
    private int line;
    private SemanticHighlightingToken[] tokens;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public SemanticHighlightingToken[] getTokens() {
        return tokens;
    }

    public void setTokens(SemanticHighlightingToken[] tokens) {
        this.tokens = tokens;
    }
}
