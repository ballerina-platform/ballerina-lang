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
package io.ballerina.parsers;

/**
 * Represents a request for partial ST Modify.
 *
 * @since 1.3.0
 */
public class STModification {
    private int startLine;
    private int startColumn;
    private int endLine;
    private int endColumn;
    private String newCodeSnippet;

    public STModification (int startLine, int startColumn, int endLine, int endColumn, String newCodeSnippet) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.newCodeSnippet = newCodeSnippet;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    public String getNewCodeSnippet() {
        return newCodeSnippet;
    }

    public void setNewCodeSnippet(String newCodeSnippet) {
        this.newCodeSnippet = newCodeSnippet;
    }
}
