/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.common;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Util used to find configurable variables defined in a module.
 *
 * @since 2201.10.0
 */
public final class ConfigurableFinder {

    private ConfigurableFinder() {
    }

    /**
     * Get all configurables in the current selected module.
     *
     * @param context Current code action context
     * @return all the configurable variables and module details
     */
    public static ModuleConfigDetails getModuleConfigDetails(CodeActionContext context) {
        SemanticModel semanticModel = context.currentSemanticModel().get();
        Module module = context.currentModule().get();
        Map<String, ConfigVariable> configVariables = new HashMap<>();
        int index = 0;
        for (DocumentId documentId : module.documentIds()) {
            Document document = module.document(documentId);
            ModulePartNode modulePartNode = document.syntaxTree().rootNode();
            for (NonTerminalNode member: modulePartNode.members()) {
                if (member.kind() != SyntaxKind.MODULE_VAR_DECL) {
                    continue;
                }
                ModuleVariableDeclarationNode moduleVar = (ModuleVariableDeclarationNode) member;
                if (hasConfigurableQualifier(moduleVar)) {
                    Optional<Symbol> symbol = semanticModel.symbol(moduleVar);
                    if (symbol.isEmpty()) {
                        continue;
                    }
                    VariableSymbol variableSymbol = (VariableSymbol) symbol.get();
                    TypeSymbol type = CommonUtil.getRawType(variableSymbol.typeDescriptor());

                    boolean isRequiredVariable = moduleVar.initializer().stream()
                            .anyMatch(var -> var.kind() == SyntaxKind.REQUIRED_EXPRESSION);
                    String varName = variableSymbol.getName().get();
                    ConfigVariable configVariable = new ConfigVariable(varName, type, isRequiredVariable, index++);
                    configVariables.put(varName, configVariable);
                }
            }
        }
        String moduleName = module.isDefaultModule() ? "" : module.moduleName().moduleNamePart();
        return new ModuleConfigDetails(moduleName, module.isDefaultModule(), configVariables);
    }

    private static boolean hasConfigurableQualifier(ModuleVariableDeclarationNode node) {
        return node.qualifiers().stream().anyMatch(q -> q.text().equals(Qualifier.CONFIGURABLE.getValue()));
    }

    /**
     * Holder to store information about a configurable variable.
     *
     * @param name name of the configurable
     * @param type type symbol of the configurable
     * @param isRequired value is required
     * @param index order of the configurable
     */
    public record ConfigVariable(String name, TypeSymbol type, boolean isRequired, int index)
            implements Comparable<ConfigVariable> {

        @Override
        public int compareTo(ConfigurableFinder.ConfigVariable o) {
            return this.index() - o.index();
        }
    }

    /**
     * Holder to store information about module and configurable variables in the module.
     *
     * @param moduleName name of the module
     * @param isDefaultModule module is default or not
     * @param configVariables configurable variables in the module
     */
    public record ModuleConfigDetails(String moduleName, boolean isDefaultModule,
                                      Map<String, ConfigVariable> configVariables) {
    }
}
