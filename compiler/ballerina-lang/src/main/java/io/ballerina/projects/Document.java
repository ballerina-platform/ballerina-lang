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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;

/**
 * {@code Document} represents a Ballerina source file(.bal).
 *
 * @since 2.0.0
 */
public class Document {
    private final DocumentContext documentContext;
    private final Module module;

    Document(DocumentContext documentContext, Module module) {
        this.documentContext = documentContext;
        this.module = module;
    }

    public static Document from(DocumentConfig documentConfig, Module module) {
        DocumentContext documentContext = DocumentContext.from(documentConfig, false);
        return new Document(documentContext, module);
    }

    public DocumentId documentId() {
        return this.documentContext.documentId();
    }

    public String name() {
        return this.documentContext.name();
    }

    public Module module() {
        return this.module;
    }
    
    public SyntaxTree syntaxTree() {
        return this.documentContext.syntaxTree();
    }

    public TextDocument textDocument() {
        return this.documentContext.textDocument();
    }

    /** Returns an instance of the Document.Modifier.
     *
     * @return  module modifier
     */
    public Modifier modify() {
        return new Modifier(this);
    }

    /**
     * Inner class that handles Document modifications.
     */
    public static class Modifier {
        private String content;
        private String name;
        private DocumentId documentId;
        private Module oldModule;

        private Modifier(Document oldDocument) {
            this.documentId = oldDocument.documentId();
            this.name = oldDocument.name();
            this.content = oldDocument.textDocument().toString();
            this.oldModule = oldDocument.module();
        }

        /**
         * Sets the content to be changed.
         *
         * @param content content to change with
         * @return Document.Modifier that holds the content to be changed
         */
        public Modifier withContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * Returns a new document with updated content.
         *
         * @return document with updated content
         */
        public Document apply() {
            DocumentConfig documentConfig = DocumentConfig.from(this.documentId, this.content,
                    this.name);
            DocumentContext documentContext = DocumentContext.from(documentConfig, false);
            Module newModule = oldModule.modify().updateDocument(documentContext).apply();
            return newModule.document(this.documentId);
        }
    }
}
