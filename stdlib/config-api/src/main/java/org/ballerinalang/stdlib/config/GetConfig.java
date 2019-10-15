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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.Map;

/**
 * Extern function ballerina.config:get.
 *
 * @since 0.970.0-alpha3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "config",
        functionName = "get"
)
public class GetConfig {

    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();
    private static final String lookupErrReason = "{ballerina/config}LookupError";

    public static Object get(Strand strand, String configKey, Object type) {

        try {
            switch (type.toString()) {
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
                default:
                    throw new IllegalStateException("invalid value type: " + type.toString());
            }
        } catch (IllegalArgumentException e) {
            throw BallerinaErrors.createError(lookupErrReason, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static MapValue buildMapValue(Map<String, Object> section) {
        MapValue map = new MapValueImpl<String, Object>();

        section.forEach((key, val) -> {
            if (val instanceof String || val instanceof Long || val instanceof Double || val instanceof Boolean) {
                map.put(key, val);
            }
        });
        return map;
    }
}
