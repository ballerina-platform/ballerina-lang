/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.function.Supplier;

import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

/**
 * MD Document Context used in ProjectAPI Inner Tree.
 *
 * @since 2.0.0
 */
class MdDocumentContext {
    private TextDocument textDocument;
    private final DocumentId documentId;
    private final String name;
    private final DocumentConfig documentConfig;

    private MdDocumentContext(DocumentId documentId, String name, DocumentConfig config) {
        this.documentId = documentId;
        this.name = name;
        this.documentConfig = config;
    }

    static MdDocumentContext from(DocumentConfig documentConfig) {
        return new MdDocumentContext(documentConfig.documentId(), documentConfig.name(), documentConfig);
    }

    DocumentId documentId() {
        return this.documentId;
    }

    String name() {
        return this.name;
    }

    public TextDocument textDocument() {
        if (this.textDocument == null) {
            this.textDocument = TextDocuments.from(this.documentConfig::content);
        }
        return this.textDocument;
    }

    String content() {
        return this.documentConfig.content();
    }
}
