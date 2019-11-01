package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

public class SemanticHighlightingToken {
    public int character;
    public int length;
    public int scope;

    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

}
