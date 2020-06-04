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

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;

import java.util.List;
import java.util.Map;

/**
 * Extern function ballerina.config:get.
 *
 * @since 0.970.0-alpha3
 */
public class GetConfig {
    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();
    private static final BMapType mapType = new BMapType(BTypes.typeAnydata);
    private static final BArrayType arrayType = new BArrayType(BTypes.typeAnydata);

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
                    return buildMapValue(configRegistry.getAsMap(configKey.getValue()));
                case "ARRAY":
                    return buildArrayValue(configRegistry.getAsArray(configKey.getValue()));
                default:
                    throw new IllegalStateException("invalid value type: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw BallerinaErrors.createError(StringUtils.fromString(
                    "error occurred while trying to retrieve the value; " + e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private static MapValue<BString, Object> buildMapValue(Map<String, Object> section) {
        MapValue<BString, Object> map = new MapValueImpl<>(mapType);
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            map.put(StringUtils.fromString(entry.getKey()), getConvertedValue(entry.getValue()));
        }
        return map;
    }

    private static BArray buildArrayValue(List value) {
        Object[] convertedValues = new Object[value.size()];
        for (Object entry : value) {
            convertedValues[value.indexOf(entry)] = getConvertedValue(entry);
        }
        return BValueCreator.createArrayValue(convertedValues, arrayType);
    }

    @SuppressWarnings("unchecked")
    private static Object getConvertedValue(Object obj) {
        if (obj instanceof Long || obj instanceof Double || obj instanceof Boolean) {
            return obj;
        } else if (obj instanceof Map) {
            return buildMapValue((Map<String, Object>) obj);
        } else if (obj instanceof List) {
            return buildArrayValue((List) obj);
        }
        return StringUtils.fromString(String.valueOf(obj));
    }
}
