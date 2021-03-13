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

package io.ballerina.runtime.internal.configurable.providers.toml;

/**
 * Constants used by toml parser.
 *
 * @since 2.0.0
 */
public class ConfigTomlConstants {
    public static final String CONFIG_FILE_NAME = "Config.toml";
    public static final String DEFAULT_MODULE = ".";
    public static final String SUBMODULE_DELIMITER = ".";
    public static final String CONFIG_ENV_VARIABLE = "BALCONFIGFILE";
    public static final String CONFIG_SECRET_ENV_VARIABLE = "BALSECRETFILE";
    public static final String SECRET_FILE_NAME = "secret.txt";

    //Error messages
    public static final String INVALID_TOML_FILE = "invalid `" + CONFIG_FILE_NAME + "` file : ";
    public static final String INVALID_TOML_TYPE = "configurable variable '%s' is expected to be of type '%s', " +
            "but found '%s'";
    public static final String CONFIGURATION_NOT_SUPPORTED = "configurable variable '%s' with type '%s' is not " +
            "supported";
    public static final String INVALID_ADDITIONAL_FIELD_IN_RECORD =
            "additional field '%s' provided for configurable variable '%s' of record '%s' is not supported";
    public static final String FIELD_TYPE_NOT_SUPPORTED =
            "field type '%s' in configurable variable '%s' is not supported";
    public static final String REQUIRED_FIELD_NOT_PROVIDED = "value not provided for non-defaultable required field" +
            " '%s' of record '%s' in configurable variable '%s'";
    public static final String TABLE_KEY_NOT_PROVIDED = "value required for key '%s' of type '%s' in " +
            "configurable variable '%s'";
    public static final String INVALID_BYTE_RANGE = "value provided for byte variable '%s' is out of range. Expected " +
            "range is (0-255), found '%s'";
    public static final String INVALID_MODULE_STRUCTURE = "invalid module structure found for module '%s'. Please " +
            "provide the module name as '[%s]'";
    public static final String CONSTRAINT_TYPE_NOT_SUPPORTED = "table constraint type '%s' in configurable variable" +
            " '%s' is not supported";
    public static final String DEFAULT_FIELD_UNSUPPORTED = "defaultable readonly record field '%s' in configurable " +
            "variable '%s' is not supported";
    public static final String CONFIG_FILE_NOT_FOUND = "configuration file is not found in path '%s'";
    public static final String EMPTY_CONFIG_FILE = "an empty configuration file is found in path '%s'. Please " +
            "provide values for configurable variables";
    public static final String VALUE_NOT_PROVIDED = "value not provided for required configurable variable '%s'";

    private ConfigTomlConstants() {
    }
}
