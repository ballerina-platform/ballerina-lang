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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@code Module} represents a Ballerina module.
 *
 * @since 2.0.0
 */
public class Module {
    private final ModuleContext moduleContext;
    private final Package packageInstance;
    private final Map<DocumentId, Document> srcDocs;
    private final Map<DocumentId, Document> testSrcDocs;
    private final Function<DocumentId, Document> populateDocumentFunc;

    Module(ModuleContext moduleContext, Package packageInstance) {
        this.moduleContext = moduleContext;
        this.packageInstance = packageInstance;
        this.srcDocs = new ConcurrentHashMap<>();
        this.testSrcDocs = new ConcurrentHashMap<>();
        this.populateDocumentFunc = documentId -> Document.from(
                this.moduleContext.documentContext(documentId), this);
    }

    static Module from(ModuleContext moduleContext, Package packageInstance) {
        return new Module(moduleContext, packageInstance);
    }

    public Package packageInstance() {
        return this.packageInstance;
    }

    public ModuleId moduleId() {
        return this.moduleContext.moduleId();
    }

    public Collection<DocumentId> documentIds() {
        return this.moduleContext.srcDocumentIds();
    }

    public Iterable<Document> documents() {
        return null;
    }

    public Collection<DocumentId> testDocumentIds() {
        return this.moduleContext.testSrcDocumentIds();
    }

    public Iterable<Document> testDocuments() {
        return null;
    }

    public Document document(DocumentId documentId) {
        // TODO Should we throw an error if the moduleId is not present
        if (documentIds().contains(documentId)) {
            return this.srcDocs.computeIfAbsent(documentId, this.populateDocumentFunc);
        } else {
            return this.testSrcDocs.computeIfAbsent(documentId, this.populateDocumentFunc);
        }
    }
}
