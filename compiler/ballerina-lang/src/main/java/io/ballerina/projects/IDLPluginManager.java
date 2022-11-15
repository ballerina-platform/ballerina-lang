/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.internal.IDLClients;
import io.ballerina.projects.internal.plugins.CompilerPlugins;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.PackageID;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class IDLPluginManager {
    private List<IDLPluginContextImpl> idlPluginContexts;
    private final List<ModuleConfig> moduleConfigs;
    private final Path target;
    private final List<IDLClientEntry> cachedClientEntries;
    private final Set<String> cachedModuleNames;
    private final Map<String, Integer> aliasNameCounter;

    private IDLPluginManager(Path target, List<IDLClientEntry> cachedPlugins) {
        this.target = target;
        this.moduleConfigs = new ArrayList<>();
        this.cachedClientEntries = cachedPlugins;
        this.cachedModuleNames = new HashSet<>();
        this.aliasNameCounter = new HashMap<>();
    }

    static IDLPluginManager from(Path sourceRoot) {
        List<IDLClientEntry> cache = new ArrayList<>();
        Path idlCacheJson = sourceRoot.resolve(ProjectConstants.GENERATED_MODULES_ROOT)
                .resolve(ProjectConstants.IDL_CACHE_FILE);
        if (Files.exists(idlCacheJson)) {
            try {
                String readString = Files.readString(idlCacheJson);
                Type cacheMapType = new TypeToken<List<IDLClientEntry>>() {
                }.getType();
                cache = new Gson().fromJson(readString, cacheMapType);
            } catch (IOException e) {
                // ignore e
            }
        }
        return new IDLPluginManager(sourceRoot, cache);

    }

    private static List<IDLPluginContextImpl> initializePlugins(List<IDLGeneratorPlugin> builtInIDLPlugins) {
        List<IDLPluginContextImpl> compilerPluginContexts = new ArrayList<>(builtInIDLPlugins.size());
        for (IDLGeneratorPlugin plugin : builtInIDLPlugins) {
            IDLPluginContextImpl idlPluginContextImpl = new IDLPluginContextImpl();
            plugin.init(idlPluginContextImpl);
            compilerPluginContexts.add(idlPluginContextImpl);
        }
        return compilerPluginContexts;
    }

    public List<IDLPluginContextImpl> idlPluginContexts() {
        if (this.idlPluginContexts == null) {
            idlPluginContexts = initializePlugins(CompilerPlugins.getBuiltInIDLPlugins());
        }
        return idlPluginContexts;
    }

    public List<ModuleConfig> generatedModuleConfigs() {
        return moduleConfigs;
    }

    public Path target() {
        return target;
    }

    public List<IDLClientEntry> cachedClientEntries() {
        return cachedClientEntries;
    }

    public Set<String> cachedModuleNames() {
        return cachedModuleNames;
    }

    public void addModuleToLoadFromCache(String generatedModuleName) {
        this.cachedModuleNames.add(generatedModuleName);
    }

    public Map<String, Integer> aliasNameCounter() {
        return aliasNameCounter;
    }

    public static class IDLSourceGeneratorContextImpl implements IDLSourceGeneratorContext {
        private final PackageID sourcePkgId;
        private final String sourceDoc;
        private final Package currentPackage;
        private final IDLClients idlClients;
        private final Node clientNode;
        private final Set<ModuleLoadRequest> moduleLoadRequests;
        private final List<ModuleConfig> moduleConfigs;
        private final List<Diagnostic> diagnostics = new ArrayList<>();
        private final Path resourcePath;
        private final List<IDLClientEntry> cachedClientEntries;
        private final Map<String, Integer> aliasNameCounter;

        public IDLSourceGeneratorContextImpl(Node clientNode, PackageID sourcePkgId, String sourceDoc,
                                             Package currentPackage, Path resourcePath,
                                             IDLClients idlClients,
                                             Set<ModuleLoadRequest> moduleLoadRequests,
                                             List<ModuleConfig> moduleConfigs,
                                             List<IDLClientEntry> cachedPlugins,
                                             Map<String, Integer> aliasNameCounter) {
            this.sourcePkgId = sourcePkgId;
            this.sourceDoc = sourceDoc;
            this.currentPackage = currentPackage;
            this.resourcePath = resourcePath;
            this.idlClients = idlClients;
            this.clientNode = clientNode;
            this.moduleLoadRequests = moduleLoadRequests;
            this.moduleConfigs = moduleConfigs;
            this.cachedClientEntries = cachedPlugins;
            this.aliasNameCounter = aliasNameCounter;
        }

        @Override
        public Node clientNode() {
            return clientNode;
        }

        @Override
        public Package currentPackage() {
            return currentPackage;
        }

        @Override
        public Path resourcePath() {
            if (resourcePath.isAbsolute()) {
                return resourcePath;
            } else {
                return currentPackage.project().sourceRoot().resolve(resourcePath);
            }
        }

        @Override
        public void reportDiagnostic(Diagnostic diagnostic) {
            diagnostics.add(diagnostic);
        }

        Collection<Diagnostic> reportedDiagnostics() {
            return diagnostics;
        }

        @Override
        public void addClient(ModuleConfig moduleConfig, NodeList<AnnotationNode> supportedAnnotations) {
            ModuleConfig newModuleConfig = createModuleConfigWithRandomName(moduleConfig);
            LineRange lineRange;
            if (this.clientNode.kind().equals(SyntaxKind.MODULE_CLIENT_DECLARATION)) {
                ModuleClientDeclarationNode moduleClientNode = (ModuleClientDeclarationNode) this.clientNode;
                lineRange = moduleClientNode.clientPrefix().location().lineRange();
            } else {
                ClientDeclarationNode clientDeclarationNode = (ClientDeclarationNode) this.clientNode;
                lineRange = clientDeclarationNode.clientPrefix().location().lineRange();
            }
            idlClients.addEntry(sourcePkgId, sourceDoc, lineRange,
                    newModuleConfig.moduleDescriptor().moduleCompilationId());
            this.moduleLoadRequests.add(new ModuleLoadRequest(
                    PackageOrg.from(newModuleConfig.moduleDescriptor().moduleCompilationId().orgName.getValue()),
                    newModuleConfig.moduleDescriptor().moduleCompilationId().name.getValue(),
                    PackageDependencyScope.DEFAULT,
                    DependencyResolutionType.SOURCE));
            this.moduleConfigs.add(newModuleConfig);

            // Generate id to cache plugins for subsequent compilations
            List<String> annotations = CompilerPlugins.annotationsAsStr(supportedAnnotations);
            String uri = getUri(this.clientNode);

            IDLClientEntry idlCacheInfo = new IDLClientEntry(uri, resourcePath, annotations,
                    newModuleConfig.moduleDescriptor().name().moduleNamePart(),
                    this.currentPackage.project().sourceRoot().resolve(resourcePath).toFile().lastModified());
            this.cachedClientEntries.add(idlCacheInfo);
        }

        private String getUri(Node clientNode) {
            BasicLiteralNode clientUri;

            if (clientNode.kind() == SyntaxKind.MODULE_CLIENT_DECLARATION) {
                clientUri = ((ModuleClientDeclarationNode) clientNode).clientUri();
            } else {
                clientUri = ((ClientDeclarationNode) clientNode).clientUri();
            }

            String text = clientUri.literalToken().text();
            return text.substring(1, text.length() - 1);
        }

        private ModuleConfig createModuleConfigWithRandomName(ModuleConfig moduleConfig) {
            ModuleName randomModuleName;
            String alias = moduleConfig.moduleDescriptor().name().moduleNamePart();
            String moduleNameStr = alias;
            if (aliasNameCounter.containsKey(alias)) {
                moduleNameStr += aliasNameCounter.get(alias);
                aliasNameCounter.put(alias, aliasNameCounter.get(alias) + 1);
            } else {
                aliasNameCounter.put(alias, 1);
            }
            if (moduleConfig.moduleDescriptor().name() == null) {
                // Module name is mandatory since this will be added as a non-default module
                throw new ProjectException("module name cannot be null");
            } else {
                randomModuleName = ModuleName.from(moduleConfig.moduleDescriptor().packageName(), moduleNameStr);
            }
            ModuleDescriptor newModuleDescriptor = ModuleDescriptor.from(randomModuleName,
                    this.currentPackage.descriptor());
            return ModuleConfig.from(
                    moduleConfig.moduleId(), newModuleDescriptor, moduleConfig.sourceDocs(),
                    moduleConfig.testSourceDocs(), moduleConfig.moduleMd().orElse(null), moduleConfig.dependencies(),
                    ModuleKind.COMPILER_GENERATED);
        }
    }
}
