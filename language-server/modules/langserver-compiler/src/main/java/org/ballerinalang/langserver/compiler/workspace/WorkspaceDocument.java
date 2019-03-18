/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.compiler.workspace;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a document open in workspace.
 */
public class WorkspaceDocument {
    public static final String LINE_SEPARATOR_SPLIT = "\\r?\\n";

    private Path path;
    private String content;

    public WorkspaceDocument(Path path, String content) {
        this.path = path;
        this.content = content;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent(Range range, String content) {
        this.content = getRangeTextAppliedContent(range, content, this.content);
    }

    private String getRangeTextAppliedContent(Range range, String newText, String oldText) {
        if (range == null) {
            return newText;
        }
        List<String> content = new ArrayList<>(Arrays.asList(oldText.split(LINE_SEPARATOR_SPLIT)));
        Position start = range.getStart();
        Position end = range.getEnd();
        int rangeLineLength = end.getLine() - start.getLine();
        if (rangeLineLength == 0) {
            // single line edit
            String line = content.get(start.getLine());
            String mLine = line.substring(0, start.getCharacter()) + newText + line.substring(end.getCharacter());
            content.set(start.getLine(), mLine);
        } else {
            // multi-line edit
            String[] newTextArr = newText.split(LINE_SEPARATOR_SPLIT);
            String sLine = content.get(start.getLine());
            String eLine = content.get(end.getLine());

            // remove lines
            int i = 0;
            while (i <= rangeLineLength) {
                content.remove(start.getLine());
                i++;
            }

            //add lines
            int j = 0;
            while (j < newTextArr.length) {
                String changeText = newTextArr[j];
                String prefix = (j == 0) ? sLine.substring(0, start.getCharacter()) : "";
                String suffix = (j == newTextArr.length - 1) ? eLine.substring(end.getCharacter()) : "";
                String mLine = prefix + changeText + suffix;
                content.add(start.getLine(), mLine);
                j++;
            }
        }
        return String.join(System.lineSeparator(), content);
    }

    @Override
    public String toString() {
        return "{" + "path:" + this.path + ", content:" + this.content + "}";
    }
}
