/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.ballerina.launcher.service.dto;

/**
 * {@link CommandDTO} launcher command DTO.
 *
 */
public class CommandDTO {

    private String command;

    private String filePath;

    private String fileName;

    private String[] commandArgs;

    public CommandDTO(String command, String filePath, String fileName, String[] args) {
        this.command = command;
        this.filePath = filePath;
        this.fileName = fileName;
        this.commandArgs = args;
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

    public String[] getCommandArgs() {
        return commandArgs.clone();
    }

    public void setCommandArgs(String[] commandArgs) {
        this.commandArgs = commandArgs;
    }
}
