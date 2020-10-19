/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.config;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.config.ConfigRegistry;

import java.util.List;
import java.util.Map;

/**
 * Extern function ballerina.config:get.
 *
 * @since 0.970.0-alpha3
 */
public class GetConfig {
    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();
    private static final MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_ANYDATA, true);
    private static final ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_ANYDATA, -1, true);

    public static Object get(BString configKey, BString type) {
        try {
            switch (type.getValue()) {
                case "STRING":
                    return StringUtils.fromString(configRegistry.getAsString(configKey.getValue()));
                case "INT":
                    return configRegistry.getAsInt(configKey.getValue());
                case "FLOAT":
                    return configRegistry.getAsFloat(configKey.getValue());
                case "BOOLEAN":
                    return configRegistry.getAsBoolean(configKey.getValue());
                case "MAP":
                    return buildBMap(configRegistry.getAsMap(configKey.getValue()));
                case "ARRAY":
                    return buildBArray(configRegistry.getAsArray(configKey.getValue()));
                default:
                    throw new IllegalStateException("invalid value type: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw ErrorCreator.createError(StringUtils.fromString(
                    "error occurred while trying to retrieve the value; " + e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private static BMap<BString, Object> buildBMap(Map<String, Object> section) {
        BMapInitialValueEntry[] keyValues = new BMapInitialValueEntry[section.size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            BMapInitialValueEntry keyValue = ValueCreator
                    .createKeyFieldEntry(StringUtils.fromString(entry.getKey()), getConvertedValue(entry.getValue()));
            keyValues[i] = keyValue;
            i++;
        }
        return ValueCreator.createMapValue(mapType, keyValues);
    }

    private static BArray buildBArray(List value) {
        Object[] convertedValues = new Object[value.size()];
        for (Object entry : value) {
            convertedValues[value.indexOf(entry)] = getConvertedValue(entry);
        }
        return ValueCreator.createArrayValue(convertedValues, arrayType);
    }

    @SuppressWarnings("unchecked")
    private static Object getConvertedValue(Object obj) {
        if (obj instanceof Long || obj instanceof Double || obj instanceof Boolean) {
            return obj;
        } else if (obj instanceof Map) {
            return buildBMap((Map<String, Object>) obj);
        } else if (obj instanceof List) {
            return buildBArray((List) obj);
        }
        return StringUtils.fromString(String.valueOf(obj));
    }
}
