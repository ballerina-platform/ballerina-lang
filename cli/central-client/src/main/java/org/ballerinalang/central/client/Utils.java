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

package org.ballerinalang.central.client;

import org.ballerinalang.central.client.util.ErrorUtil;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.packaging.converters.URIDryConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_DEV_CENTRAL;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_STAGE_CENTRAL;

/**
 * Utils class for this package.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Create http URL connection.
     *
     * @param url   connection URL
     * @param proxy proxy
     * @return http URL connection
     */
    public static HttpURLConnection createHttpUrlConnection(URL url, Proxy proxy) {
        try {
            // set proxy if exists.
            if (proxy == null) {
                return (HttpURLConnection) url.openConnection();
            } else {
                return (HttpURLConnection) url.openConnection(proxy);
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        }
    }

    /**
     * Checks if the access token is available in Settings.toml or not.
     *
     * @return access token if its present
     */
    static String authenticate(PrintStream errStream, String ballerinaCentralCliTokenUrl, Path settingsTomlFilePath) {
        String accessToken = getAccessTokenOfCLI();

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
                    accessToken = getAccessTokenOfCLI();
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
     * initialize proxy if proxy is available in settings.toml.
     *
     * @return proxy
     */
    static Proxy initializeProxy() {
        org.ballerinalang.toml.model.Proxy proxy = TomlParserUtils.readSettings().getProxy();
        if (!"".equals(proxy.getHost())) {
            InetSocketAddress proxyInet = new InetSocketAddress(proxy.getHost(), proxy.getPort());
            if (!"".equals(proxy.getUserName()) && "".equals(proxy.getPassword())) {
                Authenticator authenticator = new URIDryConverter.RemoteAuthenticator();
                Authenticator.setDefault(authenticator);
            }
            return new Proxy(Proxy.Type.HTTP, proxyInet);
        }

        return null;
    }

    /**
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    static String getAccessTokenOfCLI() {
        Settings settings = TomlParserUtils.readSettings();
        // The access token can be specified as an environment variable or in 'Settings.toml'. First we would check if
        // the access token was specified as an environment variable. If not we would read it from 'Settings.toml'
        String tokenAsEnvVar = System.getenv(ProjectDirConstants.BALLERINA_CENTRAL_ACCESS_TOKEN);
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
    static void pause() {
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
    static long getLastModifiedTimeOfFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return -1;
        }
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            throw createLauncherException("Error occurred when reading file for token " + path.toString());
        }
    }

    static String getBallerinaCentralCliTokenUrl() {
        if (SET_BALLERINA_STAGE_CENTRAL) {
            return "https://staging-central.ballerina.io/cli-token";
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            return "https://dev-central.ballerina.io/cli-token";
        } else {
            return "https://central.ballerina.io/cli-token";
        }
    }
}
