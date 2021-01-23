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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
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
        this.populateDocumentFunc = documentId -> new Document(
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

    public ModuleName moduleName() {
        return this.moduleContext.moduleName();
    }

    public ModuleDescriptor descriptor() {
        return moduleContext.descriptor();
    }

    public Collection<DocumentId> documentIds() {
        return this.moduleContext.srcDocumentIds();
    }

    public Iterable<Document> documents() {
        return new DocumentIterable(this.srcDocs.values());
    }

    public Collection<DocumentId> testDocumentIds() {
        return this.moduleContext.testSrcDocumentIds();
    }

    public Iterable<Document> testDocuments() {
        return new DocumentIterable(this.testSrcDocs.values());
    }

    public Document document(DocumentId documentId) {
        // TODO Should we throw an error if the moduleId is not present
        if (documentIds().contains(documentId)) {
            return this.srcDocs.computeIfAbsent(documentId, this.populateDocumentFunc);
        } else {
            return this.testSrcDocs.computeIfAbsent(documentId, this.populateDocumentFunc);
        }
    }

    public ModuleCompilation getCompilation() {
        return this.packageInstance.packageContext().getModuleCompilation(this.moduleContext);
    }

    public Collection<ModuleDependency> moduleDependencies() {
        return moduleContext.dependencies();
    }

    public boolean isDefaultModule() {
        return moduleContext.isDefaultModule();
    }

    public Project project() {
        return this.moduleContext.project();
    }

    /** Returns an instance of the Module.Modifier.
     *
     * @return module modifier
     */
    public Modifier modify() {
        return new Modifier(this);
    }

    ModuleContext moduleContext() {
        return moduleContext;
    }

    public Optional<MdDocument> moduleMd() {
        return this.moduleContext.moduleMd();
    }

    private static class DocumentIterable implements Iterable {
        private final Collection<Document> documentList;

        public DocumentIterable(Collection<Document> documentList) {
            this.documentList = documentList;
        }

        @Override
        public Iterator<Document> iterator() {
            return this.documentList.iterator();
        }
    
        @Override
        public Spliterator spliterator() {
            return this.documentList.spliterator();
        }
    }

    /**
     * Inner class that handles module modifications.
     */
    public static class Modifier {
        private ModuleId moduleId;
        private ModuleDescriptor moduleDescriptor;
        private Map<DocumentId, DocumentContext> srcDocContextMap;
        private Map<DocumentId, DocumentContext> testDocContextMap;
        private boolean isDefaultModule;
        private final List<ModuleDescriptor> dependencies;
        private Package packageInstance;
        private Project project;
        private Optional<MdDocument> moduleMd;


        private Modifier(Module oldModule) {
            moduleId = oldModule.moduleId();
            moduleDescriptor = oldModule.descriptor();
            srcDocContextMap = copySrcDocs(oldModule);
            testDocContextMap = copyTestDocs(oldModule);
            isDefaultModule = oldModule.isDefaultModule();
            dependencies = oldModule.moduleContext().moduleDescDependencies();
            packageInstance = oldModule.packageInstance;
            project = oldModule.project();
            moduleMd = oldModule.moduleMd();
        }

        Modifier updateDocument(DocumentContext newDocContext) {
            if (this.srcDocContextMap.containsKey(newDocContext.documentId())) {
                this.srcDocContextMap.put(newDocContext.documentId(), newDocContext);
            } else {
                this.testDocContextMap.put(newDocContext.documentId(), newDocContext);
            }
            return this;
        }

        /**
         * Creates a copy of the existing module and adds a new source document to the new module.
         *
         * @param documentConfig configurations to create the document
         * @return an instance of the Module.Modifier
         */
        public Modifier addDocument(DocumentConfig documentConfig) {
            DocumentContext newDocumentContext = DocumentContext.from(documentConfig);
            this.srcDocContextMap.put(newDocumentContext.documentId(), newDocumentContext);
            return this;
        }

        /**
         * Creates a copy of the existing module and adds a new test document to the new module.
         *
         * @param documentConfig configurations to create the document
         * @return an instance of the Module.Modifier
         */
        public Modifier addTestDocument(DocumentConfig documentConfig) {
            DocumentContext newDocumentContext = DocumentContext.from(documentConfig);
            this.testDocContextMap.put(newDocumentContext.documentId(), newDocumentContext);
            return this;
        }

        /**
         * Creates a copy of the existing module and removes the specified document from the new module.
         *
         * @param documentId documentId of the document to remove
         * @return an instance of the Module.Modifier
         */
        public Modifier removeDocument(DocumentId documentId) {

            if (this.srcDocContextMap.containsKey(documentId)) {
                srcDocContextMap.remove(documentId);
            } else {
                testDocContextMap.remove(documentId);
            }
            return this;
        }

        /**
         * Returns the updated module created by a document add/remove/update operation.
         *
         * @return the updated module
         */
        public Module apply() {
            return createNewModule(this.srcDocContextMap, this.testDocContextMap);
        }

        private Map<DocumentId, DocumentContext> copySrcDocs(Module oldModule) {
            Map<DocumentId, DocumentContext> srcDocContextMap = new HashMap<>();
            for (DocumentId documentId : oldModule.moduleContext.srcDocumentIds()) {
                srcDocContextMap.put(documentId, oldModule.moduleContext.documentContext(documentId));
            }
            return srcDocContextMap;
        }

        private Map<DocumentId, DocumentContext> copyTestDocs(Module oldModule) {
            Map<DocumentId, DocumentContext> testDocContextMap = new HashMap<>();
            for (DocumentId documentId : oldModule.moduleContext.testSrcDocumentIds()) {
                testDocContextMap.put(documentId, oldModule.moduleContext.documentContext(documentId));
            }
            return testDocContextMap;
        }

        private Module createNewModule(Map<DocumentId, DocumentContext> srcDocContextMap, Map<DocumentId,
                DocumentContext> testDocContextMap) {
            ModuleContext newModuleContext = new ModuleContext(this.project,
                    this.moduleId, this.moduleDescriptor, this.isDefaultModule, srcDocContextMap,
                    testDocContextMap, this.moduleMd.orElse(null), this.dependencies);
            Package newPackage = this.packageInstance.modify().updateModule(newModuleContext).apply();
            return newPackage.module(this.moduleId);
        }
    }
}
