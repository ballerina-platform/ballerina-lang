/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maintains the internal state of a {@code Document} instance.
 * <p>
 * Works as a document cache.
 *
 * @since 2.0.0
 */
class DocumentContext {
    private final DocumentConfig documentConfig;
    private SyntaxTree syntaxTree;
    private TextDocument textDocument;

    private DocumentContext(DocumentConfig documentConfig) {
        this.documentConfig = documentConfig;
    }

    static DocumentContext from(DocumentConfig documentConfig) {
        return new DocumentContext(documentConfig);
    }

    DocumentId documentId() {
        return documentConfig.documentId();
    }

    public SyntaxTree syntaxTree() {
        if (this.syntaxTree != null) {
            return this.syntaxTree;
        }

        this.syntaxTree = SyntaxTree.from(this.textDocument());
        return this.syntaxTree;
    }

    public TextDocument textDocument() {
        if (this.textDocument != null) {
            return this.textDocument;
        }

        // TODO: The content should be loaded from a TextLoader
        Path documentPath = Paths.get(documentId().documentPath());
        try {
            String text = new String(Files.readAllBytes(documentPath), StandardCharsets.UTF_8);
            this.textDocument = TextDocuments.from(text);
        } catch (IOException e) {
            // TODO improve error handling
            throw new RuntimeException("Unable to read file: " + documentPath);
        }
        return this.textDocument;
    }
}
