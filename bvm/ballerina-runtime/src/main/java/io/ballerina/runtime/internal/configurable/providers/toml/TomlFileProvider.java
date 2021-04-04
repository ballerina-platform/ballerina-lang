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

import io.ballerina.toml.semantic.ast.TomlTableNode;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILE_NOT_FOUND;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.EMPTY_CONFIG_FILE;

/**
 * Toml parser that reads from file content for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlFileProvider extends TomlProvider {

    private final Path configPath;

    public TomlFileProvider(Path configPath) {
        this.configPath = configPath;
    }

    @Override
    public void initialize() {
        super.tomlNode = getConfigTomlData(configPath);
    }

    private TomlTableNode getConfigTomlData(Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            throw new TomlConfigException(String.format(CONFIG_FILE_NOT_FOUND, configFilePath));
        }
        ConfigToml configToml = new ConfigToml(configFilePath);
        TomlTableNode rootNode = configToml.tomlAstNode();
        if (rootNode.entries().isEmpty()) {
            throw new TomlConfigException(String.format(EMPTY_CONFIG_FILE, configFilePath));
        }
        return rootNode;
    }

}
