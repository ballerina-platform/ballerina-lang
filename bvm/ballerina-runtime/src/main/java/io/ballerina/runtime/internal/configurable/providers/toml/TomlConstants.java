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

import io.ballerina.runtime.internal.util.RuntimeUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constants used by toml parser.
 *
 * @since 2.0.0
 */
public class TomlConstants {
    public static final String CONFIG_FILE_NAME = "Config.toml";
    public static final String ENV_VAR_PREFIX = "BAL_CONFIG_VAR_";
    public static final String CONFIG_FILES_ENV_VARIABLE = "BAL_CONFIG_FILES";
    public static final String CONFIG_DATA_ENV_VARIABLE = "BAL_CONFIG_DATA";
    public static final String MODULES_ROOT = "modules";
    public static final String TEST_DIR_NAME = "tests";
    public static final Path DEFAULT_CONFIG_PATH = Paths.get(RuntimeUtils.USER_DIR, CONFIG_FILE_NAME);

    private TomlConstants() {
    }
}
