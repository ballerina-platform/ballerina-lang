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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.Map;

/**
 * Extern function ballerina.config:get.
 *
 * @since 0.970.0-alpha3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "config",
        functionName = "get",
        args = {@Argument(name = "key", type = TypeKind.STRING), @Argument(name = "vType", type = TypeKind.RECORD)}
)
public class GetConfig extends BlockingNativeCallableUnit {

    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();

    @Override
    public void execute(Context context) {
        String configKey = context.getStringArgument(0);
        BString type = (BString) context.getNullableRefArgument(0);

        // TODO: Add a try-catch
        switch (type.stringValue()) {
            case "STRING":
                String val = configRegistry.getAsString(configKey);
                context.setReturnValues(val != null ? new BString(val) : null);
                break;
            case "INT":
                context.setReturnValues(new BInteger(configRegistry.getAsInt(configKey)));
                break;
            case "FLOAT":
                context.setReturnValues(new BFloat(configRegistry.getAsFloat(configKey)));
                break;
            case "BOOLEAN":
                context.setReturnValues(new BBoolean(configRegistry.getAsBoolean(configKey)));
                break;
            case "MAP":
                context.setReturnValues(buildBMap(configRegistry.getAsMap(configKey)));
                break;
            default:
                throw new IllegalStateException("invalid value type: " + type.stringValue());
        }
    }

    private BMap buildBMap(Map<String, Object> section) {
        BMap map = new BMap();

        section.entrySet().forEach(entry -> {
            Object val = entry.getValue();
            if (val instanceof String) {
                map.put(entry.getKey(), new BString((String) val));
            } else if (val instanceof Long) {
                map.put(entry.getKey(), new BInteger((Long) val));
            } else if (val instanceof Double) {
                map.put(entry.getKey(), new BFloat((Double) val));
            } else if (val instanceof Boolean) {
                map.put(entry.getKey(), new BBoolean((Boolean) val));
            }
        });

        return map;
    }

    public static Object get(Strand strand, String configKey, Object type) {

        // TODO: Add a try-catch
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
