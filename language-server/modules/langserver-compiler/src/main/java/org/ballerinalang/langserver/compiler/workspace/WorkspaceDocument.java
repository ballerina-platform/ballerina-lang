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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
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
    private LSDocumentIdentifier lsDocument;
    private SyntaxTree tree;

    public WorkspaceDocument(Path path, String content, boolean isTempFile) {
        this.path = path;
        this.content = content;
        Path namePart = path.getFileName();
        if (namePart != null) {
            String fileName = namePart.toString();
            setTree(SyntaxTree.from(TextDocuments.from(this.content), fileName));
        } else {
            setTree(SyntaxTree.from(TextDocuments.from(this.content)));
        }
        this.codeLenses = new ArrayList<>();
        lsDocument = isTempFile ? null : new LSDocumentIdentifierImpl(path.toUri().toString());
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
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        Path namePart = this.path.getFileName();
        // TODO: Fix this, each time creates a new tree
        if (namePart != null) {
            setTree(SyntaxTree.from(TextDocuments.from(this.content), namePart.toString()));
        } else {
            setTree(SyntaxTree.from(TextDocuments.from(this.content)));
        }
    }

    public void setIncrementContent(String content) {
        this.content = content;
        // TODO: Fix this, each time creates a new tree
        Path namePath = this.path.getFileName();
        if (namePath != null) {
            setTree(SyntaxTree.from(TextDocuments.from(this.content), namePath.toString()));
        } else {
            setTree(SyntaxTree.from(TextDocuments.from(this.content)));
        }
    }

    public SyntaxTree getTree() {
        return this.tree;
    }

    public void setTree(SyntaxTree tree) {
        this.tree = tree;
        // TODO: Added to support inter-operability. Remove this once getContent() is removed
        this.content = tree.toSourceCode();
    }

    public LSDocumentIdentifier getLSDocument() {
        return lsDocument;
    }

    @Override
    public String toString() {
        return "{" + "path:" + this.path + ", content:" + this.content + "}";
    }
}
