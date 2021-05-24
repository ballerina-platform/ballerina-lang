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
import io.ballerina.toml.api.Toml;

import java.util.Set;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_DATA_ENV_VARIABLE;

/**
 * Toml parser that reads from text content for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlContentProvider extends TomlProvider {

    private final String configContent;

    public TomlContentProvider(Module rootModule, String configContent, Set<Module> moduleSet) {
        super(rootModule, moduleSet);
        this.configContent = configContent;
    }

    @Override
    public void initialize() {
        if (configContent.isEmpty()) {
            return;
        }
        super.tomlNode = Toml.read(configContent, CONFIG_DATA_ENV_VARIABLE).rootNode();
        super.initialize();
    }

}
