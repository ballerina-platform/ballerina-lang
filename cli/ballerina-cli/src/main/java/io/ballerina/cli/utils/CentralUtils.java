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

import org.ballerinalang.central.client.CentralAPIClient;

import java.nio.file.Path;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_CENTRAL_ACCESS_TOKEN;
import static io.ballerina.projects.util.ProjectConstants.SETTINGS_FILE_NAME;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_DEV_CENTRAL;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_STAGE_CENTRAL;

/**
 * {@code CentralUtils} has utilities for central commands.
 *
 * @since 2.0.0
 */
public final class CentralUtils {

    private static final String BALLERINA_CENTRAL_PRODUCTION_URL = "https://central.ballerina.io";
    private static final String BALLERINA_CENTRAL_STAGING_URL = "https://staging-central.ballerina.io";
    private static final String BALLERINA_CENTRAL_DEV_URL = "https://dev-central.ballerina.io";

    private CentralUtils() {
    }

    /**
     * Checks if the Ballerina Central access token is available and fails with an actionable error if it is not.
     *
     * @param settingsTomlFilePath `Settings.toml` file path
     * @param client               central API client
     */
    public static void authenticate(Path settingsTomlFilePath, CentralAPIClient client) {
        if (client.accessToken().isEmpty()) {
            throw createLauncherException("Ballerina Central access token is missing in " + settingsTomlFilePath
                    + "\nVisit " + getBallerinaCentralCliTokenUrl() + " to get an access token and add it to the '"
                    + SETTINGS_FILE_NAME + "' file as follows,\n\n\t[central]\n\taccesstoken=\"<access-token>\"\n\n"
                    + "Alternatively, set the '" + BALLERINA_CENTRAL_ACCESS_TOKEN + "' environment variable.");
        }
    }

    public static String getBallerinaCentralCliTokenUrl() {
        if (SET_BALLERINA_STAGE_CENTRAL) {
            return "https://staging-central.ballerina.io/dashboard?tab=token";
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            return "https://dev-central.ballerina.io/dashboard?tab=token";
        } else {
            return "https://central.ballerina.io/dashboard?tab=token";
        }
    }

    /**
     * Get the central package URL.
     *
     * @param org     package org
     * @param pkgName package name
     * @return central package URL
     */
    public static String getCentralPackageURL(String org, String pkgName) {
        if (SET_BALLERINA_STAGE_CENTRAL) {
            return BALLERINA_CENTRAL_STAGING_URL + "/" + org + "/" + pkgName;
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            return BALLERINA_CENTRAL_DEV_URL + "/" + org + "/" + pkgName;
        }
        return BALLERINA_CENTRAL_PRODUCTION_URL + "/" + org + "/" + pkgName;
    }
}
