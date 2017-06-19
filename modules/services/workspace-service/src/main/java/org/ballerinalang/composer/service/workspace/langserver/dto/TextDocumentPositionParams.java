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

    private PositionDTO position;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PositionDTO getPosition() {
        return position;
    }

    public void setPosition(PositionDTO positionDTO) {
        this.position = positionDTO;
    }
}
