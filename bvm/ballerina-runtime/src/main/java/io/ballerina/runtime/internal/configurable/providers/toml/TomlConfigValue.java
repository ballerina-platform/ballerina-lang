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

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.toml.semantic.ast.TomlNode;

/**
 *  This class represents a configurable value retrieved from TOML configuration.
 *
 *  @since 2.0.0
 */
public class TomlConfigValue implements ConfigValue {
    private static final ConfigValueCreator valueCreator = new ConfigValueCreator();
    private Object value;
    private Type type;

    public TomlConfigValue(Object value, Type type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public Object getValue() {
        if (value instanceof TomlNode) {
            return valueCreator.createValue((TomlNode) value, type);
        }
        return value;
    }
}
