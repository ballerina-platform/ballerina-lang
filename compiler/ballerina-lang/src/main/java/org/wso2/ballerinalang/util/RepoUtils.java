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

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Home repository util methods.
 */
public class RepoUtils {

    public static final String BALLERINA_INSTALL_DIR_PROP = "ballerina.home";
    public static final String COMPILE_BALLERINA_ORG_PROP = "BALLERINA_DEV_COMPILE_BALLERINA_ORG";
    public static final String LOAD_BUILTIN_FROM_SOURCE_PROP = "BALLERINA_DEV_LOAD_BUILTIN_FROM_SOURCE";
    public static final boolean COMPILE_BALLERINA_ORG = getBooleanProp(COMPILE_BALLERINA_ORG_PROP);
    public static final boolean LOAD_BUILTIN_FROM_SOURCE = getBooleanProp(LOAD_BUILTIN_FROM_SOURCE_PROP);

    private static final String USER_HOME = "user.home";
    private static final String DEFAULT_TERMINAL_SIZE = "80";
    private static final String BALLERINA_CLI_WIDTH = "BALLERINA_CLI_WIDTH";
    private static final String PRODUCTION_URL = "https://api.central.ballerina.io/packages/";
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io/packages/";
    private static final boolean BALLERINA_DEV_STAGE_CENTRAL = Boolean.parseBoolean(
            System.getenv("BALLERINA_DEV_STAGE_CENTRAL"));

    /**
     * Create and get the home repository path.
     *
     * @return home repository path
     */
    public static Path createAndGetHomeReposPath() {
        Path homeRepoPath;
        String homeRepoDir = System.getenv(ProjectDirConstants.HOME_REPO_ENV_KEY);
        if (homeRepoDir == null || homeRepoDir.isEmpty()) {
            String userHomeDir = System.getProperty(USER_HOME);
            if (userHomeDir == null || userHomeDir.isEmpty()) {
                throw new BLangCompilerException("Error creating home repository: unable to get user home directory");
            }
            homeRepoPath = Paths.get(userHomeDir, ProjectDirConstants.HOME_REPO_DEFAULT_DIRNAME);
        } else {
            // User has specified the home repo path with env variable.
            homeRepoPath = Paths.get(homeRepoDir);
        }

        homeRepoPath = homeRepoPath.toAbsolutePath();
        if (Files.exists(homeRepoPath) && !Files.isDirectory(homeRepoPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new BLangCompilerException("Home repository is not a directory: " + homeRepoPath.toString());
        }
        return homeRepoPath;
    }

    /**
     * Checks if the path is a project repo.
     *
     * @param path dir path
     * @return true if the directory is a project repo, false if its the home repo
     */
    public static boolean hasProjectRepo(Path path) {
        path = path.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME);
        return !path.equals(createAndGetHomeReposPath()) && Files.exists(path, LinkOption.NOFOLLOW_LINKS) &&
                Files.isDirectory(path);
    }

    /**
     * Get the remote repo URL.
     *
     * @return URL of the remote repository
     */
    public static String getRemoteRepoURL() {
        if (BALLERINA_DEV_STAGE_CENTRAL) {
            return STAGING_URL;
        }
        return PRODUCTION_URL;
    }

    public static Path getLibDir() {
        return Paths.get(System.getProperty(BALLERINA_INSTALL_DIR_PROP, ".")).resolve("lib");
    }

    /**
     * Get the terminal width.
     *
     * @return terminal width as a string
     */
    public static String getTerminalWidth() {
        Map<String, String> envVariableMap = System.getenv();
        if (envVariableMap.containsKey(BALLERINA_CLI_WIDTH)) {
            return envVariableMap.get(BALLERINA_CLI_WIDTH);
        }
        return DEFAULT_TERMINAL_SIZE;
    }

    public static Path createAndGetLibsRepoPath() {
        String ballerinaHome = System.getProperty(ProjectDirConstants.BALLERINA_HOME);
        if (ballerinaHome == null || ballerinaHome.isEmpty()) {
            return null;
        }

        return Paths.get(ballerinaHome).resolve(ProjectDirConstants.BALLERINA_HOME_LIB);
    }

    private static boolean getBooleanProp(String key) {
        return Boolean.parseBoolean(System.getProperty(key));
    }


    /**
     * Get the ballerina version the package is built with.
     *
     * @return ballerina version
     */
    public static String getBallerinaVersion() {
        try (InputStream inputStream = RepoUtils.class.getResourceAsStream(ProjectDirConstants.PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(ProjectDirConstants.BALLERINA_VERSION);
        } catch (Throwable ignore) {
        }
        return "unknown";
    }


    /**
     * Validates the org-name and package name.
     *
     * @param orgName The org-name
     * @return True if valid org-name or package name, else false.
     */
    public static boolean validateOrg(String orgName) {
        String validRegex = "^[a-z0-9_]*$";
        return Pattern.matches(validRegex, orgName);
    }

    /**
     * Validates the org-name and package name.
     *
     * @param pkgName The org-name or package name.
     * @return True if valid org-name or package name, else false.
     */
    public static boolean validatePkg(String pkgName) {
        String validRegex = "^[a-zA-Z0-9_.]*$";
        return Pattern.matches(validRegex, pkgName);
    }
}
