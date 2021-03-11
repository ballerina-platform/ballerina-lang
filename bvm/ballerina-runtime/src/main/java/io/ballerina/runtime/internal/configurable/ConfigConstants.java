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

package io.ballerina.runtime.internal.configurable;

/**
 * Constants used by configs.
 *
 * @since 2.0.0
 */
public class ConfigConstants {

    public static final String CONFIG_FILE_NAME = "Config.toml";

    public static final String CONFIG_ENV_VARIABLE = "BALCONFIGFILE";

    public static final String CONFIG_SECRET_ENV_VARIABLE = "BALSECRETFILE";

    public static final String CONFIGURATION_NOT_SUPPORTED = "configurable variable '%s' with type '%s' is not " +
            "supported";

    private ConfigConstants() {
    }
}
