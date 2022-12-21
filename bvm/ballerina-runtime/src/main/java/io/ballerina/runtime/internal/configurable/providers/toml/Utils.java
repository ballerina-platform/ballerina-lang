/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.IntersectableReferenceType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.types.BAnydataType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlDoubleValueNodeNode;
import io.ballerina.toml.semantic.ast.TomlInlineTableValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_READONLY_ANYDATA;
import static io.ballerina.runtime.internal.ValueUtils.createReadOnlyXmlValue;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILE_NAME;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_UNION_VALUE_AMBIGUOUS_TARGET;

/**
 * Util methods required for configurable variables.
 *
 * @since 2.0.0
 */
public class Utils {

    private static final Type TYPE_READONLY_ANYDATA_INTERSECTION =
            new BIntersectionType(null, new Type[]{TYPE_READONLY_ANYDATA},
                    (IntersectableReferenceType) TYPE_READONLY_ANYDATA, 0, true);

    private Utils() {
    }

    static Object getTomlTypeString(TomlNode tomlNode) {
        switch (tomlNode.kind()) {
            case STRING:
                return "string";
            case INTEGER:
                return "int";
            case DOUBLE:
                return "float";
            case BOOLEAN:
                return "boolean";
            case ARRAY:
                return "array";
            case TABLE:
            case INLINE_TABLE:
                return "record";
            case TABLE_ARRAY:
                return "table";
            case KEY_VALUE:
                return getTomlTypeString(((TomlKeyValueNode) tomlNode).value());
            default:
                return "unsupported type";
        }
    }

    static Object getBalValueFromToml(TomlNode tomlNode, Set<TomlNode> visitedNodes, BUnionType unionType,
                                      Set<LineRange> invalidTomlLines, String variableName) {
        visitedNodes.add(tomlNode);
        switch (tomlNode.kind()) {
            case STRING:
                return validateAndGetStringValue((TomlStringValueNode) tomlNode, unionType, invalidTomlLines,
                        variableName);
            case INTEGER:
                return ((TomlLongValueNode) tomlNode).getValue();
            case DOUBLE:
                return validateAndGetDoubleValue((TomlDoubleValueNodeNode) tomlNode, unionType, invalidTomlLines,
                        variableName);
            case BOOLEAN:
                return ((TomlBooleanValueNode) tomlNode).getValue();
            case KEY_VALUE:
                return getBalValueFromToml(((TomlKeyValueNode) tomlNode).value(), visitedNodes, unionType,
                        invalidTomlLines, variableName);
            case ARRAY:
                return getAnydataArray((TomlArrayValueNode) tomlNode, visitedNodes, invalidTomlLines, variableName);
            case TABLE:
                return getAnydataMap((TomlTableNode) tomlNode, visitedNodes, invalidTomlLines, variableName);
            case TABLE_ARRAY:
                return validateAndGetTableArrayValue((TomlTableArrayNode) tomlNode, visitedNodes, unionType,
                        invalidTomlLines, variableName);
            case INLINE_TABLE:
                return getAnydataMap(((TomlInlineTableValueNode) tomlNode).toTable(), visitedNodes, invalidTomlLines,
                        variableName);
            default:
                // should not come here
                return null;
        }
    }

