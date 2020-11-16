/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.configurable;

import com.moandjiezana.toml.Toml;
import io.ballerina.runtime.internal.configurable.exceptions.TomlException;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.values.BmpStringValue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlParser {

    public static final String CONFIG_FILE_NAME = "config-variables.toml";
    static final Path PROJECT_PATH = Paths.get(RuntimeUtils.USER_DIR);

    private static Toml getConfigurationData() {
        Path configPath = PROJECT_PATH.resolve(CONFIG_FILE_NAME);
        if (!Files.exists(configPath)) {
//            Ignoring the file not found exception for now
//            throw new TomlException("Configuration toml file `" + CONFIG_FILE_NAME + "` is not found");
            return null;
        }
        return new Toml().read(configPath.toFile());
    }

    public static void populateConfigMap(String version) throws TomlException {
        //TODO: Validations over org name/module name/identifier
        Map<VariableKey, Object> configurableMap = ConfigurableMapHolder.getConfigurationMap();
        Toml toml = getConfigurationData();
        if (toml != null && !toml.isEmpty()) {
            for (Map.Entry<String, Object> organizaion : toml.entrySet()) {
                String orgName = organizaion.getKey();
                Toml modules = (Toml) organizaion.getValue();
                for (Map.Entry<String, Object> module : modules.entrySet()) {
                    String moduleName = module.getKey();
                    Toml variables = (Toml) module.getValue();
                    for (Map.Entry<String, Object> variable : variables.entrySet()) {
                        VariableKey key = new VariableKey(orgName, moduleName, version, variable.getKey());
                        Object value = getConfiguredValue(variable.getValue());
                        configurableMap.put(key, value);
                    }
                }
            }
        }
    }

    private static Object getConfiguredValue(Object value) throws TomlException {
        if (value instanceof Date) {
            throw new TomlException("Toml primitive type `Date` is not supported by Ballerina");
        }
        if (value instanceof String) {
            return new BmpStringValue((String) value);
        }
      //TODO: Remove the `instanceof` checks when we have configurable information
        return value;
    }

}
