/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.parser;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.toml.model.Settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * SettingHeaders Processor which processes the settings toml file parsed and populate a {@link Settings}.
 *
 * @since 0.964
 */
public class SettingsProcessor {
    
    /**
     * Get a {@link Settings} object by giving the path to the settings toml file.
     *
     * @param settingsPath Path of the Settings.toml file.
     * @return The settings object.
     * @throws IOException Exception if the file cannot be found
     */
    public static Settings parseTomlContentFromFile(Path settingsPath) throws IOException {
        InputStream settingsInputStream = new FileInputStream(settingsPath.toFile());
        return getSettings(settingsInputStream);
    }
    
    /**
     * Get the settings config object by an input stream.
     *
     * @param inputStream Settings.toml file's input stream.
     * @return The Settings object
     */
    private static Settings getSettings(InputStream inputStream) {
        Toml toml;
        try {
            toml = new Toml().read(inputStream);
        } catch (IllegalStateException e) {
            throw new BLangCompilerException("invalid Settings.toml due to " + e.getMessage());
        }
        return toml.to(Settings.class);
    }
}
