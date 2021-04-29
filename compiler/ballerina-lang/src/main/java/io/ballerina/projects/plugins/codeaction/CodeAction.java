/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.projects.plugins.codeaction;

import java.util.List;

/**
 * Represents a code action.
 */
public class CodeAction {

    private String title;
    private String command;
    private List<CodeActionExecutor.CommandArg> arguments;

    private CodeAction(String title, String command, List<CodeActionExecutor.CommandArg> arguments) {
        this.title = title;
        this.command = command;
        this.arguments = arguments;
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return command;
    }

    public List<CodeActionExecutor.CommandArg> getArguments() {
        return arguments;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setArguments(List<CodeActionExecutor.CommandArg> arguments) {
        this.arguments = arguments;
    }

    public static CodeAction from(String title, String command, List<CodeActionExecutor.CommandArg> arguments) {
        return new CodeAction(title, command, arguments);
    }
}
