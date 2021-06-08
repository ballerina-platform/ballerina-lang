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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILE_NAME;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TYPE_NOT_SUPPORTED;

/**
 * Util methods required for configurable variables.
 *
 * @since 2.0.0
 */
public class Utils {

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
                return "record";
            case TABLE_ARRAY:
                return "table";
            case KEY_VALUE:
                return getTomlTypeString(((TomlKeyValueNode) tomlNode).value());
            default:
                return "unsupported type";
        }
    }

    static TomlType getEffectiveTomlType(Type expectedType, String variableName) {
        switch (expectedType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
                return TomlType.INTEGER;
            case TypeTags.BOOLEAN_TAG:
                return TomlType.BOOLEAN;
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
                return TomlType.DOUBLE;
            case TypeTags.STRING_TAG:
            case TypeTags.UNION_TAG:
                return TomlType.STRING;
            case TypeTags.ARRAY_TAG:
                return TomlType.ARRAY;
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return TomlType.TABLE;
            case TypeTags.TABLE_TAG:
                return TomlType.TABLE_ARRAY;
            case TypeTags.XML_ATTRIBUTES_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TAG:
            case TypeTags.XML_TEXT_TAG:
                throw new ConfigException(CONFIG_TOML_TYPE_NOT_SUPPORTED, variableName, expectedType.toString());
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) expectedType).getEffectiveType();
                return getEffectiveTomlType(effectiveType, variableName);
            default:
                throw new ConfigException(CONFIG_TYPE_NOT_SUPPORTED, variableName, expectedType.toString());
        }
    }

    static boolean isPrimitiveType(int typeTag) {
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

    static Type getMutableType(Type type) {
        switch (type.getTag()) {
            case TypeTags.ARRAY_TAG:
                Type elementType = ((ArrayType) type).getElementType();
                return TypeCreator.createArrayType(getMutableType(elementType));
            case TypeTags.MAP_TAG:
                MapType mapType = (MapType) type;
                return TypeCreator.createMapType(getMutableType(mapType.getConstrainedType()));
            case TypeTags.TABLE_TAG:
                TableType tableType = (TableType) type;
                String[] keys = tableType.getFieldNames() == null ? new String[]{} : tableType.getFieldNames();
                return TypeCreator.createTableType(getMutableType(tableType.getConstrainedType()), keys,
                        false);
            case TypeTags.INTERSECTION_TAG:
                return getMutableType(((BIntersectionType) type).getConstituentTypes().get(0));
            default:
                return type;
        }
    }
}
