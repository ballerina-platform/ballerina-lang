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

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a document open in workspace.
 */
public class WorkspaceDocument {
    public static final String LINE_SEPARATOR_SPLIT = "\\r?\\n";

    /* Tracking code lenses sent to client, to make-use in compilation failures */
    private List<CodeLens> codeLenses;
    private Path path;
    private String content;
    private String prunedContent;
    private boolean usePrunedSource;

    public WorkspaceDocument(Path path, String content) {
        this.path = path;
        this.content = content;
        this.codeLenses = new ArrayList<>();
        this.usePrunedSource = false;
    }

    public List<CodeLens> getCodeLenses() {
        return codeLenses;
    }

    public void setCodeLenses(List<CodeLens> codeLenses) {
        this.codeLenses = codeLenses;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getContent() {
        /*
        If the pruned source flag is true, return the pruned source. After single access, the pruned source will be 
        stale, and hence set to null. If a certain operation need to use the pruned source, then the operation set the
        pruned source within the operation as well as rhe flag
         */
        if (this.usePrunedSource) {
            this.usePrunedSource = false;
            String prunedSourceCopy = this.prunedContent;
            this.prunedContent = null;
            return prunedSourceCopy;
        }
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent(Range range, String content) {
        this.content = getRangeTextAppliedContent(range, content, this.content);
    }

    public void setPrunedContent(String prunedContent) {
        this.prunedContent = prunedContent;
        this.usePrunedSource = true;
    }

    private String getRangeTextAppliedContent(Range range, String newText, String oldText) {
        if (range == null) {
            return newText;
        }
        String trimmedOldText = oldText.trim();
        int trimmedTextStart = oldText.indexOf(trimmedOldText);
        int trailingNewLines = oldText.substring(trimmedTextStart + trimmedOldText.length())
                .replaceAll("\\r?\\n", " ").length();
        List<String> oldTextLines = new ArrayList<>(Arrays.asList(oldText.split(LINE_SEPARATOR_SPLIT)));
        oldTextLines.addAll(Collections.nCopies(trailingNewLines, ""));
        Position start = range.getStart();
        Position end = range.getEnd();
        int rangeLineLength = end.getLine() - start.getLine();
        if (rangeLineLength == 0) {
            // single line edit
            String line = oldTextLines.get(start.getLine());
            String mLine = line.substring(0, start.getCharacter()) + newText + line.substring(end.getCharacter());
            oldTextLines.set(start.getLine(), mLine);
        } else {
            // multi-line edit

            String trimmedNewText = newText.trim();
            int trimmedNewTextStart = newText.indexOf(trimmedNewText);
            int newTextTrailingNewLines = newText.substring(trimmedNewTextStart + trimmedNewText.length())
                    .replaceAll("\\r?\\n", " ").length();
            List<String> newTextList = new ArrayList<>(Arrays.asList(newText.split(LINE_SEPARATOR_SPLIT)));
            newTextList.addAll(Collections.nCopies(newTextTrailingNewLines, ""));

            String sLine = oldTextLines.get(start.getLine());
            String eLine = oldTextLines.get(end.getLine());

            // remove lines
            int i = 0;
            while (i <= rangeLineLength) {
                oldTextLines.remove(start.getLine());
                i++;
            }

            //add lines
            int j = 0;
            while (j < newTextList.size()) {
                String changeText = newTextList.get(j);
                String prefix = (j == 0) ? sLine.substring(0, start.getCharacter()) : "";
                String suffix = (j == newTextList.size() - 1) ? eLine.substring(end.getCharacter()) : "";
                String mLine = prefix + changeText + suffix;
                oldTextLines.add(start.getLine() + j, mLine);
                j++;
            }
        }
        return String.join(System.lineSeparator(), oldTextLines);
    }

    @Override
    public String toString() {
        String cont = (this.usePrunedSource) ? prunedContent : this.content;
        return "{" + "path:" + this.path + ", content:" + cont + "}";
    }
}
