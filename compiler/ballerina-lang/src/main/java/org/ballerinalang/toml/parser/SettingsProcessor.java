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
import org.ballerinalang.toml.model.Settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * {@code SettingsProcessor} processes the settings toml file parsed and populate a {@link Settings}.
 *
 * @since 0.964
 */
public class SettingsProcessor {

    private static final PrintStream out = System.out;
    
    /**
     * Get a {@link Settings} object by giving the path to the settings toml file.
     *
     * @param settingsPath Path of the Settings.toml file.
     * @return The settings object.
     * @throws IOException Exception if the file cannot be found
     */
    public static Settings parseTomlContentFromFile(Path settingsPath) throws IOException {
        Toml toml;
        try (InputStream settingsInputStream = new FileInputStream(settingsPath.toFile())) {
            toml = new Toml().read(settingsInputStream);
            return toml.to(Settings.class);
        } catch (IllegalStateException e) {
            out.println("warning: invalid 'Settings.toml' file at:" + settingsPath + " due to:" + e.getMessage());
        }
        return new Settings();
    }
}
