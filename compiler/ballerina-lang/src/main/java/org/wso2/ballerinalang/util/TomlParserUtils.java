/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.util;

import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.ballerinalang.toml.parser.SettingsProcessor;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Toml parser util methods.
 *
 * @since 0.982.0
 */
public class TomlParserUtils {
    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return {@link Settings} settings object
     */
    public static Settings readSettings() {
        Path settingsFilePath = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.SETTINGS_FILE_NAME);
        try {
            return SettingsProcessor.parseTomlContentFromFile(settingsFilePath.toString());
        } catch (IOException e) {
            return new Settings();
        }
    }

    /**
     * Read Ballerina.toml (Manifest) to populate the configurations.
     *
     * @param projectDirPath Project Directory Path
     * @return {@link Manifest} manifest object
     */
    public static Manifest getManifest(Path projectDirPath) {
        Path manifestFilePath = projectDirPath.resolve((ProjectDirConstants.MANIFEST_FILE_NAME));
        try {
            return ManifestProcessor.parseTomlContentFromFile(manifestFilePath.toString());
        } catch (IOException e) {
            return new Manifest();
        }
    }
}
