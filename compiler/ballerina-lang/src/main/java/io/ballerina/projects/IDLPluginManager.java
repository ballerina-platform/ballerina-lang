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

import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.internal.plugins.CompilerPlugins;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.PackageID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class IDLPluginManager {
    private final List<IDLPluginContextImpl> idlPluginContexts;
    private final List<ModuleConfig> moduleConfigs;

    private IDLPluginManager(List<IDLPluginContextImpl> compilerPluginContexts) {
        this.idlPluginContexts = compilerPluginContexts;
        this.moduleConfigs = new ArrayList<>();
    }

    static IDLPluginManager initPlugins() {
        List<IDLPluginContextImpl> compilerPluginContexts = initializePlugins(CompilerPlugins.getBuiltInIDLPlugins());
        return new IDLPluginManager(compilerPluginContexts);
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
        return idlPluginContexts;
    }

    public List<ModuleConfig> generatedModuleConfigs() {
        return moduleConfigs;
    }

    public static class IDLSourceGeneratorContextImpl implements IDLSourceGeneratorContext {
        private final Package currentPackage;
        private final Map<LineRange, PackageID> idlClientMap;
        private final Node clientNode;
        private final List<ModuleConfig> moduleConfigs;
        private final List<Diagnostic> diagnostics = new ArrayList<>();

        public IDLSourceGeneratorContextImpl(Node clientNode, Package currentPackage,
                                             Map<LineRange, PackageID> idlClientMap, List<ModuleConfig> moduleConfigs) {
            this.currentPackage = currentPackage;
            this.idlClientMap = idlClientMap;
            this.clientNode = clientNode;
            this.moduleConfigs = moduleConfigs;
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
        public void reportDiagnostic(Diagnostic diagnostic) {
            diagnostics.add(diagnostic);
        }

        // TODO: add diagnostics to the package
        Collection<Diagnostic> reportedDiagnostics() {
            return diagnostics;
        }

        @Override
        public void addClient(ModuleConfig moduleConfig) {
            ModuleConfig newModuleConfig = createModuleConfigWithRandomName(moduleConfig);
            LineRange lineRange;
            if (this.clientNode.kind().equals(SyntaxKind.MODULE_CLIENT_DECLARATION)) {
                ModuleClientDeclarationNode moduleClientNode = (ModuleClientDeclarationNode) this.clientNode;
                lineRange = moduleClientNode.clientPrefix().location().lineRange();
            } else {
                ClientDeclarationNode clientDeclarationNode = (ClientDeclarationNode) this.clientNode;
                lineRange = clientDeclarationNode.clientPrefix().location().lineRange();
            }
            this.idlClientMap.put(lineRange, newModuleConfig.moduleDescriptor().moduleCompilationId());
            this.moduleConfigs.add(newModuleConfig);
        }

        private ModuleConfig createModuleConfigWithRandomName(ModuleConfig moduleConfig) {
            ModuleName randomModuleName;
            if (moduleConfig.moduleDescriptor().name() == null) {
                randomModuleName = ModuleName.from(moduleConfig.moduleDescriptor().packageName(),
                        String.valueOf(System.currentTimeMillis()));
            } else {
                randomModuleName = ModuleName.from(moduleConfig.moduleDescriptor().packageName(),
                        moduleConfig.moduleDescriptor().name() + String.valueOf(System.currentTimeMillis()));
            }
            ModuleDescriptor newModuleDescriptor = ModuleDescriptor.from(randomModuleName,
                    this.currentPackage.descriptor());
            return ModuleConfig.from(
                    moduleConfig.moduleId(), newModuleDescriptor, moduleConfig.sourceDocs(),
                    moduleConfig.testSourceDocs(), moduleConfig.moduleMd().orElse(null), moduleConfig.dependencies());
        }
    }
}
