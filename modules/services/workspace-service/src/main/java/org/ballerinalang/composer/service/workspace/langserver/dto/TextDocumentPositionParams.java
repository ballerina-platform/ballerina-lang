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
    private String fileName;
    private String filePath;
    private String packageName;
    private boolean isDirty;

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


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }
}
