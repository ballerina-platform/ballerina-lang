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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.configurations.ConfigModuleDetails;
import io.ballerina.projects.configurations.ConfigVariable;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util class to read configurable variables defined in a package.
 *
 * @since 2.0.0
 */
public class ConfigReader {

    /**
     * Retrieve configurable variables in a package as a per module map.
     *
     * @param packageInstance to read configurable variables
     * @return A map with ConfigModuleDetails(module details) as key and
     * List of ConfigVariable (configurable variables in the module) as value
     */
    public static Map<ConfigModuleDetails, List<ConfigVariable>> getConfigVariables(Package packageInstance) {
        Map<ConfigModuleDetails, List<ConfigVariable>> configDetails = new HashMap<>();
        Set<BVarSymbol> validConfigs = getValidConfigs(packageInstance.getDefaultModule().moduleContext().
                bLangPackage().symbol);
        for (Module module : packageInstance.modules()) {
            getConfigs(module, module.moduleContext().bLangPackage(), configDetails, validConfigs);
        }
        return configDetails;
    }

    /**
     * Update provided map with the configurable variable details for the given module.
     *
     * @param module Module to retrieve module details
     * @param bLangPackage to retrieve configurable variable details
     * @param configDetails Map to store the configurable variables against module
     */
    private static void getConfigs(Module module,
                                   BLangPackage bLangPackage, Map<ConfigModuleDetails,
            List<ConfigVariable>> configDetails, Set<BVarSymbol> validConfigs) {
        List<ConfigVariable> configVariables = new ArrayList<>();
        PackageID currentPkgId = bLangPackage.symbol.pkgID;
        for (Scope.ScopeEntry entry : bLangPackage.symbol.scope.entries.values()) {
            BSymbol symbol = entry.symbol;
            // Filter configurable variables
            if (symbol != null && symbol.tag == SymTag.VARIABLE && Symbols.isFlagOn(symbol.flags,
                    Flags.CONFIGURABLE)) {
                if (symbol instanceof BVarSymbol) {
                    BVarSymbol varSymbol = (BVarSymbol) symbol;
                    if (validConfigs.contains(varSymbol)) {
                        // Get description
                        String description = getDescription(varSymbol, module);
                        if (description.startsWith("\"") && description.endsWith("\"")) {
                            description = description.substring(1, description.length() - 1);
                        }
                        configVariables.add(new ConfigVariable(varSymbol.name.value.replace("\\", ""), varSymbol.type,
                                Symbols.isFlagOn(varSymbol.flags, Flags.REQUIRED), description));
                    }
                }
            }
        }
        if (!configVariables.isEmpty()) {
            // Add configurable variable details for the current package
            configDetails.put(getConfigModuleDetails(module.moduleName(), currentPkgId,
                    module.project().kind()), configVariables);
        }
    }

    private static Set<BVarSymbol> getValidConfigs(BPackageSymbol packageSymbol) {
        Set<BVarSymbol> configVars = new HashSet<>();
        populateConfigVars(packageSymbol, configVars);
        if (!packageSymbol.imports.isEmpty()) {
            for (BPackageSymbol importSymbol : packageSymbol.imports) {
                populateConfigVars(importSymbol, configVars);
            }
        }
        return configVars;
    }

    private static void populateConfigVars(BPackageSymbol pkgSymbol, Set<BVarSymbol> configVars) {
        for (Scope.ScopeEntry entry : pkgSymbol.scope.entries.values()) {
            BSymbol symbol = entry.symbol;
            if (symbol != null) {
                if (symbol.tag == SymTag.TYPE_DEF) {
                    symbol = symbol.type.tsymbol;
                }
                if (symbol != null && symbol.tag == SymTag.VARIABLE
                        && Symbols.isFlagOn(symbol.flags, Flags.CONFIGURABLE) && symbol instanceof BVarSymbol) {
                    configVars.add((BVarSymbol) symbol);
                }
            }
        }
    }

