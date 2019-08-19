/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.tool.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility functions used by tools.
 */
public class OSUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());
    private static final String BALLERINA_HOME_DIR = ".ballerina";
    private static final String BALLERINA_CONFIG = "ballerina-version";

    /**
     * Provide the path of configuration file.
     * @return File path
     */
    public static String getInstalltionPath() {
       // String home = System.getProperty("user.home");
        if (OSUtils.isWindows()) {
            return System.getenv("ProgramFiles") + File.separator + "Ballerina";
        } else if (OSUtils.isUnix() || OSUtils.isSolaris()) {
            return File.separator + "usr" + File.separator + "lib" + File.separator + "ballerina";
        } else if (OSUtils.isMac()) {
            return File.separator + "Library" + File.separator + "Ballerina";
        }
        return null;
    }

    public static String getBallerinaVersionFilePath() throws IOException {

        String userHome = System.getProperty("user.home");
        File file = new File(userHome + File.separator
                + BALLERINA_HOME_DIR + File.separator + BALLERINA_CONFIG);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            InputStream inputStream = OSUtils.class.getResourceAsStream("/META-INF/tool.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            ToolUtil.setVersion(file.getPath(), properties.getProperty("ballerina.version"));
        }
        return System.getProperty("user.home") + File.separator
                + BALLERINA_HOME_DIR + File.separator + BALLERINA_CONFIG;
    }

    public static String getDistributionsPath() {
        return getInstalltionPath() + File.separator + "distributions";
    }

    public static String getUserAgent(String ballerinaVersion, String toolVersion, String distributionType) {
        String os = "none";
        if (OSUtils.isWindows()) {
            os = "win-64";
        } else if (OSUtils.isUnix() || OSUtils.isSolaris()) {
            os = "linux-64";
        } else if (OSUtils.isMac()) {
            os = "macos-64";
        }
        return distributionType + "/" + ballerinaVersion + " (" + os + ") Updater/" + toolVersion;
    }

    private static boolean isWindows() {
        return OS.contains("win");
    }

    private static boolean isMac() {
        return OS.contains("mac");
    }

    private static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    private static boolean isSolaris() {
        return OS.contains("sunos");
    }
}
