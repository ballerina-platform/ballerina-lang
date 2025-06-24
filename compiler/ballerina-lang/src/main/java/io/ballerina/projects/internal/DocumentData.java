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
package io.ballerina.projects.internal;

import java.util.function.Supplier;

/**
 * {@code ModuleFileData} represents a Ballerina source file (.bal).
 *
 * @since 2.0.0
 */
public abstract class DocumentData {
    //TODO: Remove this class and use DocumentConfig for creating a document
    private final String name;

    private DocumentData(String name) {
        this.name = name;
    }

    public static DocumentData from(String name, String content) {
        return new DocumentData.EagerDocumentData(name, content);
    }

    public static DocumentData from(String name, Supplier<String> content) {
        return new DocumentData.LazyDocumentData(name, content);
    }

    public abstract String content();

    public String name() {
        return name;
    }

    private static class EagerDocumentData extends DocumentData {

        private final String content;

        private EagerDocumentData(String name, String content) {
            super(name);
            this.content = content;
        }

        @Override
        public String content() {
            return content;
        }
    }

    private static class LazyDocumentData extends DocumentData {

        private final Supplier<String> contentSupplier;

        public LazyDocumentData(String name, Supplier<String> content) {
            super(name);
            this.contentSupplier = content;
        }

        @Override
        public String content() {
            return contentSupplier.get();
        }
    }
}
