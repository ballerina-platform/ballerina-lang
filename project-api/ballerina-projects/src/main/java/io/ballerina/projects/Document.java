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
 * {@code Document} represents a Ballerina source file(.bal).
 *
 * @since 2.0.0
 */
public class Document {
    private final DocumentContext documentContext;
    private final Module module;
    private SyntaxTree syntaxTree;

    Document(DocumentContext documentContext, Module module) {
        this.documentContext = documentContext;
        this.module = module;
    }

    static Document from(DocumentContext documentContext, Module module) {
        return new Document(documentContext, module);
    }

    public DocumentId documentId() {
        return this.documentContext.documentId();
    }

    public Module module() {
        return this.module;
    }
    
    public SyntaxTree syntaxTree() {
        if (this.syntaxTree == null) {
            Path documentPath = Paths.get(documentId().documentPath());
            try {
                String text = new String(Files.readAllBytes(documentPath), StandardCharsets.UTF_8);
                TextDocument textDocument = TextDocuments.from(text);
                this.syntaxTree = SyntaxTree.from(textDocument);
            } catch (IOException e) {
                throw new RuntimeException("unable to parse file: " + documentPath);
            }
        }
        
        return this.syntaxTree;
    }

    public TextDocument textDocument() {
        return null;
    }
}
