package org.ballerinalang.composer.service.workspace.langserver.dto;

/**
 * DTO for Text document positionDTO parameters
 */
public class TextDocumentPositionParams {

    /**
     * Here at the moment we keep the full text
     *
     * TODO: Need to refactor this to keep track of the document identifier. we need to issue didChange notifications
     */
    private String text;

    private Position position;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