    /**
     * Get module details to store the configurable variables against.
     *
     * @param moduleName to retrieve module details
     * @param packageID to retrieve package details
     * @param projectKind to retrieve information about the type of project
     * @return module details stored in object ConfigModuleDetails
     */
    private static ConfigModuleDetails getConfigModuleDetails(ModuleName moduleName, PackageID packageID,
                                                              ProjectKind projectKind) {
        String orgName = packageID.getOrgName().getValue();
        String packageName = packageID.getPkgName().getValue();
        String moduleNameVal = moduleName.isDefaultModuleName() ? moduleName.toString() : moduleName.moduleNamePart();
        return new ConfigModuleDetails(orgName, packageName, moduleNameVal, projectKind);
    }

    /**
     * Get configurable variable description as String.
     *
     * @param symbol to get position details
     * @param module to retrieve module details
     * @return configurable variable description
     */
    private static String getDescription(BVarSymbol symbol, Module module) {
        Map<Document, SyntaxTree> syntaxTreeMap = getSyntaxTreeMap(module);
        Node variableNode = getVariableNode(symbol.getPosition().lineRange().startLine().line(), syntaxTreeMap);
        if (variableNode != null) {
            Optional<MetadataNode> optionalMetadataNode =
                    ((ModuleVariableDeclarationNode) variableNode).metadata();
            if (optionalMetadataNode.isPresent()) {
                NodeList<AnnotationNode> annotations = optionalMetadataNode.get().annotations();
                for (AnnotationNode annotation : annotations) {
                    Node annotReference = annotation.annotReference();
                    if (annotReference.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                        SimpleNameReferenceNode simpleNameRef = (SimpleNameReferenceNode) annotReference;
                        if (simpleNameRef.name().text().equals("display") && annotation.annotValue().isPresent()) {
                            for (MappingFieldNode fieldNode : annotation.annotValue().get().fields()) {
                                if (fieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                                    SpecificFieldNode specificField = (SpecificFieldNode) fieldNode;
                                    if (specificField.fieldName().kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                                        if (((IdentifierToken) specificField.fieldName()).text().
                                                equals("description")) {
                                            if (((SpecificFieldNode) fieldNode).valueExpr().isPresent()) {
                                                ExpressionNode valueNode =
                                                        ((SpecificFieldNode) fieldNode).valueExpr().get();
                                                if (valueNode instanceof BasicLiteralNode) {
                                                    return ((BasicLiteralNode) valueNode).literalToken().text();
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    /**
     * Get Syntax tree node for the configurable variable in given position.
     *
     * @param position position of configurable BVarSymbol
     * @param syntaxTreeMap Syntax tree map for the specific module
     * @return Relevant syntax tree node for the variable
     */
    private static Node getVariableNode(int position, Map<Document, SyntaxTree> syntaxTreeMap) {
        for (Map.Entry<Document, SyntaxTree> syntaxTreeEntry : syntaxTreeMap.entrySet()) {
            if (syntaxTreeEntry.getValue().containsModulePart()) {
                ModulePartNode modulePartNode = syntaxTreeMap.get(syntaxTreeEntry.getKey()).rootNode();
                List<Node> filteredVarNodes = modulePartNode.members().stream()
                        .filter(node -> node.kind() == SyntaxKind.MODULE_VAR_DECL &&
                                node instanceof ModuleVariableDeclarationNode)
                        .collect(Collectors.toList());
                for (Node node : filteredVarNodes) {
                    if (node.location().lineRange().startLine().line() <= position &&
                            node.location().lineRange().endLine().line() >= position) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get syntax tree map for given module.
     *
     * @param module to get module details
     * @return map of SyntaxTree against Document in module
     */
    private static Map<Document, SyntaxTree> getSyntaxTreeMap(Module module) {
        Map<Document, SyntaxTree> syntaxTreeMap = new HashMap<>();
        module.documentIds().forEach(documentId -> {
            Document document = module.document(documentId);
            syntaxTreeMap.put(document, document.syntaxTree());
        });
        return syntaxTreeMap;
    }

}
