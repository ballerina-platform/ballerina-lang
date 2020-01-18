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

import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.eclipse.lsp4j.CodeLens;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a document open in workspace.
 */
public class WorkspaceDocument {
    /* Tracking code lenses sent to client, to make-use in compilation failures */
    private List<CodeLens> codeLenses;
    private Path path;
    private String content;
    private String prunedContent;
    private boolean usePrunedSource;
    private LSDocument lsDocument;

    public WorkspaceDocument(Path path, String content, boolean isTempFile) {
        this.path = path;
        this.content = content;
        this.codeLenses = new ArrayList<>();
        this.usePrunedSource = false;
        lsDocument = isTempFile ? null : new LSDocument(path.toUri().toString());
    }

    public WorkspaceDocument(Path path, String content) {
        this(path, content, false);
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
            return this.prunedContent;
        }
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPrunedContent(String prunedContent) {
        this.prunedContent = prunedContent;
        this.usePrunedSource = true;
    }

    public void resetPrunedContent() {

        this.prunedContent = null;
        this.usePrunedSource = false;
    }

    public LSDocument getLSDocument() {
        return lsDocument;
    }

    @Override
    public String toString() {
        String cont = (this.usePrunedSource) ? prunedContent : this.content;
        return "{" + "path:" + this.path + ", content:" + cont + "}";
    }
}
