/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.cli.utils;

import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.parser.SettingsProcessor;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_DEV_CENTRAL;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_STAGE_CENTRAL;

/**
 * {@code CentralUtils} has utilities for central commands.
 *
 * @since 2.0.0
 */
public class CentralUtils {

    private CentralUtils() {
    }

    /**
     * Checks if the access token is available in Settings.toml or not.
     *
     * @return access token if its present
     */
    public static String authenticate(PrintStream errStream, String ballerinaCentralCliTokenUrl, Settings settings,
            Path settingsTomlFilePath) {
        String accessToken = getAccessTokenOfCLI(settings);

        if (accessToken.isEmpty()) {
            try {
                errStream.println(
                        "Opening the web browser to " + ballerinaCentralCliTokenUrl + " for auto token update ...");

                BrowserLauncher.startInDefaultBrowser(ballerinaCentralCliTokenUrl);
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException(
                        "Access token is missing in " + settingsTomlFilePath.toString()
                                + "\nAuto update failed. Please visit https://central.ballerina.io");
            }
            long modifiedTimeOfFileAtStart = getLastModifiedTimeOfFile(settingsTomlFilePath);
            TokenUpdater.execute(settingsTomlFilePath.toString());

            boolean waitForToken = true;
            while (waitForToken) {
                pause();
                long modifiedTimeOfFileAfter = getLastModifiedTimeOfFile(settingsTomlFilePath);
                if (modifiedTimeOfFileAtStart != modifiedTimeOfFileAfter) {
                    // read updated Settings.toml file to get the token
                    settings = readSettings();
                    accessToken = getAccessTokenOfCLI(settings);
                    if (accessToken.isEmpty()) {
                        throw createLauncherException(
                                "Access token is missing in " + settingsTomlFilePath.toString() + "\nPlease "
                                        + "visit https://central.ballerina.io");
                    } else {
                        waitForToken = false;
                    }
                }
            }
        }
        return accessToken;
    }

    /**
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    static String getAccessTokenOfCLI(Settings settings) {
        // The access token can be specified as an environment variable or in 'Settings.toml'. First we would check if
        // the access token was specified as an environment variable. If not we would read it from 'Settings.toml'
        String tokenAsEnvVar = System.getenv(ProjectConstants.BALLERINA_CENTRAL_ACCESS_TOKEN);
        if (tokenAsEnvVar != null) {
            return tokenAsEnvVar;
        }
        if (settings.getCentral() != null) {
            return settings.getCentral().getAccessToken();
        }
        return "";
    }

    /**
     * Pause for 3s to check if the access token is received.
     */
    private static void pause() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            throw createLauncherException("Error occurred while retrieving the access token");
        }
    }

    /**
     * Get last modified time of file.
     *
     * @param path file path
     * @return last modified time in milliseconds
     */
    private static long getLastModifiedTimeOfFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return -1;
        }
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            throw createLauncherException("Error occurred when reading file for token " + path.toString());
        }
    }

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return {@link Settings} settings object
     */
    public static Settings readSettings() {
        Path settingsFilePath = RepoUtils.createAndGetHomeReposPath().resolve(ProjectConstants.SETTINGS_FILE_NAME);
        try {
            return SettingsProcessor.parseTomlContentFromFile(settingsFilePath);
        } catch (IOException e) {
            return new Settings();
        }
    }

    public static String getBallerinaCentralCliTokenUrl() {
        if (SET_BALLERINA_STAGE_CENTRAL) {
            return "https://staging-central.ballerina.io/cli-token";
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            return "https://dev-central.ballerina.io/cli-token";
        } else {
            return "https://central.ballerina.io/cli-token";
        }
    }
}
