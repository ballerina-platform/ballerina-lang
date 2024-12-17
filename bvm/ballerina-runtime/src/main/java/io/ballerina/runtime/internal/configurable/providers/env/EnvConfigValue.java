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

package io.ballerina.runtime.internal.configurable.providers.env;

import io.ballerina.runtime.internal.configurable.ConfigValue;

/**
 *  This class represents a configurable value retrieved from environment variable configuration.
 *
 *  @since 2201.9.0
 */
public class EnvConfigValue implements ConfigValue {
    private final Object value;

    public EnvConfigValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
