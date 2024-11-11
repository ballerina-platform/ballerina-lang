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
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.toml.semantic.ast.TomlTableNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Toml parser that reads from file content for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlFileProvider extends TomlProvider {

    private final Path configPath;

    public TomlFileProvider(Module rootModule, Path configPath, Set<Module> moduleSet) {
        super(rootModule, moduleSet);
        this.configPath = configPath;
    }

    @Override
    public void initialize() {
        super.tomlNode = getConfigTomlData(configPath);
        super.initialize();
    }

    private TomlTableNode getConfigTomlData(Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            throw new ConfigException(ErrorCodes.CONFIG_TOML_FILE_NOT_FOUND, configFilePath);
        }
        return new ConfigToml(configFilePath).tomlAstNode();
    }

}
