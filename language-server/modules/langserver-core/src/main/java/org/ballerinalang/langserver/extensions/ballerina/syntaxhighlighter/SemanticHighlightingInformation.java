package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

public class SemanticHighlightingInformation {
    private int line;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
