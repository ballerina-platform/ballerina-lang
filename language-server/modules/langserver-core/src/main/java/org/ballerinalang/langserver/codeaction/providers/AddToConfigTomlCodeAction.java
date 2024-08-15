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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ConfigurableFinder;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.ConfigTomlValueGenerationUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Code Action to undefined configurables into Config.toml file.
 *
 * @since 2201.10.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddToConfigTomlCodeAction implements RangeBasedCodeActionProvider {

    private static final String CONFIG_TOML = "Config.toml";

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.MODULE_VAR_DECL);
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        return context.currentModule().isPresent()
                && hasConfigurableQualifier((ModuleVariableDeclarationNode) positionDetails.matchedCodeActionNode());
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        if (context.currentModule().isEmpty()) {
            return Collections.emptyList();
        }
        Module module = context.currentModule().get();
        Project project = module.project();
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return Collections.emptyList();
        }
        String orgName = project.currentPackage().packageOrg().value();
        SemanticModel semanticModel = context.currentSemanticModel().get();
        Types types = semanticModel.types();
        TypeBuilder builder = types.builder();
        TypeSymbol basicType = builder.UNION_TYPE.withMemberTypes(types.INT, types.STRING, types.BOOLEAN,
                types.BYTE, types.FLOAT, types.DECIMAL, types.XML).build();
        TypeSymbol anyDataOrJsonType = builder.UNION_TYPE.withMemberTypes(types.ANYDATA, types.JSON).build();

        Optional<Symbol> confSymbol = semanticModel.symbol(posDetails.matchedCodeActionNode());
        if (confSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        ConfigurableFinder.ModuleConfigDetails moduleConfigDetails = ConfigurableFinder.getModuleConfigDetails(context);
        Map<String, ConfigurableFinder.ConfigVariable> configVarMap = moduleConfigDetails.configVariables();
        VariableSymbol variableSymbol = (VariableSymbol) confSymbol.get();
        ConfigurableFinder.ConfigVariable selectedConf = configVarMap.get(variableSymbol.getName().get());

        Path configTomlPath = context.workspace().projectRoot(context.filePath()).resolve(CONFIG_TOML);
        if (!configTomlPath.toFile().exists()) {
            if (configVarMap.isEmpty()) {
                return Collections.emptyList();
            }
            return withoutConfigTomlCodeActions(basicType, anyDataOrJsonType, moduleConfigDetails,
                    selectedConf, configTomlPath);
        }

        Optional<Toml> toml = readToml(configTomlPath);
        if (toml.isEmpty() || configVarMap.isEmpty()) {
            return Collections.emptyList();
        }
        int lastNodeLine = module.isDefaultModule() ? rootModuleConfigDiff(toml.get(),
                orgName, module.moduleName().toString(), configVarMap)
                : nonRootModuleConfigDiff(toml.get(), orgName, module.moduleName(), configVarMap);

        if (!configVarMap.containsKey(selectedConf.name())) {
            return Collections.emptyList();
        }

        return getWithConfigTomlCodeActions(basicType, anyDataOrJsonType, moduleConfigDetails, selectedConf,
                configTomlPath, toml.get(), module, lastNodeLine);
    }

    private List<CodeAction> getWithConfigTomlCodeActions(TypeSymbol basicType, TypeSymbol anyDataOrJsonType,
                                                          ConfigurableFinder.ModuleConfigDetails moduleConfigDetails,
                                                          ConfigurableFinder.ConfigVariable selectedConf,
                                                          Path configTomlPath, Toml toml,
                                                          Module module, int lastNodeLine) {
        boolean hasAtLeastOneEntry = lastNodeLine > 0;
        Position position = getTomlInsertPosition(toml, module, lastNodeLine);
        List<CodeAction> codeActions = new ArrayList<>();
        if (selectedConf.isRequired()) {
            TextEdit textEdit = new TextEdit(new Range(position, position),
                    getEdits(moduleConfigDetails, basicType, anyDataOrJsonType, hasAtLeastOneEntry));
            CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.ADD_ALL_TO_CONFIG_TOML,
                    List.of(textEdit), configTomlPath.toUri().getPath());
            codeActions.add(codeAction);
        }
        TextEdit textEdit = new TextEdit(new Range(position, position),
                getTableEntryEdit(selectedConf, basicType, anyDataOrJsonType));
        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.ADD_TO_CONFIG_TOML,
                List.of(textEdit), configTomlPath.toUri().getPath());
        codeActions.add(codeAction);

        return codeActions;
    }

    private static Position getTomlInsertPosition(Toml toml, Module module, int lastNodeLine) {
        if (lastNodeLine == 0 && !module.isDefaultModule()) {
            Map<String, TopLevelNode> entries = toml.rootNode().entries();
            for (TopLevelNode node : entries.values()) {
                int nodeLine = node.location().lineRange().endLine().line();
                if (nodeLine > lastNodeLine) {
                    lastNodeLine = nodeLine;
                }
            }
            lastNodeLine = lastNodeLine == 0 ? 0 : lastNodeLine + 3;
        } else {
            lastNodeLine = lastNodeLine == 0 ? 0 : lastNodeLine + 1;
        }
        return PositionUtil.toPosition(LinePosition.from(lastNodeLine, 0));
    }

    private static List<CodeAction> withoutConfigTomlCodeActions(TypeSymbol basicType, TypeSymbol anyDataOrJsonType,
                                                          ConfigurableFinder.ModuleConfigDetails moduleConfigDetails,
                                                          ConfigurableFinder.ConfigVariable selectedConf,
                                                          Path configTomlPath) {
        List<CodeAction> codeActions = new ArrayList<>();
        if (selectedConf.isRequired()) {
            String newText = getEdits(moduleConfigDetails, basicType, anyDataOrJsonType);
            CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.ADD_ALL_TO_CONFIG_TOML,
                    new Command(CommandConstants.ADD_TO_CONFIG_TOML, CommandConstants.CREATE_CONFIG_TOML_COMMAND,
                            List.of(configTomlPath.toString(), newText)), CodeActionKind.Empty);
            codeActions.add(codeAction);
        }
        String newText = getTableEntryEdit(selectedConf, basicType, anyDataOrJsonType);
        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.ADD_TO_CONFIG_TOML,
                new Command(CommandConstants.ADD_TO_CONFIG_TOML, CommandConstants.CREATE_CONFIG_TOML_COMMAND,
                        List.of(configTomlPath.toString(), newText)), CodeActionKind.Empty);
        codeActions.add(codeAction);
        return codeActions;
    }

    private static String getEdits(ConfigurableFinder.ModuleConfigDetails moduleConfigDetails, TypeSymbol basicType,
                                   TypeSymbol anydataOrJson) {
        return getEdits(moduleConfigDetails, basicType, anydataOrJson, false);
    }

    private static String getEdits(ConfigurableFinder.ModuleConfigDetails moduleConfigDetails, TypeSymbol basicType,
                                   TypeSymbol anydataOrJson, boolean hasAtLeastOneEntry) {
        StringBuilder basicKeyValueBuilder = new StringBuilder();
        StringBuilder complexValueBuilder = new StringBuilder();
        if (!moduleConfigDetails.isDefaultModule() && !hasAtLeastOneEntry) {
            basicKeyValueBuilder.append(String.format("[%s]%n", moduleConfigDetails.moduleName()));
        }

        moduleConfigDetails.configVariables().values().stream().sorted()
                .forEach(variable -> {
                    if (variable.isRequired()) {
                        ConfigTomlValueGenerationUtil.TomlEntryValue tableEntry =
                                ConfigTomlValueGenerationUtil.getDefaultValueStr(variable.type(),
                                basicType, anydataOrJson, variable.name());
                        if (tableEntry.keyValue()) {
                            basicKeyValueBuilder.append(
                                    String.format("%s = %s%n", variable.name(), tableEntry.value()));
                        } else {
                            complexValueBuilder.append(String.format("%n%s%n", tableEntry.value()));
                        }
                    }
                });

        basicKeyValueBuilder.append(complexValueBuilder);
        return basicKeyValueBuilder.toString();
    }

    private static String getTableEntryEdit(ConfigurableFinder.ConfigVariable variable, TypeSymbol basicType,
                                            TypeSymbol anydataOrJson) {
        ConfigTomlValueGenerationUtil.TomlEntryValue defaultValueStr =
                ConfigTomlValueGenerationUtil.getDefaultValueStr(variable.type(),
                basicType, anydataOrJson, variable.name());
        return defaultValueStr.keyValue() ? String.format("%s = %s%n", variable.name(), defaultValueStr.value())
                : String.format("%n%s%n", defaultValueStr.value());
    }

    private static int rootModuleConfigDiff(Toml baseToml,
                                            String orgName,
                                            String moduleName,
                                            Map<String, ConfigurableFinder.ConfigVariable> configVariables) {
        String orgModuleKey = orgName + "." + moduleName;
        Optional<Toml> table = baseToml.getTable(orgModuleKey);
        table.ifPresent(toml -> configDiff(configVariables, toml.rootNode())); // [org.default-module-name]
        table = baseToml.getTable(moduleName);
        table.ifPresent(toml -> configDiff(configVariables, toml.rootNode())); // [default-module-name]
        return configDiff(configVariables, baseToml.rootNode()); // without name
    }

    private static int nonRootModuleConfigDiff(Toml baseToml,
                                               String orgName,
                                               ModuleName moduleName,
                                               Map<String, ConfigurableFinder.ConfigVariable> configVariables) {
        String moduleNameStr = moduleName.toString();
        String orgModuleKey = orgName + "." + moduleNameStr;
        Optional<Toml> table;
        table = baseToml.getTable(orgModuleKey);
        table.ifPresent(toml -> configDiff(configVariables, toml.rootNode())); // [org.default-module-name.module-name]
        table = baseToml.getTable(moduleNameStr);
        table.ifPresent(toml -> configDiff(configVariables, toml.rootNode())); // [default-module-name.module-name]
        table = baseToml.getTable(moduleName.moduleNamePart());
        return table.map(toml -> configDiff(configVariables, toml.rootNode())).orElse(0); // [module-name]
    }

    private static int configDiff(Map<String, ConfigurableFinder.ConfigVariable> configVariables,
                                  TomlTableNode moduleNode) {
        int lastNodeLine = 0;
        for (Map.Entry<String, TopLevelNode> entry: moduleNode.entries().entrySet()) {
            if (entry.getValue().kind() == TomlType.KEY_VALUE) {
                lastNodeLine = entry.getValue().location().lineRange().endLine().line();
            }
            configVariables.remove(entry.getKey());
        }
        return lastNodeLine;
    }
    
    private static boolean hasConfigurableQualifier(ModuleVariableDeclarationNode node) {
        return node.qualifiers().stream().anyMatch(q -> q.text().equals(Qualifier.CONFIGURABLE.getValue()));
    }

    private static Optional<Toml> readToml(Path path) {
        try {
            return Optional.ofNullable(Toml.read(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public String getName() {
        return CommandConstants.ADD_TO_CONFIG_TOML;
    }
}
