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

import java.util.function.Supplier;

/**
 * {@code DocumentConfig} contains necessary configuration elements required to
 * create an instance of a {@code Document}.
 *
 * @since 2.0.0
 */
public abstract class DocumentConfig {
    // This class should contain project-agnostic information
    private final DocumentId documentId;
    private final String name;

    private DocumentConfig(DocumentId documentId, String name) {
        this.documentId = documentId;
        this.name = name;
    }

    public static DocumentConfig from(DocumentId documentId, String content, String name) {
        return new EagerDocumentConfig(documentId, content, name);
    }

    public static DocumentConfig from(DocumentId documentId, Supplier<String> content, String name) {
        return new LazyDocumentConfig(documentId, content, name);
    }

    public DocumentId documentId() {
        return documentId;
    }

    public abstract String content();

    public String name() {
        return name;
    }

    private static class EagerDocumentConfig extends DocumentConfig {

        private final String content;

        private EagerDocumentConfig(DocumentId documentId, String content, String name) {
            super(documentId, name);
            this.content = content;
        }

        @Override
        public String content() {
            return content;
        }
    }

    private static class LazyDocumentConfig extends DocumentConfig {

        private final Supplier<String> contentSupplier;

        private LazyDocumentConfig(DocumentId documentId, Supplier<String> contentSupplier, String name) {
            super(documentId, name);
            this.contentSupplier = contentSupplier;
        }

        @Override
        public String content() {
            String s = contentSupplier.get();
            assert s != null : "LazyDocumentConfig content supplier should not return null";
            return s;
        }
    }
}
