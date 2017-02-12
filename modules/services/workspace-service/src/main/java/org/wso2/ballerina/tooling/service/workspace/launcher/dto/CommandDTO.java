package org.wso2.ballerina.tooling.service.workspace.launcher.dto;

/**
 * {@link CommandDTO} debug command DTO.
 *
 * @since 0.8.0
 */
public class CommandDTO {

    private String command;

    private String filePath;

    private String fileName;

    public CommandDTO(String command, String filePath, String fileName) {
        this.command = command;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
