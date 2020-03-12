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
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BArray;
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
    private static final String LOOKUP_ERROR_REASON = "{ballerina/config}LookupError";

    public static Object get(String configKey, String type) {
        try {
            switch (type) {
                case "STRING":
                    return configRegistry.getAsString(configKey);
                case "INT":
                    return configRegistry.getAsInt(configKey);
                case "FLOAT":
                    return configRegistry.getAsFloat(configKey);
                case "BOOLEAN":
                    return configRegistry.getAsBoolean(configKey);
                case "MAP":
                    return buildMapValue(configRegistry.getAsMap(configKey));
                case "ARRAY":
                    return buildArrayValue(configRegistry.getAsArray(configKey));
                default:
                    throw new IllegalStateException("invalid value type: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw BallerinaErrors.createError(LOOKUP_ERROR_REASON, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static MapValue buildMapValue(Map<String, Object> section) {
        MapValue map = new MapValueImpl<String, Object>(new BMapType(BTypes.typeAnydata));
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            map.put(entry.getKey(), getConvertedValue(entry.getValue()));
        }
        return map;
    }

    private static BArray buildArrayValue(List value) {
        Object[] convertedValues = new Object[value.size()];
        for (Object entry : value) {
            convertedValues[value.indexOf(entry)] = getConvertedValue(entry);
        }
        return BValueCreator.createArrayValue(convertedValues, new BArrayType(BTypes.typeAnydata));
    }

    @SuppressWarnings("unchecked")
    private static Object getConvertedValue(Object obj) {
        if (obj instanceof String || obj instanceof Long || obj instanceof Double || obj instanceof Boolean) {
            return obj;
        } else if (obj instanceof Map) {
            return buildMapValue((Map<String, Object>) obj);
        } else if (obj instanceof List) {
            return buildArrayValue((List) obj);
        }
        return String.valueOf(obj);
    }
}
