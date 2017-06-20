package org.ballerinalang.composer.service.workspace.langserver.dto;

/**
 * DTO to represent the position inside a document
 */
public class Position {
    private int line;

    private int character;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }
}
