/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal.configurable.providers.toml;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBasicValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.tools.text.LineRange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.internal.ValueUtils.createReadOnlyXmlValue;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getEffectiveTomlType;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getLineRange;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getModuleKey;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getTomlTypeString;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.isSimpleType;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.isXMLType;
import static io.ballerina.runtime.internal.util.RuntimeUtils.isByteLiteral;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INCOMPATIBLE_TYPE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INVALID_BYTE_RANGE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_INVALID_ADDTIONAL_RECORD_FIELD;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_INVALID_MODULE_STRUCTURE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_REQUIRED_FILED_NOT_PROVIDED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_TABLE_KEY_NOT_PROVIDED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_UNUSED_VALUE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_UNION_VALUE_AMBIGUOUS_TARGET;

/**
 * Toml value provider for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlProvider implements ConfigProvider {

    private final Module rootModule;
    private final Set<TomlNode> visitedNodes = new HashSet<>();
    private final Set<LineRange> invalidTomlLines = new HashSet<>();
    private final ModuleInfo moduleInfo;

    Map<Module, List<TomlTableNode>> moduleNodeMap = new HashMap<>();

    Set<String> invalidRequiredModuleSet = new HashSet<>();

    TomlTableNode tomlNode;

    TomlProvider(Module rootModule, Set<Module> moduleSet) {
        this.rootModule = rootModule;
        this.moduleInfo = new ModuleInfo(moduleSet);
    }

    @Override
    public void initialize() {
        moduleInfo.analyseModules(rootModule);
    }

    @Override
    public void complete(RuntimeDiagnosticLog diagnosticLog) {
        if (tomlNode == null) {
            return;
        }
        validateUnusedNodes(tomlNode, "", diagnosticLog);
        visitedNodes.clear();
    }

    private void validateUnusedNodes(TomlTableNode baseNode, String moduleHeader, RuntimeDiagnosticLog diagnosticLog) {
        for (Map.Entry<String, TopLevelNode> nodeEntry : baseNode.entries().entrySet()) {
            TomlNode node = nodeEntry.getValue();
            String lineRange = getLineRange(node);
            String entryKey = nodeEntry.getKey();
            boolean isInvalidNode = invalidTomlLines.contains(node.location().lineRange());
            switch (node.kind()) {
                case KEY_VALUE:
                    if (!visitedNodes.contains(node) && !isInvalidNode) {
                        diagnosticLog.warn(CONFIG_TOML_UNUSED_VALUE, null, lineRange, moduleHeader + entryKey);
                    }
                    break;
                case TABLE:
                    validateUnusedTableNodes((TomlTableNode) node, diagnosticLog, moduleHeader, entryKey);
                    break;
                case TABLE_ARRAY:
                    if (!visitedNodes.contains(node) && !isInvalidNode) {
                        diagnosticLog.warn(CONFIG_TOML_UNUSED_VALUE, null, lineRange, moduleHeader + entryKey);
                    }
                    for (TomlTableNode tableNode : ((TomlTableArrayNode) node).children()) {
                        validateUnusedNodes(tableNode, entryKey + ".", diagnosticLog);
                    }
                    break;
                default:
            }

        }
    }

    private void validateUnusedTableNodes(TomlTableNode node, RuntimeDiagnosticLog diagnosticLog, String moduleHeader,
                                          String nodeName) {
        if (!visitedNodes.contains(node)) {
            String lineRange = getLineRange(node);
            boolean containsOrg = moduleInfo.containsOrg(nodeName);
            boolean containsModule = moduleInfo.containsModule(nodeName);
            if (!containsOrg && containsModule) {
                Module module = moduleInfo.getModuleFromName(nodeName);
                if (module != null && !invalidRequiredModuleSet.contains(module.toString()) &&
                        !rootModule.getOrg().equals(module.getOrg())) {
                    diagnosticLog.warn(CONFIG_TOML_INVALID_MODULE_STRUCTURE, null, lineRange, nodeName,
                            getModuleKey(module));
                }
            }
            if (!(containsOrg || containsModule) && !invalidTomlLines.contains(node.location().lineRange())) {
                diagnosticLog.warn(CONFIG_TOML_UNUSED_VALUE, null, lineRange, moduleHeader + nodeName);
            }
        }
        validateUnusedNodes(node, moduleHeader + nodeName + ".", diagnosticLog);
    }

    @Override
    public boolean hasConfigs() {
        return (tomlNode != null && !tomlNode.entries().isEmpty());
    }

    @Override
    public Optional<ConfigValue> getAsIntAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return getTomlConfigValue(value, key);
    }

    @Override
    public Optional<ConfigValue> getAsByteAndMark(Module module, VariableKey key) {
        TomlNode valueNode = getBasicTomlValue(module, key);
        if (valueNode == null) {
            return Optional.empty();
        }
        int byteValue = ((Long) (((TomlBasicValueNode<?>) valueNode).getValue())).intValue();
        if (!isByteLiteral(byteValue)) {
            invalidTomlLines.add(valueNode.location().lineRange());
            throw new ConfigException(RuntimeErrors.CONFIG_INVALID_BYTE_RANGE, getLineRange(valueNode),
                    key.variable, byteValue);
        }
        return getTomlConfigValue(byteValue, key);

    }

    @Override
    public Optional<ConfigValue> getAsStringAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        String stringVal = (String) value;
        return getTomlConfigValue(StringUtils.fromString(stringVal), key);
    }

    @Override
    public Optional<ConfigValue> getAsFloatAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return getTomlConfigValue(value, key);
    }

    @Override
    public Optional<ConfigValue> getAsBooleanAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return getTomlConfigValue(value, key);
    }

    @Override
    public Optional<ConfigValue> getAsDecimalAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return getTomlConfigValue(ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) value)), key);
    }

    @Override
    public Optional<ConfigValue> getAsArrayAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        for (TomlTableNode moduleNode : getModuleTomlNodes(module, key)) {
            if (moduleNode.entries().containsKey(key.variable)) {
                TomlNode tomlValue = moduleNode.entries().get(key.variable);
                validateArrayValue(tomlValue, key.variable, (ArrayType) effectiveType);
                return getTomlConfigValue(tomlValue, key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsRecordAndMark(Module module, VariableKey key) {
        for (TomlTableNode moduleNode : getModuleTomlNodes(module, key)) {
            if (moduleNode.entries().containsKey(key.variable)) {
                TomlNode tomlValue = moduleNode.entries().get(key.variable);
                validateRecordValue(tomlValue, key.variable, key.type);
                return getTomlConfigValue(tomlValue, key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsMapAndMark(Module module, VariableKey key) {
        String variableName = key.variable;
        MapType effectiveType = (MapType) ((IntersectionType) key.type).getEffectiveType();
        for (TomlTableNode moduleNode : getModuleTomlNodes(module, key)) {
            if (moduleNode.entries().containsKey(variableName)) {
                TomlNode tomlValue = moduleNode.entries().get(variableName);
                validateMapValue(tomlValue, variableName, effectiveType);
                return getTomlConfigValue(tomlValue, key);
            }
        }
        return Optional.empty();
    }

    private void validateMapValue(TomlNode tomlValue, String variableName, MapType mapType) {
        if (tomlValue.kind() != getEffectiveTomlType(mapType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, mapType,
                    getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        TomlTableNode tomlTableValue = (TomlTableNode) tomlValue;
        for (Map.Entry<String, TopLevelNode> field : tomlTableValue.entries().entrySet()) {
            String fieldName = field.getKey();
            validateValue(field.getValue(), variableName + "." + fieldName, mapType.getConstrainedType());
        }
    }

    private void validateValue(TomlNode tomlValue, String variableName, Type type) {
        if (isSimpleType(type.getTag()) || isXMLType(type)) {
            validatePrimitiveValue(tomlValue, variableName, type);
        } else {
            validateStructuredValue(tomlValue, variableName, type);
        }
    }

    private void validateStructuredValue(TomlNode tomlValue, String variableName, Type type) {
        switch (type.getTag()) {
            case TypeTags.ARRAY_TAG:
                validateArrayValue(tomlValue, variableName, (ArrayType) type);
                break;
            case TypeTags.RECORD_TYPE_TAG:
                validateRecordValue(tomlValue, variableName, type);
                break;
            case TypeTags.MAP_TAG:
                validateMapValue(tomlValue, variableName, (MapType) type);
                break;
            case TypeTags.TABLE_TAG:
                validateTableValue(tomlValue, variableName, (TableType) type);
                break;
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) type).getEffectiveType();
                if (effectiveType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    validateRecordValue(tomlValue, variableName, type);
                } else {
                    validateValue(tomlValue, variableName, effectiveType);
                }
                break;
            case TypeTags.ANYDATA_TAG:
            case TypeTags.UNION_TAG:
                validateUnionValue(tomlValue, variableName, (BUnionType) type);
                break;
            default:
                invalidTomlLines.add(tomlValue.location().lineRange());
                throw new ConfigException(CONFIG_TYPE_NOT_SUPPORTED, variableName, type.toString());
        }
    }

    @Override
    public Optional<ConfigValue> getAsTableAndMark(Module module, VariableKey key) {
        TableType tableType = (TableType) ((BIntersectionType) key.type).getEffectiveType();
        for (TomlTableNode moduleNode : getModuleTomlNodes(module, key)) {
            if (moduleNode.entries().containsKey(key.variable)) {
                TomlNode tomlValue = moduleNode.entries().get(key.variable);
                validateTableValue(tomlValue, key.variable, tableType);
                return getTomlConfigValue(tomlValue, key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsUnionAndMark(Module module, VariableKey key) {
        for (TomlTableNode moduleNode : getModuleTomlNodes(module, key)) {
            if (moduleNode.entries().containsKey(key.variable)) {
                TomlNode tomlValue = moduleNode.entries().get(key.variable);
                BUnionType unionType = (BUnionType) ((BIntersectionType) key.type).getEffectiveType();
                validateUnionValue(tomlValue, key.variable, unionType);
                return getTomlConfigValue(tomlValue, key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsXmlAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return getTomlConfigValue(createReadOnlyXmlValue((String) value), key);
    }

    private Object getPrimitiveTomlValue(Module module, VariableKey key) {
        TomlNode tomlValue = getBasicTomlValue(module, key);
        if (tomlValue == null) {
            return null;
        }
        return ((TomlBasicValueNode<?>) tomlValue).getValue();
    }

    private TomlNode getBasicTomlValue(Module module, VariableKey key) {
        String variableName = key.variable;
        for (TomlTableNode moduleNode : getModuleTomlNodes(module, key)) {
            if (moduleNode.entries().containsKey(variableName)) {
                TomlNode tomlValue = moduleNode.entries().get(variableName);
                return getTomlNode(tomlValue, variableName, key.type);
            }
        }
        return null;
    }

    private TomlNode getTomlNode(TomlNode tomlValue, String variableName, Type type) {
        TomlType tomlType = tomlValue.kind();
        if (tomlType != TomlType.KEY_VALUE) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, type,
                                      getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        tomlType = tomlValue.kind();
        if (tomlType != getEffectiveTomlType(type, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, type,
                                      getTomlTypeString(tomlValue));
        }
        return tomlValue;
    }

    private List<TomlTableNode> retrieveModuleNode(Module module, boolean hasRequired) {
        Toml baseToml = new Toml(tomlNode);
        String orgName = module.getOrg();
        String moduleName = module.getName();
        if (orgName.equals(rootModule.getOrg())) {
            if (moduleName.equals(rootModule.getName())) {
                return getRootModuleNode(baseToml);
            }
            return getNonDefaultModuleNode(baseToml, module, hasRequired);
        }
        return getImportedModuleNode(baseToml, module, hasRequired);
    }

    private List<TomlTableNode> getImportedModuleNode(Toml baseToml, Module module, boolean hasRequired) {
        String moduleKey = getModuleKey(module);
        Optional<Toml> table = baseToml.getTable(moduleKey);
        List<TomlTableNode> moduleNodes = new ArrayList<>();
        if (table.isEmpty() && hasRequired && !invalidRequiredModuleSet.contains(module.toString())) {
            throwInvalidImportedModuleError(baseToml, module);
        }
        table.ifPresent(toml -> addToModuleNodeMap(toml, moduleNodes));
        return moduleNodes;
    }

    private void addToModuleNodeMap(Toml table, List<TomlTableNode> moduleNodes) {
        TomlTableNode tableNode = table.rootNode();
        moduleNodes.add(tableNode);
        visitedNodes.add(tableNode);
    }

    private void throwInvalidImportedModuleError(Toml toml, Module module) {
        String moduleKey = getModuleKey(module);
        TomlNode errorNode = null;
        Optional<TomlValueNode> valueNode =  toml.get(moduleKey);
        List<Toml> tomlTables = toml.getTables(moduleKey);
        String moduleName = module.getName();
        if (valueNode.isEmpty()) {
            valueNode =  toml.get(moduleName);
        }
        if (tomlTables.isEmpty()) {
            tomlTables = toml.getTables(moduleName);
        }
        Optional<Toml> tableNode = toml.getTable(moduleName);
        if (tableNode.isPresent()) {
            errorNode = tableNode.get().rootNode();
        } else if (valueNode.isPresent()) {
            errorNode = valueNode.get();
        } else if (!tomlTables.isEmpty()) {
            errorNode = tomlTables.get(0).rootNode();
        }
        if (errorNode != null) {
            invalidRequiredModuleSet.add(module.toString());
            invalidTomlLines.add(errorNode.location().lineRange());
            throw new ConfigException(CONFIG_TOML_INVALID_MODULE_STRUCTURE, getLineRange(errorNode), moduleKey,
                    moduleKey);
        }
    }

    private List<TomlTableNode> getNonDefaultModuleNode(Toml baseToml, Module module, boolean hasRequired) {
        String moduleName = module.getName();
        Optional<Toml> table;
        List<TomlTableNode> moduleNodes = new ArrayList<>();
        String moduleKey = getModuleKey(module);
        if (moduleInfo.hasModuleAmbiguity()) {
            table = baseToml.getTable(moduleKey);
            if (table.isPresent()) {
                addToModuleNodeMap(table.get(), moduleNodes);
            } else if (!invalidRequiredModuleSet.contains(module.toString())) {
                invalidRequiredModuleSet.add(module.toString());
                throw new ConfigException(RuntimeErrors.CONFIG_TOML_MODULE_AMBIGUITY, getLineRange(baseToml.rootNode()),
                        moduleName, moduleKey);
            }
        }
        table = baseToml.getTable(moduleName);
        table.ifPresent(toml -> moduleNodes.add(toml.rootNode()));
        table = baseToml.getTable(moduleKey);
        if (table.isEmpty() && hasRequired && !invalidRequiredModuleSet.contains(module.toString())) {
            throwInvalidSubModuleError(baseToml, module);
        }
        table.ifPresent(toml -> addToModuleNodeMap(toml, moduleNodes));
        return moduleNodes;
    }


    private void throwInvalidSubModuleError(Toml toml, Module module) {
        String moduleName = module.getName();
        TomlNode errorNode = null;
        Optional<TomlValueNode> valueNode = toml.get(moduleName);
        List<Toml> tomlTables = toml.getTables(moduleName);
        if (valueNode.isEmpty()) {
            valueNode =  toml.get(getModuleKey(module));
        }
        if (tomlTables.isEmpty()) {
            tomlTables =  toml.getTables(getModuleKey(module));
        }
        if (valueNode.isPresent()) {
            errorNode = valueNode.get();
        } else if (!tomlTables.isEmpty()) {
            errorNode = tomlTables.get(0).rootNode();
        } else {
            Optional<Toml> tomlValueNode = toml.getTable(moduleName.replaceFirst(rootModule.getName() + ".", ""));
            if (tomlValueNode.isPresent()) {
                errorNode = tomlValueNode.get().rootNode();
            }
        }
        if (errorNode != null) {
            invalidRequiredModuleSet.add(module.toString());
            invalidTomlLines.add(errorNode.location().lineRange());
            throw new ConfigException(CONFIG_TOML_INVALID_MODULE_STRUCTURE, getLineRange(errorNode), moduleName,
                    moduleName);
        }
    }

    private List<TomlTableNode> getRootModuleNode(Toml baseToml) {
        String moduleName = rootModule.getName();
        String moduleKey = getModuleKey(rootModule);
        Optional<Toml> table = baseToml.getTable(moduleKey);
        List<TomlTableNode> moduleNodes = new ArrayList<>();
        if (table.isPresent()) {
            moduleNodes.add(table.get().rootNode());
        } else if (moduleInfo.hasModuleAmbiguity()) {
            throw new ConfigException(RuntimeErrors.CONFIG_TOML_MODULE_AMBIGUITY, getLineRange(baseToml.rootNode()),
                    moduleName, moduleKey);
        }
        table = baseToml.getTable(moduleName);
        table.ifPresent(toml -> addToModuleNodeMap(toml, moduleNodes));
        moduleNodes.add(baseToml.rootNode());
        return moduleNodes;
    }

    private void validatePrimitiveValue(TomlNode tomlValue, String variableName, Type type) {
        TomlType tomlType = tomlValue.kind();
        if (tomlType != TomlType.KEY_VALUE) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, type,
                    getTomlTypeString(tomlValue));
        }
        TomlValueNode value = ((TomlKeyValueNode) tomlValue).value();
        tomlType = value.kind();
        if (tomlType != getEffectiveTomlType(type, variableName)) {
            invalidTomlLines.add(value.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(value), variableName, type,
                    getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        validateByteValue(variableName, type, value);
    }

    private void validateUnionValue(TomlNode tomlValue, String variableName, BUnionType unionType) {
        visitedNodes.add(tomlValue);
        Object balValue = Utils.getBalValueFromToml(tomlValue, visitedNodes, unionType, invalidTomlLines, variableName);
        List<Type> convertibleTypes = new ArrayList<>();
        for (Type type : unionType.getMemberTypes()) {
            if (TypeChecker.checkIsLikeType(balValue, type, false)) {
                convertibleTypes.add(type);
                if (convertibleTypes.size() > 1) {
                    invalidTomlLines.add(tomlValue.location().lineRange());
                    throw new ConfigException(CONFIG_UNION_VALUE_AMBIGUOUS_TARGET, getLineRange(tomlValue),
                                              variableName, IdentifierUtils.decodeIdentifier(unionType.toString()));
                }
            }
        }
        if (convertibleTypes.isEmpty()) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                                      IdentifierUtils.decodeIdentifier(unionType.toString()),
                    getTomlTypeString(tomlValue));
        }
        Type type = convertibleTypes.get(0);
        if (isSimpleType(type.getTag()) || isXMLType(type)) {
            return;
        }

        if (type.getTag() == TypeTags.FINITE_TYPE_TAG) {
            if (((BFiniteType) type).valueSpace.contains(balValue)) {
                return;
            } else {
                throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                        IdentifierUtils.decodeIdentifier(type.toString()), getTomlTypeString(tomlValue));
            }
        }
        validateStructuredValue(tomlValue, variableName, type);
    }

    private void validateArrayValue(TomlNode tomlValue, String variableName, ArrayType arrayType) {
        Type elementType = arrayType.getElementType();
        if (isSimpleType(elementType.getTag())) {
            if (tomlValue.kind() != TomlType.KEY_VALUE) {
                invalidTomlLines.add(tomlValue.location().lineRange());
                throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                                          arrayType, getTomlTypeString(tomlValue));
            }
            visitedNodes.add(tomlValue);
            tomlValue = ((TomlKeyValueNode) tomlValue).value();
            validatePrimitiveArray(tomlValue, variableName, arrayType);
        } else {
            validateNonPrimitiveArray(tomlValue, variableName, arrayType, elementType);
        }
    }

    private void validateNonPrimitiveArray(TomlNode tomlValue, String variableName, ArrayType arrayType,
                                           Type elementType) {
        switch (elementType.getTag()) {
            case TypeTags.ARRAY_TAG:
            case TypeTags.XML_ATTRIBUTES_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TAG:
            case TypeTags.XML_TEXT_TAG:
                if (tomlValue.kind() != TomlType.KEY_VALUE) {
                    invalidTomlLines.add(tomlValue.location().lineRange());
                    throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                            arrayType, getTomlTypeString(tomlValue));
                }
                visitedNodes.add(tomlValue);
                tomlValue = ((TomlKeyValueNode) tomlValue).value();
                validatePrimitiveArray(tomlValue, variableName, arrayType);
                break;
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                validateMapValueArray(tomlValue, variableName, arrayType, elementType);
                break;
            case TypeTags.ANYDATA_TAG:
            case TypeTags.UNION_TAG:
                validateUnionValueArray(tomlValue, variableName, arrayType, elementType);
                break;
            default:
                Type effectiveType = ((IntersectionType) elementType).getEffectiveType();
                validateNonPrimitiveArray(tomlValue, variableName, arrayType, effectiveType);
                break;
        }
    }

    private void validateMapValueArray(TomlNode tomlValue, String variableName, ArrayType arrayType, Type elementType) {
        if (tomlValue.kind() != TomlType.TABLE_ARRAY) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, arrayType,
                    getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        for (TomlTableNode tomlTableNode : tableNodeList) {
            validateValue(tomlTableNode, variableName, elementType);
        }
    }

    private void validateUnionValueArray(TomlNode tomlValue, String variableName, ArrayType arrayType,
                                         Type elementType) {
        TomlValueNode valueNode = ((TomlKeyValueNode) tomlValue).value();
        if (valueNode.kind() != getEffectiveTomlType(arrayType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, arrayType,
                                      getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        validateArrayElements(variableName, ((TomlArrayValueNode) valueNode).elements(), elementType);
    }

    private void validatePrimitiveArray(TomlNode tomlValue, String variableName, ArrayType arrayType) {
        if (tomlValue.kind() != getEffectiveTomlType(arrayType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, arrayType,
                    getTomlTypeString(tomlValue));
        }
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlValue).elements();
        validateArrayElements(variableName, arrayList, arrayType.getElementType());
    }

    private void validateArrayElements(String variableName, List<TomlValueNode> arrayList,
                                                                          Type elementType) {
        int arraySize = arrayList.size();
        for (int i = 0; i < arraySize; i++) {
            String elementName = variableName + "[" + i + "]";
            TomlNode tomlValueNode = arrayList.get(i);
            switch (elementType.getTag()) {
                case TypeTags.INTERSECTION_TAG:
                    validateArrayElements(variableName, arrayList,
                            ((BIntersectionType) elementType).getEffectiveType());
                    break;
                case TypeTags.ARRAY_TAG:
                    ArrayType arrayType = (ArrayType) elementType;
                    validatePrimitiveArray(tomlValueNode, variableName, arrayType);
                    break;
                case TypeTags.ANYDATA_TAG:
                case TypeTags.UNION_TAG:
                    validateUnionValue(tomlValueNode, variableName, (BUnionType) elementType);
                    break;
                default:
                    if (tomlValueNode.kind() != getEffectiveTomlType(elementType, elementName)) {
                        invalidTomlLines.add(tomlValueNode.location().lineRange());
                        throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValueNode), elementName,
                                                  elementType, getTomlTypeString(tomlValueNode));
                    }
                    validateByteValue(variableName, elementType, arrayList.get(i));
            }
        }
    }

    private void validateRecordValue(TomlNode tomlNode, String variableName, Type type) {
        RecordType recordType;
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            recordType = (RecordType) type;
        } else {
            recordType = (RecordType) ((BIntersectionType) type).constituentTypes.get(0);
        }
        if (tomlNode.kind() != getEffectiveTomlType(recordType, variableName)) {
            invalidTomlLines.add(tomlNode.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlNode), variableName, recordType,
                                      getTomlTypeString(tomlNode));
        }
        TomlTableNode tomlValue = (TomlTableNode) tomlNode;
        visitedNodes.add(tomlValue);
        for (Map.Entry<String, TopLevelNode> tomlField : tomlValue.entries().entrySet()) {
            String fieldName = tomlField.getKey();
            Field field = recordType.getFields().get(fieldName);
            TomlNode value = tomlField.getValue();
            if (field == null) {
                if (recordType.isSealed()) {
                    // Panic if this record type is closed and user adds a new field.
                    invalidTomlLines.add(value.location().lineRange());
                    throw new ConfigException(CONFIG_TOML_INVALID_ADDTIONAL_RECORD_FIELD, getLineRange(value),
                            fieldName, recordType.toString());
                }
                field = Utils.createAdditionalField(recordType, fieldName, value);
            }
            validateValue(value, variableName + "." + fieldName, field.getFieldType());
        }
        validateRequiredField(tomlValue.entries(), recordType, variableName, tomlValue);
    }

    private void validateRequiredField(Map<String, TopLevelNode> initialValueEntries, RecordType recordType,
                                       String variableName, TomlNode tomlNode) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            long flag = field.getValue().getFlags();
            if (SymbolFlags.isFlagOn(flag, SymbolFlags.REQUIRED) && initialValueEntries.get(fieldName) == null) {
                invalidTomlLines.add(tomlNode.location().lineRange());
                throw new ConfigException(CONFIG_TOML_REQUIRED_FILED_NOT_PROVIDED, getLineRange(tomlNode), fieldName,
                                          recordType.toString(), variableName);
            }
        }
    }

    private void validateTableValue(TomlNode tomlValue, String variableName, TableType tableType) {
        Type constraintType = tableType.getConstrainedType();
        if (tomlValue.kind() != getEffectiveTomlType(tableType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, tableType,
                                      getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        String[] keys = tableType.getFieldNames();
        for (TomlTableNode tomlTableNode : tableNodeList) {
            if (keys != null) {
                validateKeyField(tomlTableNode, keys, tableType, variableName);
            }
            validateValue(tomlTableNode, variableName, constraintType);
        }
    }

    private void validateKeyField(TomlTableNode recordTable, String[] fieldNames, Type tableType,
                                         String variableName) {
        for (String key : fieldNames) {
            if (recordTable.entries().get(key) == null) {
                invalidTomlLines.add(recordTable.location().lineRange());
                throw new ConfigException(CONFIG_TOML_TABLE_KEY_NOT_PROVIDED, getLineRange(recordTable), key,
                                          tableType.toString()
                        , variableName);
            }
        }
    }

    private void validateByteValue(String variableName, Type type, TomlValueNode tomlValueNode) {
        Object tomlValue = ((TomlBasicValueNode<?>) tomlValueNode).getValue();
        if (type.getTag() == TypeTags.BYTE_TAG) {
            int value = ((Long) tomlValue).intValue();
            if (!isByteLiteral(value)) {
                invalidTomlLines.add(tomlValueNode.location().lineRange());
                throw new ConfigException(CONFIG_INVALID_BYTE_RANGE, getLineRange(tomlValueNode), variableName, value);
            }
        }
    }

    private List<TomlTableNode> getModuleTomlNodes(Module module, VariableKey key) {
        if (moduleNodeMap.containsKey(module)) {
            List<TomlTableNode> moduleNodes = moduleNodeMap.get(module);
            if ((moduleNodes != null && !moduleNodes.isEmpty()) || !key.isRequired()) {
                return moduleNodes;
            }
        }
        List<TomlTableNode> tomlTableNodes = null;
        try {
            tomlTableNodes = retrieveModuleNode(module, key.isRequired());
        } finally {
            moduleNodeMap.put(module, tomlTableNodes);
        }
        return tomlTableNodes;
    }

    private Optional<ConfigValue> getTomlConfigValue(Object value, VariableKey key) {
        return Optional.of(new TomlConfigValue(value, key.type));
    }
}
