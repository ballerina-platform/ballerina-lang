/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.packerina;

import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.parser.SettingsProcessor;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;

/**
 * This class provides util methods for all Ballerina cli commands.
 *
 * @since 0.970.0
 */
public class CliUtils {

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return settings object
     */
    static Settings readSettings() {
        String tomlFilePath = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.SETTINGS_FILE_NAME)
                                       .toString();
        try {
            return SettingsProcessor.parseTomlContentFromFile(tomlFilePath);
        } catch (IOException e) {
            return new Settings();
        }
    }

    /**
     * Read proxy configurations from the SettingHeaders.toml file.
     *
     * @return array with proxy configurations
     */
    static Proxy readProxyConfigurations() {
        Settings settings = readSettings();
        return settings.getProxy();
    }
}
