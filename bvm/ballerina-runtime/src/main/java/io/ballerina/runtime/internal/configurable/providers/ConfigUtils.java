/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.internal.configurable.providers;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_INCOMPATIBLE_TYPE;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_UNION_VALUE_AMBIGUOUS_TARGET;

/*
 * Util class that contain methods that are common for env vars, cli configuration.
 */
public class ConfigUtils {

    private ConfigUtils() {
    }

    public static BString getFiniteValue(VariableKey key, BUnionType unionType, String value, String variable) {
        BString stringVal = StringUtils.fromString(value);
        List<Type> memberTypes = unionType.getMemberTypes();
        for (Type type : memberTypes) {
            if (((BFiniteType) type).valueSpace.contains(stringVal)) {
                return stringVal;
            }
        }
        throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, variable, key.variable,
                decodeIdentifier(unionType.toString()), value);
    }

    public static Object getFiniteBalValue(String strValue, BFiniteType finiteType, VariableKey key, String arg) {
        List<Object> matchingValues = new ArrayList<>();
        for (Object value : finiteType.getValueSpace()) {
            String stringValue = value.toString();
            if (stringValue.equals(strValue)) {
                matchingValues.add(value);
            }
        }
        if (matchingValues.size() == 1) {
            return matchingValues.get(0);
        }
        String typeName = decodeIdentifier(finiteType.toString());
        if (matchingValues.isEmpty()) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, arg, key.variable, typeName, strValue);
        }
        throw new ConfigException(CONFIG_UNION_VALUE_AMBIGUOUS_TARGET, arg, key.variable, typeName);
    }

    public static boolean containsUnsupportedMembers(BUnionType unionType) {
        for (Type memberType : unionType.getMemberTypes()) {
            if (!isSimpleSequenceType(TypeUtils.getImpliedType(memberType).getTag())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSimpleSequenceType(int tag) {
        return tag <= TypeTags.BOOLEAN_TAG || TypeTags.isXMLTypeTag(tag);
    }

    public static Object getUnionValue(VariableKey key, BUnionType unionType, String value, String arg) {
        List<Object> matchingValues = getConvertibleMemberValues(value, unionType);
        if (matchingValues.size() == 1) {
            return matchingValues.get(0);
        }
        String typeName = decodeIdentifier(unionType.toString());
        if (matchingValues.isEmpty()) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, arg, key.variable, typeName, value);
        }
        throw new ConfigException(CONFIG_UNION_VALUE_AMBIGUOUS_TARGET, arg, key.variable, typeName);
    }

    private static List<Object> getConvertibleMemberValues(String value, UnionType unionType) {
        List<Object> matchingValues = new ArrayList<>();
        for (Type type : unionType.getMemberTypes()) {
            switch (TypeUtils.getImpliedType(type).getTag()) {
                case TypeTags.BYTE_TAG:
                    convertAndGetValuesFromString(matchingValues, TypeConverter::stringToByte, value);
                    break;
                case TypeTags.INT_TAG:
                    convertAndGetValuesFromString(matchingValues, TypeConverter::stringToInt, value);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    convertAndGetValuesFromString(matchingValues, TypeConverter::stringToBoolean, value);
                    break;
                case TypeTags.FLOAT_TAG:
                    convertAndGetValuesFromString(matchingValues, TypeConverter::stringToFloat, value);
                    break;
                case TypeTags.DECIMAL_TAG:
                    convertAndGetValuesFromString(matchingValues, TypeConverter::stringToDecimal, value);
                    break;
                case TypeTags.STRING_TAG:
                    convertAndGetValuesFromString(matchingValues, StringUtils::fromString, value);
                    break;
                default:
                    convertAndGetValuesFromString(matchingValues, TypeConverter::stringToXml, value);
            }
        }
        return matchingValues;
    }

    private static void convertAndGetValuesFromString(List<Object> matchingValues,
                                                      Function<String, Object> convertFunc, String value) {
        Object unionValue;
        try {
            unionValue = convertFunc.apply(value);
        } catch (NumberFormatException | BError e) {
            return;
        }
        matchingValues.add(unionValue);
    }

}
