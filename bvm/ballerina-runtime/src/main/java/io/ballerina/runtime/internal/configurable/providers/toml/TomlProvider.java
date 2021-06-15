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
import io.ballerina.runtime.api.creators.TypeCreator;
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
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;
import io.ballerina.runtime.internal.values.TableValueImpl;
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

import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getEffectiveTomlType;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getLineRange;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getModuleKey;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getTomlTypeString;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.isPrimitiveType;
import static io.ballerina.runtime.internal.util.RuntimeUtils.isByteLiteral;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INCOMPATIBLE_TYPE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INVALID_BYTE_RANGE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_DEFAULT_FILED_NOT_SUPPORTED;
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

    Map<Module, TomlTableNode> moduleTomlNodeMap = new HashMap<>();

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
    public Optional<Long> getAsIntAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((Long) value);
    }

    @Override
    public Optional<Integer> getAsByteAndMark(Module module, VariableKey key) {
        TomlNode valueNode = getBasicTomlValue(module, key);
        if (valueNode != null) {
            int byteValue = ((Long) (((TomlBasicValueNode<?>) valueNode).getValue())).intValue();
            if (!isByteLiteral(byteValue)) {
                invalidTomlLines.add(valueNode.location().lineRange());
                throw new ConfigException(RuntimeErrors.CONFIG_INVALID_BYTE_RANGE, getLineRange(valueNode),
                                          key.variable, byteValue);
            }
            return Optional.of(byteValue);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BString> getAsStringAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        String stringVal = (String) value;
        return Optional.of(StringUtils.fromString(stringVal));
    }

    @Override
    public Optional<Double> getAsFloatAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((double) value);
    }

    @Override
    public Optional<Boolean> getAsBooleanAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((boolean) value);
    }

    @Override
    public Optional<BDecimal> getAsDecimalAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) value)));
    }

    @Override
    public Optional<BArray> getAsArrayAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(key.variable)) {
            TomlNode tomlValue = moduleNode.entries().get(key.variable);
            return Optional.of(retrieveArrayValue(tomlValue, key.variable, (ArrayType) effectiveType));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BMap<BString, Object>> getAsRecordAndMark(Module module, VariableKey key) {
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(key.variable)) {
            TomlNode tomlValue = moduleNode.entries().get(key.variable);
            return Optional.of(retrieveRecordValue(tomlValue, key.variable, key.type));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BMap<BString, Object>> getAsMapAndMark(Module module, VariableKey key) {
        String variableName = key.variable;
        MapType effectiveType = (MapType) ((IntersectionType) key.type).getEffectiveType();
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(variableName)) {
            TomlNode tomlValue = moduleNode.entries().get(variableName);
            return Optional.of(retrieveMapValue(tomlValue, variableName, effectiveType));
        }
        return Optional.empty();
    }

    private BMap<BString, Object> retrieveMapValue(TomlNode tomlValue, String variableName, MapType mapType) {
        if (tomlValue.kind() != getEffectiveTomlType(mapType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, mapType,
                    getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        TomlTableNode tomlTableValue = (TomlTableNode) tomlValue;
        MappingInitialValueEntry.KeyValueEntry[] keyValueEntries =
                new MappingInitialValueEntry.KeyValueEntry[tomlTableValue.entries().size()];
        int count = 0;
        for (Map.Entry<String, TopLevelNode> field : tomlTableValue.entries().entrySet()) {
            String fieldName = field.getKey();
            Object value =
                    retrieveValue(field.getValue(), variableName + "." + fieldName, mapType.getConstrainedType());
            keyValueEntries[count] =
                    new MappingInitialValueEntry.KeyValueEntry(StringUtils.fromString(fieldName), value);
            count++;

        }
        return ValueCreator.createMapValue(mapType, keyValueEntries);
    }

    private Object retrieveValue(TomlNode tomlValue, String variableName, Type type) {
        if (isPrimitiveType(type.getTag())) {
            return retrievePrimitiveValue(tomlValue, variableName, type);
        }
        return retrieveStructuredValue(tomlValue, variableName, type);
    }

    private Object retrieveStructuredValue(TomlNode tomlValue, String variableName, Type type) {
        switch (type.getTag()) {
            case TypeTags.ARRAY_TAG:
                return retrieveArrayValue(tomlValue, variableName, (ArrayType) type);
            case TypeTags.RECORD_TYPE_TAG:
                return retrieveRecordValue(tomlValue, variableName, type);
            case TypeTags.MAP_TAG:
                return retrieveMapValue(tomlValue, variableName, (MapType) type);
            case TypeTags.TABLE_TAG:
                return retrieveTableValue(tomlValue, variableName, (TableType) type);
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) type).getEffectiveType();
                if (effectiveType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    return retrieveRecordValue(tomlValue, variableName, type);
                }
                return retrieveValue(tomlValue, variableName, effectiveType);
            case TypeTags.ANYDATA_TAG:
            case TypeTags.UNION_TAG:
                return retrieveUnionValue(tomlValue, variableName, (BUnionType) type);
            default:
                invalidTomlLines.add(tomlValue.location().lineRange());
                throw new ConfigException(CONFIG_TYPE_NOT_SUPPORTED, variableName, type.toString());
        }
    }

    @Override
    public Optional<BTable<BString, Object>> getAsTableAndMark(Module module, VariableKey key) {
        TableType tableType = (TableType) ((BIntersectionType) key.type).getEffectiveType();
        Type constraintType = tableType.getConstrainedType();
        if (constraintType.getTag() == TypeTags.INTERSECTION_TAG) {
            constraintType = ((IntersectionType) constraintType).getEffectiveType();
        }
        if (constraintType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            tableType = (TableType) ((BIntersectionType) key.type).getConstituentTypes().get(0);
        }
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(key.variable)) {
            TomlNode tomlValue = moduleNode.entries().get(key.variable);
            return Optional.of(retrieveTableValue(tomlValue, key.variable, tableType));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Object> getAsUnionAndMark(Module module, VariableKey key) {
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        TomlNode tomlValue = null;
        if (moduleNode != null && moduleNode.entries().containsKey(key.variable)) {
            tomlValue = moduleNode.entries().get(key.variable);
        }
        if (tomlValue == null) {
            return Optional.empty();
        }
        BUnionType unionType = (BUnionType) ((BIntersectionType) key.type).getEffectiveType();
        return Optional.of(retrieveUnionValue(tomlValue, key.variable, unionType));
    }

    @Override
    public Optional<BXml> getAsXmlAndMark(Module module, VariableKey key) {
        // This will throw error if user has configured xml variable in the toml
        getPrimitiveTomlValue(module, key);
        return Optional.empty();
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
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(variableName)) {
            TomlNode tomlValue = moduleNode.entries().get(variableName);
            return getTomlNode(tomlValue, variableName, key.type);
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

    private TomlTableNode retrieveModuleNode(Module module, boolean hasRequired) {
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

    private TomlTableNode getImportedModuleNode(Toml baseToml, Module module, boolean hasRequired) {
        String moduleKey = getModuleKey(module);
        Optional<Toml> table = baseToml.getTable(moduleKey);
        if (table.isEmpty() && hasRequired && !invalidRequiredModuleSet.contains(module.toString())) {
            throwInvalidImportedModuleError(baseToml, module);
        }
        table.ifPresent(toml -> visitedNodes.add(toml.rootNode()));
        return table.map(Toml::rootNode).orElse(null);
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

    private TomlTableNode getNonDefaultModuleNode(Toml baseToml, Module module, boolean hasRequired) {
        String moduleName = module.getName();
        Optional<Toml> table;
        String moduleKey = getModuleKey(module);
        if (moduleInfo.hasModuleAmbiguity()) {
            table = baseToml.getTable(moduleKey);
            if (table.isPresent()) {
                TomlTableNode tableNode = table.get().rootNode();
                visitedNodes.add(tableNode);
                return tableNode;
            }
            if (!invalidRequiredModuleSet.contains(module.toString())) {
                invalidRequiredModuleSet.add(module.toString());
                throw new ConfigException(RuntimeErrors.CONFIG_TOML_MODULE_AMBIGUITY, getLineRange(baseToml.rootNode()),
                                          moduleName, moduleKey);
            } else {
                return null;
            }
        }
        table = baseToml.getTable(moduleName);
        if (table.isEmpty()) {
            table = baseToml.getTable(moduleKey);
            if (table.isEmpty() && hasRequired && !invalidRequiredModuleSet.contains(module.toString())) {
                throwInvalidSubModuleError(baseToml, module);
            }
        }
        table.ifPresent(toml -> visitedNodes.add(toml.rootNode()));
        return table.map(Toml::rootNode).orElse(null);
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

    private TomlTableNode getRootModuleNode(Toml baseToml) {
        String moduleName = rootModule.getName();
        String moduleKey = getModuleKey(rootModule);
        Optional<Toml> table = baseToml.getTable(moduleKey);
        if (table.isEmpty()) {
            if (moduleInfo.hasModuleAmbiguity()) {
                throw new ConfigException(RuntimeErrors.CONFIG_TOML_MODULE_AMBIGUITY, getLineRange(baseToml.rootNode()),
                        moduleName, moduleKey);
            }
            table = baseToml.getTable(moduleName);
            if (table.isEmpty() || moduleInfo.containsOnlySubModules(table.get().rootNode().entries().keySet())) {
                return baseToml.rootNode();
            }
        }
        table.ifPresent(toml -> visitedNodes.add(toml.rootNode()));
        return table.map(Toml::rootNode).orElse(null);
    }

    private Object retrievePrimitiveValue(TomlNode tomlValue, String variableName, Type type) {
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
        return getBalValue(variableName, type, value);
    }

    private Object retrieveUnionValue(TomlNode tomlValue, String variableName, BUnionType unionType) {
        Object balValue = Utils.getBalValue(tomlValue, visitedNodes);
        List<Type> convertibleTypes = new ArrayList<>();
        for (Type type : unionType.getMemberTypes()) {
            if (TypeChecker.checkIsLikeType(balValue, type, false)) {
                convertibleTypes.add(type);
            }
        }
        if (convertibleTypes.isEmpty()) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                                      IdentifierUtils.decodeIdentifier(unionType.toString()),
                                      getTomlTypeString(tomlValue));
        }
        if (convertibleTypes.size() > 1) {
            throw new ConfigException(CONFIG_UNION_VALUE_AMBIGUOUS_TARGET, getLineRange(tomlValue), variableName,
                                      IdentifierUtils.decodeIdentifier(unionType.toString()));
        }
        Type type = convertibleTypes.get(0);
        if (isPrimitiveType(type.getTag())) {
            return balValue;
        }
        if (type.getTag() == TypeTags.FINITE_TYPE_TAG) {
            if (((BFiniteType) type).valueSpace.contains(balValue)) {
                return balValue;
            } else {
                throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                                          IdentifierUtils.decodeIdentifier(type.toString()),
                                          getTomlTypeString(tomlValue));
            }
        }
        return retrieveStructuredValue(tomlValue, variableName, type);
    }


    private BArray retrieveArrayValue(TomlNode tomlValue, String variableName, ArrayType arrayType) {
        Type elementType = arrayType.getElementType();
        if (isPrimitiveType(elementType.getTag())) {
            if (tomlValue.kind() != TomlType.KEY_VALUE) {
                invalidTomlLines.add(tomlValue.location().lineRange());
                throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                                          arrayType, getTomlTypeString(tomlValue));
            }
            visitedNodes.add(tomlValue);
            tomlValue = ((TomlKeyValueNode) tomlValue).value();
            return getPrimitiveArray(tomlValue, variableName, arrayType);
        } else {
            return getNonPrimitiveArray(tomlValue, variableName, arrayType, elementType);
        }
    }

    private BArray getNonPrimitiveArray(TomlNode tomlValue, String variableName, ArrayType arrayType,
                                        Type elementType) {
        switch (elementType.getTag()) {
            case TypeTags.ARRAY_TAG:
                if (tomlValue.kind() != TomlType.KEY_VALUE) {
                    invalidTomlLines.add(tomlValue.location().lineRange());
                    throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                            arrayType, getTomlTypeString(tomlValue));
                }
                visitedNodes.add(tomlValue);
                tomlValue = ((TomlKeyValueNode) tomlValue).value();
                return getPrimitiveArray(tomlValue, variableName, arrayType);
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return getMapValueArray(tomlValue, variableName, arrayType, elementType);
            case TypeTags.ANYDATA_TAG:
            case TypeTags.UNION_TAG:
                return getUnionValueArray(tomlValue, variableName, arrayType, elementType);
            default:
                Type effectiveType = ((IntersectionType) elementType).getEffectiveType();
                switch(effectiveType.getTag()) {
                    case TypeTags.ARRAY_TAG:
                        if (tomlValue.kind() != TomlType.KEY_VALUE) {
                            invalidTomlLines.add(tomlValue.location().lineRange());
                            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName,
                                    arrayType, getTomlTypeString(tomlValue));
                        }
                        visitedNodes.add(tomlValue);
                        tomlValue = ((TomlKeyValueNode) tomlValue).value();
                        return getPrimitiveArray(tomlValue, variableName, arrayType);
                    case TypeTags.RECORD_TYPE_TAG:
                    case TypeTags.MAP_TAG:
                        return getMapValueArray(tomlValue, variableName, arrayType, effectiveType);
                    case TypeTags.ANYDATA_TAG:
                    case TypeTags.UNION_TAG:
                        return getUnionValueArray(tomlValue, variableName, arrayType, effectiveType);
                    default:
                        invalidTomlLines.add(tomlValue.location().lineRange());
                        throw new ConfigException(CONFIG_TYPE_NOT_SUPPORTED, variableName, arrayType.toString());
                }
        }
    }

    private BArray getMapValueArray(TomlNode tomlValue, String variableName, ArrayType arrayType, Type elementType) {
        if (tomlValue.kind() != TomlType.TABLE_ARRAY) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, arrayType,
                    getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        int arraySize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] entries = new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            Object value = retrieveValue(tableNodeList.get(i), variableName, elementType);
            entries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        return new ArrayValueImpl(arrayType, entries.length, entries);
    }

    private BArray getUnionValueArray(TomlNode tomlValue, String variableName, ArrayType arrayType, Type elementType) {
        TomlValueNode tomlNode = ((TomlKeyValueNode) tomlValue).value();
        ListInitialValueEntry.ExpressionEntry[] expressionEntries;
        if (tomlNode.kind() != getEffectiveTomlType(arrayType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, arrayType,
                                      getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlNode).elements();
        expressionEntries = createArray(variableName, arrayList, elementType);
        return new ArrayValueImpl(arrayType, expressionEntries.length, expressionEntries);
    }

    private BArray getPrimitiveArray(TomlNode tomlValue, String variableName, ArrayType arrayType) {
        ListInitialValueEntry.ExpressionEntry[] expressionEntries;
        if (tomlValue.kind() != getEffectiveTomlType(arrayType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, arrayType,
                                      getTomlTypeString(tomlValue));
        }
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlValue).elements();
        expressionEntries = createArray(variableName, arrayList, arrayType.getElementType());
        return new ArrayValueImpl(arrayType, expressionEntries.length, expressionEntries);
    }

    private ListInitialValueEntry.ExpressionEntry[] createArray(String variableName, List<TomlValueNode> arrayList,
                                                                Type elementType) {
        int arraySize = arrayList.size();
        ListInitialValueEntry.ExpressionEntry[] arrayEntries =
                new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            String elementName = variableName + "[" + i + "]";
            Object balValue;
            TomlNode tomlValueNode = arrayList.get(i);

            switch (elementType.getTag()) {
                case TypeTags.INTERSECTION_TAG:
                    ArrayType arrayType = (ArrayType) ((BIntersectionType) elementType).getEffectiveType();
                    balValue = getPrimitiveArray(tomlValueNode, variableName, arrayType);
                    break;
                case TypeTags.ANYDATA_TAG:
                case TypeTags.UNION_TAG:
                    balValue = retrieveUnionValue(tomlValueNode, variableName, (BUnionType) elementType);
                    break;
                default:
                    if (tomlValueNode.kind() != getEffectiveTomlType(elementType, elementName)) {
                        invalidTomlLines.add(tomlValueNode.location().lineRange());
                        throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValueNode), elementName,
                                                  elementType, getTomlTypeString(tomlValueNode));
                    }
                    balValue = getBalValue(variableName, elementType, arrayList.get(i));
            }
            arrayEntries[i] = new ListInitialValueEntry.ExpressionEntry(balValue);
        }
        return arrayEntries;
    }

    private BMap<BString, Object> retrieveRecordValue(TomlNode tomlNode, String variableName, Type type) {
        RecordType recordType;
        String recordName;
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            recordName = type.getName();
            recordType = (RecordType) type;
        } else {
            recordType = (RecordType) ((BIntersectionType) type).getConstituentTypes().get(0);
            recordName = recordType.getName();
        }
        if (tomlNode.kind() != getEffectiveTomlType(recordType, variableName)) {
            invalidTomlLines.add(tomlNode.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlNode), variableName, recordType,
                                      getTomlTypeString(tomlNode));
        }

        TomlTableNode tomlValue = (TomlTableNode) tomlNode;
        visitedNodes.add(tomlValue);
        Map<String, Object> initialValueEntries = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> tomlField : tomlValue.entries().entrySet()) {
            String fieldName = tomlField.getKey();
            Field field = recordType.getFields().get(fieldName);
            TomlNode value = tomlField.getValue();
            if (field == null) {
                invalidTomlLines.add(value.location().lineRange());
                throw new ConfigException(CONFIG_TOML_INVALID_ADDTIONAL_RECORD_FIELD, getLineRange(value), fieldName,
                                          variableName, recordType.toString());
            }
            Type fieldType = field.getFieldType();
            Object objectValue = retrieveValue(value, variableName + "." + fieldName, fieldType);
            initialValueEntries.put(fieldName, objectValue);
        }
        validateRequiredField(initialValueEntries, recordType, variableName, tomlValue);
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG && type.isReadOnly()) {
            return createReadOnlyFieldRecord(initialValueEntries, recordType, variableName, tomlValue);
        }
        return ValueCreator.createReadonlyRecordValue(recordType.getPackage(), recordName, initialValueEntries);
    }

    private BMap<BString, Object> createReadOnlyFieldRecord(Map<String, Object> initialValueEntries,
                                                                   RecordType recordType, String variableName,
                                                                   TomlTableNode tomlValue) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            long flag = field.getValue().getFlags();
            // remove after fixing #28966
            if (!SymbolFlags.isFlagOn(flag, SymbolFlags.OPTIONAL) && !SymbolFlags.isFlagOn(flag,
                    SymbolFlags.REQUIRED) && initialValueEntries.get(fieldName) == null) {
                invalidTomlLines.add(tomlValue.location().lineRange());
                throw new ConfigException(CONFIG_TOML_DEFAULT_FILED_NOT_SUPPORTED, getLineRange(tomlValue), fieldName
                        , variableName);
            }
        }

        MappingInitialValueEntry.KeyValueEntry[] initialValues =
                new MappingInitialValueEntry.KeyValueEntry[initialValueEntries.size()];
        int count = 0;
        for (Map.Entry<String, Object> value : initialValueEntries.entrySet()) {
            initialValues[count] = new MappingInitialValueEntry.KeyValueEntry(StringUtils.fromString(value.getKey())
                    , value.getValue());
            count++;
        }
        return ValueCreator.createMapValue(recordType, initialValues);
    }

    private void validateRequiredField(Map<String, Object> initialValueEntries, RecordType recordType,
                                              String variableName, TomlNode tomlNode) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            if (SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.REQUIRED) &&
                    initialValueEntries.get(fieldName) == null) {
                invalidTomlLines.add(tomlNode.location().lineRange());
                throw new ConfigException(CONFIG_TOML_REQUIRED_FILED_NOT_PROVIDED, getLineRange(tomlNode), fieldName,
                                          recordType.toString(), variableName);
            }
        }
    }

    private BTable<BString, Object> retrieveTableValue(TomlNode tomlValue, String variableName, TableType tableType) {
        Type constraintType = tableType.getConstrainedType();
        if (tomlValue.kind() != getEffectiveTomlType(tableType, variableName)) {
            invalidTomlLines.add(tomlValue.location().lineRange());
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, getLineRange(tomlValue), variableName, tableType,
                                      getTomlTypeString(tomlValue));
        }
        visitedNodes.add(tomlValue);
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        int tableSize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries = new ListInitialValueEntry.ExpressionEntry[tableSize];
        String[] keys = tableType.getFieldNames();
        for (int i = 0; i < tableSize; i++) {
            if (keys != null) {
                validateKeyField(tableNodeList.get(i), keys, tableType, variableName);
            }
            Object value = retrieveValue(tableNodeList.get(i), variableName, constraintType);
            tableEntries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        ArrayValue tableData =
                new ArrayValueImpl(TypeCreator.createArrayType(constraintType), tableSize, tableEntries);
        ArrayValue keyNames = keys == null ? (ArrayValue) ValueCreator.createArrayValue(new BString[]{}) :
                (ArrayValue) StringUtils.fromStringArray(keys);
        if (constraintType.getTag() == TypeTags.INTERSECTION_TAG) {
            constraintType = ((IntersectionType) constraintType).getEffectiveType();
        }
        TableType type = TypeCreator.createTableType(constraintType, keys, true);
        return new TableValueImpl<>((BTableType) type, tableData, keyNames);
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

    private Object getBalValue(String variableName, Type type, TomlValueNode tomlValueNode) {
        Object tomlValue = ((TomlBasicValueNode<?>) tomlValueNode).getValue();
        int typeTag = type.getTag();
        if (typeTag == TypeTags.BYTE_TAG) {
            int value = ((Long) tomlValue).intValue();
            if (!isByteLiteral(value)) {
                invalidTomlLines.add(tomlValueNode.location().lineRange());
                throw new ConfigException(CONFIG_INVALID_BYTE_RANGE, getLineRange(tomlValueNode), variableName, value);
            }
            return value;
        }
        if (typeTag == TypeTags.DECIMAL_TAG) {
            return ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) tomlValue));
        }
        if (typeTag == TypeTags.STRING_TAG) {
            String stringVal = (String) tomlValue;
            return StringUtils.fromString(stringVal);
        }
        return tomlValue;
    }

    private TomlTableNode getModuleTomlNode(Module module, VariableKey key) {
        if (moduleTomlNodeMap.containsKey(module)) {
            TomlTableNode tomlTableNode = moduleTomlNodeMap.get(module);
            if (tomlTableNode != null || !key.isRequired()) {
                return tomlTableNode;
            }
        }
        TomlTableNode tomlTableNode = null;
        try {
            tomlTableNode = retrieveModuleNode(module, key.isRequired());
        } finally {
            moduleTomlNodeMap.put(module, tomlTableNode);
        }
        return tomlTableNode;
    }
}
