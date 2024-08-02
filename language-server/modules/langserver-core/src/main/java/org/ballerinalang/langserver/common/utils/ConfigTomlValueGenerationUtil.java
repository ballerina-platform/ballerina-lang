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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Util used to generate value entries for Config.toml keys.
 *
 * @since 2201.10.0
 */
public final class ConfigTomlValueGenerationUtil {

    private ConfigTomlValueGenerationUtil() {
    }

    /**
     * Get {@link TomlEntryValue} for a configurable {@link TypeSymbol}.
     *
     * @param type Type symbol of the configurable
     * @param basicType union of basic types
     * @param anydataOrJson union of anydata and json
     * @param confName name of the configurable
     * @return value string wrapped in {@link  TomlEntryValue}
     */
    public static TomlEntryValue getDefaultValueStr(TypeSymbol type, TypeSymbol basicType,
                                                    TypeSymbol anydataOrJson, String confName) {
        switch (type.typeKind()) {
            case BOOLEAN -> {
                return new TomlEntryValue("false", true);
            }
            case BYTE, INT, INT_SIGNED8, INT_SIGNED32, INT_UNSIGNED8, INT_UNSIGNED16, INT_UNSIGNED32, INT_SIGNED16 -> {
                return new TomlEntryValue("0", true);
            }
            case FLOAT, DECIMAL -> {
                return new TomlEntryValue("0.0", true);
            }
            case STRING, XML, ANYDATA, JSON, SINGLETON -> {
                return new TomlEntryValue("\"\"", true);
            }
            case TUPLE -> {
                return new TomlEntryValue("[]", true);
            }
            case ARRAY -> {
                ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) type;
                TypeSymbol memberType = CommonUtil.getRawType(arrayTypeSymbol.memberTypeDescriptor());
                if (memberType.typeKind() == TypeDescKind.UNION) {
                    memberType = ((UnionTypeSymbol) memberType).memberTypeDescriptors().get(0);
                }
                if (memberType.subtypeOf(basicType) || basicType.subtypeOf(anydataOrJson)) {
                    return new TomlEntryValue("[]", true);
                }
                return new TomlEntryValue(String.format("[[%s]]%n", confName), false); // records and maps
            }
            case MAP -> {
                return new TomlEntryValue(String.format("[%s]%n", confName), false);
            }
            case RECORD -> {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(String.format("[%s]%n", confName));
                getRecordFields((RecordTypeSymbol) type, stringBuilder, "");
                return new TomlEntryValue(stringBuilder.toString(), false);
            }
            case TABLE -> {
                return new TomlEntryValue(String.format("[[%s]]%n", confName), false);
            }
            case UNION -> {
                return  getDefaultValueStr(((UnionTypeSymbol) type).memberTypeDescriptors().get(0),
                        basicType, anydataOrJson, confName);
            }
            case TYPE_REFERENCE -> {
                return getDefaultValueStr(CommonUtil.getRawType(type), basicType, anydataOrJson, confName);
            }
            default -> {
                return new TomlEntryValue("", true);
            }
        }
    }

    private static StringBuilder getRecordFields(RecordTypeSymbol recordTypeSymbol, StringBuilder builder,
                                                 String prefix) {
        List<RawTypeSymbolWrapper<RecordTypeSymbol>> recordTypeSymbols =
                RecordUtil.getRecordTypeSymbols(recordTypeSymbol);

        List<RecordFieldSymbol> validFields = new ArrayList<>();
        for (RawTypeSymbolWrapper<RecordTypeSymbol> symbol : recordTypeSymbols) {
            validFields.addAll(RecordUtil.getRecordFields(symbol, Collections.emptyList()).values());
        }
        for (RecordFieldSymbol recordFieldSymbol : validFields) {
            TypeSymbol typeSymbol = CommonUtil.getRawType(recordFieldSymbol.typeDescriptor());
            if (typeSymbol.typeKind() == TypeDescKind.RECORD) {
                String nestedPrefix = prefix + recordFieldSymbol.getName().get() + ".";
                getRecordFields((RecordTypeSymbol) typeSymbol, builder, nestedPrefix);
            } else {
                builder.append(String.format("%s%s = %s%n", prefix,
                        recordFieldSymbol.getName().get(), getValueForObjectKey(typeSymbol)));
            }
        }
        return builder;
    }

    private static String getValueForObjectKey(TypeSymbol type) {
        switch (type.typeKind()) {
            case BOOLEAN -> {
                return "false";
            }
            case BYTE, INT, INT_SIGNED8, INT_SIGNED32, INT_UNSIGNED8, INT_UNSIGNED16, INT_UNSIGNED32, INT_SIGNED16 -> {
                return "0";
            }
            case FLOAT, DECIMAL -> {
                return "0.0";
            }
            case STRING, XML, ANYDATA, JSON, SINGLETON -> {
                return "\"\"";
            }
            case TUPLE, ARRAY, TABLE -> {
                return "[]";
            }
            case MAP -> {
                return "{}";
            }
            default -> {
                return "";
            }
        }
    }

    /**
     * Wrapper for toml entry value that specifies whether it's for a key or not.
     *
     * @param value entry value
     * @param keyValue is the entry for a key
     */
    public record TomlEntryValue(String value, boolean keyValue) {
    }
}