    private static Object getAnydataTable(TomlTableArrayNode tomlNode, Set<TomlNode> visitedNodes,
                                          Set<LineRange> invalidTomlLines, String variableName) {
        List<TomlTableNode> tableNodeList = tomlNode.children();
        int tableSize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries =
                new ListInitialValueEntry.ExpressionEntry[tableSize];
        int count = 0;
        for (TomlTableNode tomlTableNode : tableNodeList) {
            Object value = getBalValueFromToml(tomlTableNode, visitedNodes, (BAnydataType) TYPE_READONLY_ANYDATA,
                    invalidTomlLines, variableName);
            tableEntries[count++] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        ArrayValue tableData = new ArrayValueImpl(TypeCreator.createArrayType(TYPE_READONLY_ANYDATA_INTERSECTION,
                true), tableEntries);
        ArrayValue keyNames = (ArrayValue) StringUtils.fromStringArray(new String[0]);
        TableType tableType = TypeCreator.createTableType(TypeCreator.createMapType(TYPE_READONLY_ANYDATA_INTERSECTION,
                true), true);
        return new TableValueImpl<>(tableType, tableData, keyNames);
    }

    private static Object getAnydataMap(TomlTableNode tomlNode, Set<TomlNode> visitedNodes,
                                        Set<LineRange> invalidTomlLines, String variableName) {
        BMapInitialValueEntry[] initialValues = new BMapInitialValueEntry[tomlNode.entries().size()];
        int count = 0;
        for (Map.Entry<String, TopLevelNode> entry : tomlNode.entries().entrySet()) {
            initialValues[count++] = ValueCreator.createKeyFieldEntry(StringUtils.fromString(entry.getKey()),
                    getBalValueFromToml(entry.getValue(), visitedNodes, (BAnydataType) TYPE_READONLY_ANYDATA,
                            invalidTomlLines, variableName));
        }
        return ValueCreator.createMapValue(TypeCreator.createMapType(TYPE_READONLY_ANYDATA_INTERSECTION, true),
                initialValues);
    }

    private static Object getAnydataArray(TomlArrayValueNode tomlNode, Set<TomlNode> visitedNodes,
                                          Set<LineRange> invalidTomlLines, String variableName) {
        ListInitialValueEntry[] arrayValues = new ListInitialValueEntry[tomlNode.elements().size()];
        List<TomlValueNode> elements = tomlNode.elements();
        int count = 0;
        for (TomlValueNode tomlValueNode : elements) {
            arrayValues[count++] = new ListInitialValueEntry.ExpressionEntry(getBalValueFromToml(tomlValueNode,
                    visitedNodes, (BAnydataType) TYPE_READONLY_ANYDATA, invalidTomlLines, variableName));
        }
        return new ArrayValueImpl(TypeCreator.createArrayType(TYPE_READONLY_ANYDATA_INTERSECTION, true), arrayValues);
    }

    private static Object getMapAnydataArray(TomlTableArrayNode tomlNode, Set<TomlNode> visitedNodes,
                                             Set<LineRange> invalidTomlLines, String variableName) {
        ListInitialValueEntry[] arrayValues = new ListInitialValueEntry[tomlNode.children().size()];
        List<TomlTableNode> elements = tomlNode.children();
        int count = 0;
        for (TomlTableNode tomlValueNode : elements) {
            arrayValues[count++] = new ListInitialValueEntry.ExpressionEntry(getBalValueFromToml(tomlValueNode,
                    visitedNodes, (BAnydataType) TYPE_READONLY_ANYDATA, invalidTomlLines, variableName));
        }
        return new ArrayValueImpl(
                TypeCreator.createArrayType(TypeCreator.createMapType(TYPE_READONLY_ANYDATA_INTERSECTION), true),
                arrayValues);
    }

    static boolean checkEffectiveTomlType(TomlType kind, Type expectedType, String variableName) {
        switch (expectedType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
                return kind == TomlType.INTEGER;
            case TypeTags.BOOLEAN_TAG:
                return kind == TomlType.BOOLEAN;
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
                return kind == TomlType.DOUBLE;
            case TypeTags.STRING_TAG:
            case TypeTags.UNION_TAG:
            case TypeTags.XML_ATTRIBUTES_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TAG:
            case TypeTags.XML_TEXT_TAG:
                return kind == TomlType.STRING;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                return kind == TomlType.ARRAY;
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return kind == TomlType.INLINE_TABLE || kind == TomlType.TABLE;
            case TypeTags.TABLE_TAG:
                return kind == TomlType.TABLE_ARRAY || kind == TomlType.ARRAY;
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) expectedType).getEffectiveType();
                return checkEffectiveTomlType(kind, effectiveType, variableName);
            default:
                throw new ConfigException(CONFIG_TYPE_NOT_SUPPORTED, variableName, expectedType.toString());
        }
    }

    static boolean isSimpleType(int typeTag) {
        return typeTag <= TypeTags.BOOLEAN_TAG;
    }

    static String getModuleKey(Module module) {
        return module.getOrg() + "." + module.getName();
    }

    static String getLineRange(TomlNode node) {
        if (node.location() == null) {
            return CONFIG_FILE_NAME;
        }
        LineRange oneBasedLineRange = getOneBasedLineRange(node.location().lineRange());
        return oneBasedLineRange.filePath() + ":" + oneBasedLineRange;
    }

    static LineRange getOneBasedLineRange(LineRange lineRange) {
        return LineRange.from(
                lineRange.filePath(),
                LinePosition.from(lineRange.startLine().line() + 1, lineRange.startLine().offset() + 1),
                LinePosition.from(lineRange.endLine().line() + 1, lineRange.endLine().offset() + 1));
    }

    public static Type getTypeFromTomlValue(TomlNode tomlNode) {
        switch (tomlNode.kind()) {
            case STRING:
                return PredefinedTypes.TYPE_STRING;
            case INTEGER:
                return PredefinedTypes.TYPE_INT;
            case DOUBLE:
                return PredefinedTypes.TYPE_FLOAT;
            case BOOLEAN:
                return PredefinedTypes.TYPE_BOOLEAN;
            case KEY_VALUE:
                return getTypeFromTomlValue(((TomlKeyValueNode) tomlNode).value());
            case ARRAY:
                if (containsInlineTable((TomlArrayValueNode) tomlNode)) {
                    return TypeCreator.createArrayType(TypeCreator.createMapType(TYPE_ANYDATA), true);
                }
                return TypeCreator.createArrayType(TYPE_ANYDATA, true);
            case TABLE:
            case INLINE_TABLE:
                return TypeCreator.createMapType(TYPE_ANYDATA, true);
            case TABLE_ARRAY:
                return TypeCreator.createArrayType(TypeCreator.createMapType(TYPE_ANYDATA), true);
            default:
                // should not come here
                return null;
        }
    }

    private static boolean containsInlineTable(TomlArrayValueNode tomlNode) {
        for (TomlValueNode valueNode : tomlNode.elements()) {
            if (valueNode.kind() == TomlType.INLINE_TABLE) {
                return true;
            }
        }
        return false;
    }

    public static Field createAdditionalField(RecordType recordType, String fieldName, TomlNode value) {
        Type restFieldType = recordType.getRestFieldType();
        if (!isAnyDataType(restFieldType)) {
            return TypeCreator.createField(restFieldType, fieldName, SymbolFlags.READONLY);
        } else {
            return TypeCreator.createField(getTypeFromTomlValue(value), fieldName, SymbolFlags.READONLY);
        }
    }

    private static Object validateAndGetTableArrayValue(TomlTableArrayNode tomlNode, Set<TomlNode> visitedNodes,
                                                        BUnionType unionType, Set<LineRange> invalidTomlLines,
                                                        String variableName) {
        boolean hasTable = containsType(unionType, TypeTags.TABLE_TAG);
        boolean hasMapArray = containsMapArray(unionType);
        if (hasTable && hasMapArray && unionType.getTag() != TypeTags.ANYDATA_TAG) {
            throwMemberAmbiguityError(unionType, invalidTomlLines, variableName, tomlNode);
        }
        if (hasMapArray) {
            return getMapAnydataArray(tomlNode, visitedNodes, invalidTomlLines, variableName);
        }
        return getAnydataTable(tomlNode, visitedNodes, invalidTomlLines, variableName);
    }

    private static Object validateAndGetDoubleValue(TomlDoubleValueNodeNode tomlNode, BUnionType unionType,
                                                    Set<LineRange> invalidTomlLines, String variableName) {
        boolean hasDecimal = containsType(unionType, TypeTags.DECIMAL_TAG);
        boolean hasFloat = containsType(unionType, TypeTags.FLOAT_TAG);
        if (hasDecimal && hasFloat && unionType.getTag() != TypeTags.ANYDATA_TAG) {
            throwMemberAmbiguityError(unionType, invalidTomlLines, variableName, tomlNode);
        }
        Double value = tomlNode.getValue();
        if (hasFloat) {
            return value;
        }
        return ValueCreator.createDecimalValue(BigDecimal.valueOf(value));
    }

    private static Object validateAndGetFiniteDoubleValue(TomlDoubleValueNodeNode tomlNode, BFiniteType finiteType,
                                                          Set<LineRange> invalidTomlLines, String variableName) {
        Double value = tomlNode.getValue();
        boolean decimalValueFound = checkDoubleValue(finiteType, TypeTags.DECIMAL_TAG, value);
        boolean floatValueFound = checkDoubleValue(finiteType, TypeTags.FLOAT_TAG, value);
        if (decimalValueFound && floatValueFound) {
            throwMemberAmbiguityError(finiteType, invalidTomlLines, variableName, tomlNode);
        }
        if (floatValueFound) {
            return value;
        }
        return ValueCreator.createDecimalValue(BigDecimal.valueOf(value));
    }

    private static boolean checkDoubleValue(BFiniteType type, int tag, double doubleValue) {
        for (Object value : type.getValueSpace()) {
            if (TypeUtils.getReferredType(TypeChecker.getType(value)).getTag() == tag) {
                if (tag == TypeTags.DECIMAL_TAG) {
                    return doubleValue == ((DecimalValue) value).floatValue();
                } else {
                    return doubleValue == (double) value;
                }
            }
        }
        return false;
    }

    private static Object validateAndGetStringValue(TomlStringValueNode tomlNode, BUnionType unionType,
                                                    Set<LineRange> invalidTomlLines, String variableName) {
        boolean hasString = containsType(unionType, TypeTags.STRING_TAG);
        boolean hasXml = containsXMLType(unionType);
        if (hasString && hasXml && unionType.getTag() != TypeTags.ANYDATA_TAG) {
            throwMemberAmbiguityError(unionType, invalidTomlLines, variableName, tomlNode);
        }
        String value = tomlNode.getValue();
        if (hasString || SymbolFlags.isFlagOn(unionType.getFlags(), SymbolFlags.ENUM) || containsType(unionType,
                TypeTags.FINITE_TYPE_TAG)) {
            return StringUtils.fromString(value);
        }
        return createReadOnlyXmlValue(value);
    }

    private static void throwMemberAmbiguityError(Type type, Set<LineRange> invalidTomlLines,
                                                  String variableName, TomlNode tomlNode) {
        invalidTomlLines.add(tomlNode.location().lineRange());
        throw new ConfigException(CONFIG_UNION_VALUE_AMBIGUOUS_TARGET, getLineRange(tomlNode), variableName,
                decodeIdentifier(type.toString()));
    }

    private static boolean containsType(BUnionType unionType, int tag) {
        for (Type type : unionType.getMemberTypes()) {
            Type effectiveType = getEffectiveType(type);
            int typeTag = effectiveType.getTag();
            if (typeTag == TypeTags.FINITE_TYPE_TAG) {
                for (Object obj : ((FiniteType) effectiveType).getValueSpace()) {
                    if (TypeUtils.getReferredType(TypeChecker.getType(obj)).getTag() == tag) {
                        return true;
                    }
                }
            }
            if (typeTag == tag) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsMapArray(BUnionType unionType) {
        for (Type type : unionType.getMemberTypes()) {
            Type effectiveType = getEffectiveType(type);
            if (effectiveType.getTag() == TypeTags.ARRAY_TAG &&
                    isMappingType(getEffectiveType(((ArrayType) effectiveType).getElementType()).getTag())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsXMLType(BUnionType unionType) {
        for (Type type : unionType.getMemberTypes()) {
            if (isXMLType(type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnyDataType(Type restFieldType) {
        return getEffectiveType(restFieldType).getTag() == TypeTags.ANYDATA_TAG;
    }

    static boolean isXMLType(Type type) {
        return TypeTags.isXMLTypeTag(getEffectiveType(type).getTag());
    }

    static Type getEffectiveType(Type type) {
        switch (type.getTag()) {
            case TypeTags.INTERSECTION_TAG:
                return ((IntersectionType) type).getEffectiveType();
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return getEffectiveType(((ReferenceType) type).getReferredType());
            default:
                return type;
        }
    }

    private static boolean isMappingType(int typeTag) {
        return typeTag == TypeTags.MAP_TAG || typeTag == TypeTags.RECORD_TYPE_TAG;
    }

    static boolean containsMapType(List<Type> memberTypes) {
        for (Type type : memberTypes) {
            if (isMappingType(getEffectiveType(type).getTag())) {
                return true;
            }
        }
        return false;
    }

    public static Object getFiniteBalValue(TomlNode tomlNode, Set<TomlNode> visitedNodes,
                                           BFiniteType finiteType, Set<LineRange> invalidTomlLines,
                                           String variableName) {
        visitedNodes.add(tomlNode);
        switch (tomlNode.kind()) {
            case STRING:
                return StringUtils.fromString(((TomlStringValueNode) tomlNode).getValue());
            case INTEGER:
                return ((TomlLongValueNode) tomlNode).getValue();
            case DOUBLE:
                return validateAndGetFiniteDoubleValue((TomlDoubleValueNodeNode) tomlNode, finiteType, invalidTomlLines,
                        variableName);
            case BOOLEAN:
                return ((TomlBooleanValueNode) tomlNode).getValue();
            case KEY_VALUE:
                return getFiniteBalValue(((TomlKeyValueNode) tomlNode).value(), visitedNodes, finiteType,
                        invalidTomlLines, variableName);
            default:
                // should not come here
                return null;
        }

    }

    static Type getTupleElementType(List<Type> tupleTypes, int i, TupleType tupleType) {
        Type restType = tupleType.getRestType();
        if (i >= tupleTypes.size() && restType != null) {
            return restType;
        }
        return tupleTypes.get(i);
    }

    static TomlNode getValueFromKeyValueNode(TomlNode value) {
        if (value.kind() == TomlType.KEY_VALUE) {
            return ((TomlKeyValueNode) value).value();
        }
        return value;
    }
}
