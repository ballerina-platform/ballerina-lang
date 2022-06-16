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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final Map<DocumentId, Resource> resources;
    private final Map<DocumentId, Resource> testResources;
    private final Function<DocumentId, Document> populateDocumentFunc;
    private final Function<DocumentId, Resource> populateResourceFunc;

    private Optional<ModuleMd> moduleMd = null;

    Module(ModuleContext moduleContext, Package packageInstance) {
        this.moduleContext = moduleContext;
        this.packageInstance = packageInstance;

        this.srcDocs = new ConcurrentHashMap<>();
        this.testSrcDocs = new ConcurrentHashMap<>();
        this.resources = new ConcurrentHashMap<>();
        this.testResources = new ConcurrentHashMap<>();
        this.populateDocumentFunc = documentId -> new Document(
                this.moduleContext.documentContext(documentId), this);
        this.populateResourceFunc = documentId -> new Resource(
                this.moduleContext.resourceContext(documentId), this);
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

    public Collection<DocumentId> testDocumentIds() {
        return this.moduleContext.testSrcDocumentIds();
    }

    public Collection<DocumentId> resourceIds() {
        return this.moduleContext.resourceIds();
    }

    public Collection<DocumentId> testResourceIds() {
        return this.moduleContext.testResourceIds();
    }

    public Document document(DocumentId documentId) {
        // TODO Should we throw an error if the moduleId is not present
        if (documentIds().contains(documentId)) {
            return this.srcDocs.computeIfAbsent(documentId, this.populateDocumentFunc);
        } else {
            return this.testSrcDocs.computeIfAbsent(documentId, this.populateDocumentFunc);
        }
    }

    public Resource resource(DocumentId documentId) {
        // TODO Should we throw an error if the documentId is not present
        if (resourceIds().contains(documentId)) {
            return this.resources.computeIfAbsent(documentId, this.populateResourceFunc);
        } else {
            return this.testResources.computeIfAbsent(documentId, this.populateResourceFunc);
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

    public Optional<ModuleMd> moduleMd() {
        if (null == this.moduleMd) {
            this.moduleMd = this.moduleContext.moduleMdContext().map(c ->
                    ModuleMd.from(c, this)
            );
        }
        return this.moduleMd;
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
        private MdDocumentContext moduleMdContext;
        private final Map<DocumentId, ResourceContext> resourceContextMap;
        private final Map<DocumentId, ResourceContext> testResourceContextMap;


        private Modifier(Module oldModule) {
            moduleId = oldModule.moduleId();
            moduleDescriptor = oldModule.descriptor();
            srcDocContextMap = copySrcDocs(oldModule, oldModule.moduleContext.srcDocumentIds());
            testDocContextMap = copySrcDocs(oldModule, oldModule.moduleContext.testSrcDocumentIds());;
            isDefaultModule = oldModule.isDefaultModule();
            dependencies = oldModule.moduleContext().moduleDescDependencies();
            packageInstance = oldModule.packageInstance;
            project = oldModule.project();
            moduleMdContext = oldModule.moduleContext.moduleMdContext().orElse(null);
            resourceContextMap = copyResources(oldModule, oldModule.moduleContext.resourceIds());
            testResourceContextMap = copyResources(oldModule, oldModule.moduleContext.testResourceIds());
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
         * Creates a copy of the existing module and adds a new resource to the new module.
         *
         * @param resourceConfig configurations to create the resource
         * @return an instance of the Module.Modifier
         */
        public Modifier addResource(ResourceConfig resourceConfig) {
            ResourceContext newResourceContext = ResourceContext.from(resourceConfig);
            this.resourceContextMap.put(newResourceContext.documentId(), newResourceContext);
            return this;
        }

        /**
         * Creates a copy of the existing module and adds a new test resource to the new module.
         *
         * @param resourceConfig configurations to create the test resource
         * @return an instance of the Module.Modifier
         */
        public Modifier addTestResource(ResourceConfig resourceConfig) {
            ResourceContext newResourceContext = ResourceContext.from(resourceConfig);
            this.testResourceContextMap.put(newResourceContext.documentId(), newResourceContext);
            return this;
        }

        /**
         * Creates a copy of the existing module and removes the specified resource from the new module.
         *
         * @param documentId documentId of the resource to remove
         * @return an instance of the Module.Modifier
         */
        public Modifier removeResource(DocumentId documentId) {

            if (this.resourceContextMap.containsKey(documentId)) {
                this.resourceContextMap.remove(documentId);
            } else {
                this.testResourceContextMap.remove(documentId);
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
            this.srcDocContextMap = sortDocuments(this.srcDocContextMap);
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
            this.testDocContextMap = sortDocuments(this.testDocContextMap);
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
         * Creates a copy of the existing module and removes the Module.md from the new module.
         *
         * @return an instance of the Module.Modifier
         */
        public Modifier removeModuleMd() {
            moduleMdContext = null;
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

        private Map<DocumentId, ResourceContext> copyResources(Module oldModule, Collection<DocumentId> documentIds) {
            Map<DocumentId, ResourceContext> resourceContextMap = new HashMap<>();
            for (DocumentId documentId : documentIds) {
                resourceContextMap.put(documentId, oldModule.moduleContext.resourceContext(documentId));
            }
            return resourceContextMap;
        }


        private Map<DocumentId, DocumentContext> copySrcDocs(Module oldModule, Collection<DocumentId> documentIds) {
            Map<DocumentId, DocumentContext> srcDocContextMap = new LinkedHashMap<>();
            for (DocumentId documentId : documentIds) {
                srcDocContextMap.put(documentId, oldModule.moduleContext.documentContext(documentId));
            }
            return srcDocContextMap;
        }

        private Module createNewModule(Map<DocumentId, DocumentContext> srcDocContextMap, Map<DocumentId,
                DocumentContext> testDocContextMap) {
            Set<ModuleContext> moduleContextSet = new HashSet<>();
            ModuleContext newModuleContext = new ModuleContext(this.project,
                    this.moduleId, this.moduleDescriptor, this.isDefaultModule, srcDocContextMap,
                    testDocContextMap, this.moduleMdContext, this.dependencies, this.resourceContextMap,
                    this.testResourceContextMap);
            moduleContextSet.add(newModuleContext);

            // add dependant modules including transitives
            Collection<ModuleDescriptor> dependants = getAllDependants(this.moduleDescriptor);
            for (ModuleDescriptor dependentDescriptor : dependants) {
                if (dependentDescriptor.equals(this.moduleDescriptor)) {
                    continue;
                }
                Modifier module = this.packageInstance.module(dependentDescriptor.name()).modify();
                moduleContextSet.add(new ModuleContext(this.project,
                        module.moduleId, dependentDescriptor, module.isDefaultModule, module.srcDocContextMap,
                        module.testDocContextMap, module.moduleMdContext, module.dependencies, this.resourceContextMap,
                        this.testResourceContextMap));
            }

            Package newPackage = this.packageInstance.modify().updateModules(moduleContextSet).apply();
            return newPackage.module(this.moduleId);
        }

        Modifier updateModuleMd(MdDocumentContext moduleMd) {
            this.moduleMdContext = moduleMd;
            return this;
        }

        private Collection<ModuleDescriptor> getAllDependants(ModuleDescriptor updatedModuleDescriptor) {
            packageInstance.getResolution(); // this will build the dependency graph if it is not built yet
            return getAllDependants(updatedModuleDescriptor, new HashSet<>(), new HashSet<>());
        }

        private Collection<ModuleDescriptor> getAllDependants(
                ModuleDescriptor updatedModuleDescriptor,
                HashSet<ModuleDescriptor> visited,
                HashSet<ModuleDescriptor> dependants) {
            if (!visited.contains(updatedModuleDescriptor)) {
                visited.add(updatedModuleDescriptor);
                Collection<ModuleDescriptor> directDependents = this.project.currentPackage()
                        .moduleDependencyGraph().getDirectDependents(updatedModuleDescriptor);
                if (directDependents.size() > 0) {
                    dependants.addAll(directDependents);
                    for (ModuleDescriptor directDependent : directDependents) {
                        getAllDependants(directDependent, visited, dependants);
                    }

                }
            }

            return dependants;
        }

        private Map<DocumentId, DocumentContext> sortDocuments(Map<DocumentId, DocumentContext> docContextMap) {
            return docContextMap.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(entry -> entry.getValue().name()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));
        }
    }
}
